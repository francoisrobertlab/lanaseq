package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.REPLICATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.SAMPLE_ID;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Sample dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleDialogPresenter {
  private SampleDialog dialog;
  private Binder<Sample> binder = new BeanValidationBinder<>(Sample.class);

  void init(SampleDialog dialog) {
    this.dialog = dialog;
    setSample(null);
  }

  public void localeChange(Locale locale) {
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.sampleId).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(SAMPLE_ID);
    binder.forField(dialog.replicate).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(REPLICATE);
  }

  BinderValidationStatus<Sample> validateSample() {
    return binder.validate();
  }

  boolean validate() {
    return validateSample().isOk();
  }

  void save() {
    if (validate()) {
      dialog.fireSavedEvent();
      dialog.close();
    }
  }

  void cancel() {
    dialog.close();
  }

  void delete() {
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
  }
}
