package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME_REGEX;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILE_RENAME_ERROR;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.SampleFile;
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
import java.util.Objects;
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
public class SampleFilesDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(SampleFilesDialogPresenter.class);
  private SampleFilesDialog dialog;
  private Sample sample;
  private SampleService service;
  private AuthorizationService authorizationService;
  private Binder<SampleFile> fileBinder = new BeanValidationBinder<>(SampleFile.class);

  @Autowired
  protected SampleFilesDialogPresenter(SampleService service,
      AuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  void init(SampleFilesDialog dialog) {
    this.dialog = dialog;
    dialog.files.getEditor().setBinder(fileBinder);
    localeChange(Constants.DEFAULT_LOCALE);
  }

  public void localeChange(Locale locale) {
    final AppResources resources = new AppResources(SampleFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
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
    dialog.files.setItems(service.files(sample).stream().map(file -> new SampleFile(file)));
  }

  void rename(SampleFile file, Locale locale) {
    Path source = file.getPath();
    Path target = source.resolveSibling(file.getFilename());
    try {
      logger.debug("rename file {} to {}", source, target);
      Files.move(source, target);
      updateFiles();
    } catch (IOException e) {
      logger.error("renaming of file {} to {} failed", source, target);
      final AppResources resources = new AppResources(SampleFilesDialog.class, locale);
      dialog.showNotification(
          resources.message(FILE_RENAME_ERROR, source.getFileName(), file.getFilename()));
    }
  }

  void deleteFile(SampleFile file, Locale locale) {
    Path path = file.getPath();
    try {
      logger.debug("delete file {}", path);
      Files.delete(path);
      updateFiles();
    } catch (IOException e) {
      logger.error("deleting file {} failed", path);
      final AppResources resources = new AppResources(SampleFilesDialog.class, locale);
      dialog.showNotification(resources.message(FILE_RENAME_ERROR, path.getFileName()));
    }
  }

  BinderValidationStatus<SampleFile> validateSampleFile() {
    return fileBinder.validate();
  }

  Sample getSample() {
    return sample;
  }

  void setSample(Sample sample) {
    Objects.requireNonNull(sample);
    Objects.requireNonNull(sample.getId());
    this.sample = sample;
    boolean readOnly = !authorizationService.hasPermission(sample, Permission.WRITE)
        || (sample.getId() != null && !sample.isEditable());
    fileBinder.setReadOnly(readOnly);
    dialog.delete.setVisible(!readOnly);
    updateFiles();
  }
}
