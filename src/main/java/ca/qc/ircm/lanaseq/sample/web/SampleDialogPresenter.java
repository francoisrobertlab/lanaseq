package ca.qc.ircm.lanaseq.sample.web;

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
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.Permission;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.spring.annotation.SpringComponent;
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
    localeChange(Constants.DEFAULT_LOCALE);
    setSample(null);
  }

  public void localeChange(Locale locale) {
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.sampleId).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(SAMPLE_ID);
    binder.forField(dialog.replicate).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(REPLICATE);
    binder.forField(dialog.protocol).asRequired(webResources.message(REQUIRED)).bind(PROTOCOL);
    binder.forField(dialog.assay).asRequired(webResources.message(REQUIRED)).bind(ASSAY);
    binder.forField(dialog.type).withNullRepresentation(DatasetType.NULL).bind(TYPE);
    binder.forField(dialog.target).withNullRepresentation("").bind(TARGET);
    binder.forField(dialog.strain).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(STRAIN);
    binder.forField(dialog.strainDescription).withNullRepresentation("").bind(STRAIN_DESCRIPTION);
    binder.forField(dialog.treatment).withNullRepresentation("").bind(TREATMENT);
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
    boolean readOnly = !authorizationService.hasPermission(sample, Permission.WRITE);
    binder.setReadOnly(readOnly);
    dialog.save.setVisible(!readOnly);
    dialog.cancel.setVisible(!readOnly);
    dialog.delete.setVisible(!readOnly && service.isDeletable(sample));
  }
}
