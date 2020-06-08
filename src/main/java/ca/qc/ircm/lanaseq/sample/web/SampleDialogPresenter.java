package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.REPLICATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.SAMPLE_ID;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.FILENAME_REGEX;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.FILE_RENAME_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.sample.web.SampleDialog.SampleFile;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.Permission;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Sample dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(SampleDialogPresenter.class);
  private SampleDialog dialog;
  private Binder<Sample> binder = new BeanValidationBinder<>(Sample.class);
  private SampleService service;
  private ProtocolService protocolService;
  private AuthorizationService authorizationService;
  private Binder<SampleFile> fileBinder = new BeanValidationBinder<>(SampleFile.class);

  @Autowired
  protected SampleDialogPresenter(SampleService service, ProtocolService protocolService,
      AuthorizationService authorizationService) {
    this.service = service;
    this.protocolService = protocolService;
    this.authorizationService = authorizationService;
  }

  void init(SampleDialog dialog) {
    this.dialog = dialog;
    dialog.protocol.setItems(protocolService.all());
    dialog.files.getEditor().setBinder(fileBinder);
    localeChange(Constants.DEFAULT_LOCALE);
    setSample(null);
  }

  public void localeChange(Locale locale) {
    final AppResources resources = new AppResources(SampleDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.sampleId).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(SAMPLE_ID);
    binder.forField(dialog.replicate).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(REPLICATE);
    binder.forField(dialog.protocol).asRequired(webResources.message(REQUIRED)).bind(PROTOCOL);
    binder.forField(dialog.assay).asRequired(webResources.message(REQUIRED)).bind(ASSAY);
    binder.forField(dialog.type).withNullRepresentation(SampleType.NULL).bind(TYPE);
    binder.forField(dialog.target).withNullRepresentation("").bind(TARGET);
    binder.forField(dialog.strain).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(STRAIN);
    binder.forField(dialog.strainDescription).withNullRepresentation("").bind(STRAIN_DESCRIPTION);
    binder.forField(dialog.treatment).withNullRepresentation("").bind(TREATMENT);
    fileBinder.forField(dialog.filenameEdit).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(new RegexpValidator(resources.message(FILENAME_REGEX_ERROR), FILENAME_REGEX))
        .withValidator(exists(locale)).bind(FILENAME);
  }

  private Validator<String> exists(Locale locale) {
    return (value, context) -> {
      SampleFile item = dialog.files.getEditor().getItem();
      if (value != null && item != null && !value.equals(item.getPath().getFileName().toString())
          && Files.exists(item.getPath().resolveSibling(value))) {
        final AppResources webResources = new AppResources(Constants.class, locale);
        return ValidationResult.error(webResources.message(ALREADY_EXISTS));
      }
      return ValidationResult.ok();
    };
  }

  private void updateFiles() {
    dialog.files
        .setItems(service.files(binder.getBean()).stream().map(file -> new SampleFile(file)));
  }

  void rename(SampleFile file, Locale locale) {
    Path source = file.getPath();
    Path target = source.resolveSibling(file.getFilename());
    try {
      logger.debug("rename {} to {}", source, target);
      Files.move(source, target);
      updateFiles();
    } catch (IOException e) {
      logger.error("renaming of file {} to {} failed", source, target);
      final AppResources resources = new AppResources(SampleDialog.class, locale);
      dialog.showNotification(
          resources.message(FILE_RENAME_ERROR, source.getFileName(), file.getFilename()));
    }
  }

  BinderValidationStatus<SampleFile> validateSampleFile() {
    return fileBinder.validate();
  }

  BinderValidationStatus<Sample> validateSample() {
    return binder.validate();
  }

  boolean validate() {
    return validateSample().isOk();
  }

  void save(Locale locale) {
    if (validate()) {
      Sample sample = binder.getBean();
      logger.debug("save sample {}", sample);
      service.save(sample);
      AppResources resources = new AppResources(SampleDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, sample.getName()));
      dialog.fireSavedEvent();
      dialog.close();
    }
  }

  void cancel() {
    dialog.close();
  }

  void delete(Locale locale) {
    Sample sample = binder.getBean();
    logger.debug("delete sample {}", sample);
    service.delete(sample);
    AppResources resources = new AppResources(SampleDialog.class, locale);
    dialog.showNotification(resources.message(DELETED, sample.getName()));
    dialog.fireDeletedEvent();
    dialog.close();
  }

  Sample getSample() {
    return binder.getBean();
  }

  void setSample(Sample sample) {
    if (sample == null) {
      sample = new Sample();
    }
    binder.setBean(sample);
    boolean readOnly = !authorizationService.hasPermission(sample, Permission.WRITE)
        || (sample.getId() != null && !sample.isEditable());
    binder.setReadOnly(readOnly);
    fileBinder.setReadOnly(readOnly);
    updateFiles();
    dialog.save.setVisible(!readOnly);
    dialog.cancel.setVisible(!readOnly);
    dialog.delete.setVisible(!readOnly && service.isDeletable(sample));
  }
}
