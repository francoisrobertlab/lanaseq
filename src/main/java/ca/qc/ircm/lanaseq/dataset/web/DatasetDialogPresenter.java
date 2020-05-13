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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.ArrayList;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(DatasetDialogPresenter.class);
  private DatasetDialog dialog;
  private Binder<Dataset> binder = new BeanValidationBinder<>(Dataset.class);
  private Binder<Sample> sampleBinder = new BeanValidationBinder<>(Sample.class);
  private ListDataProvider<Sample> samplesDataProvider =
      DataProvider.ofCollection(new ArrayList<>());
  private Dataset dataset;
  private DatasetService service;
  private ProtocolService protocolService;

  @Autowired
  protected DatasetDialogPresenter(DatasetService service, ProtocolService protocolService) {
    this.service = service;
    this.protocolService = protocolService;
  }

  void init(DatasetDialog dialog) {
    this.dialog = dialog;
    dialog.protocol.setItems(protocolService.all());
    dialog.sampleDialog.addSavedListener(e -> savedSample());
    dialog.sampleDialog.addDeletedListener(e -> deletedSample());
    setDataset(null);
  }

  void localeChange(Locale locale) {
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.project).withNullRepresentation("").bind(PROJECT);
    sampleBinder.forField(dialog.protocol).asRequired(webResources.message(REQUIRED))
        .bind(PROTOCOL);
    sampleBinder.forField(dialog.assay).asRequired(webResources.message(REQUIRED)).bind(ASSAY);
    sampleBinder.forField(dialog.type).withNullRepresentation(DatasetType.NULL).bind(TYPE);
    sampleBinder.forField(dialog.target).withNullRepresentation("").bind(TARGET);
    sampleBinder.forField(dialog.strain).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(STRAIN);
    sampleBinder.forField(dialog.strainDescription).withNullRepresentation("")
        .bind(STRAIN_DESCRIPTION);
    sampleBinder.forField(dialog.treatment).withNullRepresentation("").bind(TREATMENT);
  }

  void addSample() {
    dialog.sampleDialog.setSample(null);
    dialog.sampleDialog.open();
  }

  void editSample(Sample sample) {
    dialog.sampleDialog.setSample(sample);
    dialog.sampleDialog.open();
  }

  private void savedSample() {
    Sample sample = dialog.sampleDialog.getSample();
    if (!samplesDataProvider.getItems().contains(sample)) {
      samplesDataProvider.getItems().add(sample);
    }
    refreshSamplesDataProvider();
  }

  private void deletedSample() {
    Sample sample = dialog.sampleDialog.getSample();
    samplesDataProvider.getItems().remove(sample);
    refreshSamplesDataProvider();
  }

  private void refreshSamplesDataProvider() {
    samplesDataProvider = DataProvider.ofCollection(samplesDataProvider.getItems());
    dialog.samples.setDataProvider(samplesDataProvider);
  }

  BinderValidationStatus<Dataset> validateDataset() {
    return binder.validate();
  }

  BinderValidationStatus<Sample> validateSample() {
    return sampleBinder.validate();
  }

  private boolean validate() {
    return validateDataset().isOk() && validateSample().isOk();
  }

  void save(Locale locale) {
    if (validate()) {
      logger.debug("Save dataset {}", dataset);
      dataset.setSamples(new ArrayList<>(samplesDataProvider.getItems()));
      Sample from = sampleBinder.getBean();
      for (Sample sample : dataset.getSamples()) {
        copy(from, sample);
      }
      service.save(dataset);
      AppResources resources = new AppResources(DatasetDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, dataset.getName()));
      dialog.close();
      dialog.fireSavedEvent();
    }
  }

  void cancel() {
    dialog.close();
  }

  Dataset getDataset() {
    return dataset;
  }

  /**
   * Sets dataset.
   *
   * @param dataset
   *          dataset
   */
  void setDataset(Dataset dataset) {
    if (dataset == null) {
      dataset = new Dataset();
    }
    if (dataset.getSamples() == null) {
      dataset.setSamples(new ArrayList<>());
    }
    Sample sample = new Sample();
    copy(dataset.getSamples().stream().findFirst().orElse(new Sample()), sample);
    this.dataset = dataset;
    binder.setBean(dataset);
    sampleBinder.setBean(sample);
    samplesDataProvider = DataProvider.ofCollection(dataset.getSamples());
    dialog.samples.setDataProvider(samplesDataProvider);
  }

  private void copy(Sample from, Sample to) {
    to.setAssay(from.getAssay());
    to.setType(from.getType());
    to.setTarget(from.getTarget());
    to.setStrain(from.getStrain());
    to.setStrainDescription(from.getStrainDescription());
    to.setTreatment(from.getTreatment());
    to.setProtocol(from.getProtocol());
  }
}
