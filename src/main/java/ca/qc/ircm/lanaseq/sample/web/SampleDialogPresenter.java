/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.Sample.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NOTE;
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
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.util.List;
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
  private Locale locale;
  private SampleService service;
  private ProtocolService protocolService;
  private AuthenticatedUser authenticatedUser;

  @Autowired
  protected SampleDialogPresenter(SampleService service, ProtocolService protocolService,
      AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.protocolService = protocolService;
    this.authenticatedUser = authenticatedUser;
  }

  void init(SampleDialog dialog) {
    this.dialog = dialog;
    dialog.protocol.setItems(protocolService.all());
    dialog.assay.setItems(service.topAssays(50));
    dialog.error.setVisible(false);
    localeChange(Constants.DEFAULT_LOCALE);
    setSample(null);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.date).asRequired(webResources.message(REQUIRED)).bind(DATE);
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
    binder.forField(dialog.note).withNullRepresentation("").bind(NOTE);
  }

  BinderValidationStatus<Sample> validateSample() {
    return binder.validate();
  }

  boolean validate() {
    dialog.error.setVisible(false);
    boolean valid = validateSample().isOk();
    if (valid) {
      Sample sample = binder.getBean();
      if (service.exists(sample.getName()) && (sample.getId() == null || !sample.getName()
          .equalsIgnoreCase(service.get(sample.getId()).map(Sample::getName).orElse("")))) {
        valid = false;
        AppResources sampleResources = new AppResources(Sample.class, locale);
        dialog.error.setText(sampleResources.message(NAME_ALREADY_EXISTS, sample.getName()));
        dialog.error.setVisible(true);
      }
    }
    return valid;
  }

  void save() {
    Sample sample = binder.getBean();
    sample.generateName();
    if (validate()) {
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

  void delete() {
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
    if (sample.getDate() == null) {
      sample.setDate(LocalDate.now());
    }
    binder.setBean(sample);
    boolean readOnly = !authenticatedUser.hasPermission(sample, Permission.WRITE)
        || (sample.getId() != null && !sample.isEditable());
    binder.setReadOnly(readOnly);
    dialog.save.setVisible(!readOnly);
    dialog.cancel.setVisible(!readOnly);
    dialog.delete.setVisible(!readOnly && service.isDeletable(sample));
  }
}
