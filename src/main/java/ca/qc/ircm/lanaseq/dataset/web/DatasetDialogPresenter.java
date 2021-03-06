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
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
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
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleProperties;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.Permission;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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
  private Locale locale;
  private Binder<Dataset> binder = new BeanValidationBinder<>(Dataset.class);
  private Binder<Sample> sampleBinder = new BeanValidationBinder<>(Sample.class);
  private Map<Sample, Binder<Sample>> sampleBinders = new HashMap<>();
  private List<Sample> samples = new ArrayList<>();
  private DatasetService service;
  private ProtocolService protocolService;
  private AuthorizationService authorizationService;

  @Autowired
  protected DatasetDialogPresenter(DatasetService service, ProtocolService protocolService,
      AuthorizationService authorizationService) {
    this.service = service;
    this.protocolService = protocolService;
    this.authorizationService = authorizationService;
  }

  void init(DatasetDialog dialog) {
    this.dialog = dialog;
    dialog.protocol.setItems(protocolService.all());
    dialog.tags.setTagSuggestions(service.topTags(50));
    dialog.selectSampleDialog.addSelectedListener(e -> addSample(e.getSelection()));
    dialog.error.setVisible(false);
    localeChange(Constants.DEFAULT_LOCALE);
    setDataset(null);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.tags).bind(TAGS);
    sampleBinder.forField(dialog.protocol).asRequired(webResources.message(REQUIRED))
        .bind(PROTOCOL);
    sampleBinder.forField(dialog.assay).asRequired(webResources.message(REQUIRED)).bind(ASSAY);
    sampleBinder.forField(dialog.type).withNullRepresentation(SampleType.NULL).bind(TYPE);
    sampleBinder.forField(dialog.target).withNullRepresentation("").bind(TARGET);
    sampleBinder.forField(dialog.strain).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(STRAIN);
    sampleBinder.forField(dialog.strainDescription).withNullRepresentation("")
        .bind(STRAIN_DESCRIPTION);
    sampleBinder.forField(dialog.treatment).withNullRepresentation("").bind(TREATMENT);
    binder.forField(dialog.date).asRequired(webResources.message(REQUIRED)).bind(DATE);
  }

  private boolean isReadOnly(Dataset dataset) {
    return !authorizationService.hasPermission(dataset, Permission.WRITE)
        || (dataset.getId() != null && !dataset.isEditable());
  }

  private void setReadOnly() {
    Dataset dataset = binder.getBean();
    boolean readOnly = isReadOnly(dataset);
    binder.setReadOnly(readOnly);
    boolean sampleReadOnly = readOnly || !samples.stream()
        .map(sa -> authorizationService.hasPermission(sa, Permission.WRITE) && sa.isEditable())
        .filter(val -> val).findFirst().orElse(false);
    sampleBinder.setReadOnly(sampleReadOnly);
  }

  private void bindSampleFields(Sample sample) {
    final boolean forceReadOnly = isReadOnly(binder.getBean()) || !sample.isEditable();
    final AppResources webResources = new AppResources(Constants.class, locale);
    Binder<Sample> binder = new BeanValidationBinder<Sample>(Sample.class);
    binder.forField(dialog.sampleIdField(sample))
        .asRequired(sampleRequiredValidator(sample, webResources.message(REQUIRED)))
        .withNullRepresentation("").bind(SampleProperties.SAMPLE_ID);
    binder.forField(dialog.sampleReplicateField(sample))
        .asRequired(sampleRequiredValidator(sample, webResources.message(REQUIRED)))
        .withNullRepresentation("").bind(SampleProperties.REPLICATE);
    binder.setBean(sample);
    binder.setReadOnly(
        forceReadOnly || !authorizationService.hasPermission(sample, Permission.WRITE));
    sampleBinders.put(sample, binder);
  }

  void addNewSample() {
    Sample sample = new Sample();
    sample.setEditable(true);
    samples.add(sample);
    bindSampleFields(sample);
    refreshSamplesDataProvider();
    setReadOnly();
  }

  void addSample() {
    dialog.selectSampleDialog.open();
  }

  private void addSample(Sample sample) {
    if (!samples.stream().filter(sa -> sa.getId() != null && sa.getId().equals(sample.getId()))
        .findAny().isPresent()) {
      samples.add(sample);
      bindSampleFields(sample);
      refreshSamplesDataProvider();
      setReadOnly();
    }
  }

  void removeSample(Sample sample) {
    samples.remove(sample);
    refreshSamplesDataProvider();
    setReadOnly();
  }

  void dropSample(Sample dragged, Sample drop, GridDropLocation dropLocation) {
    if (!dragged.equals(drop)) {
      samples.remove(dragged);
      int dropIndex = samples.indexOf(drop) + (dropLocation == GridDropLocation.BELOW ? 1 : 0);
      samples.add(dropIndex, dragged);
      refreshSamplesDataProvider();
    }
  }

  private void refreshSamplesDataProvider() {
    dialog.samples.getDataProvider().refreshAll();
  }

  BinderValidationStatus<Dataset> validateDataset() {
    return binder.validate();
  }

  BinderValidationStatus<Sample> validateSample() {
    return sampleBinder.validate();
  }

  List<BinderValidationStatus<Sample>> validateSamples() {
    return samples.stream().map(sample -> sampleBinders.get(sample).validate())
        .collect(Collectors.toList());
  }

  private boolean validate() {
    dialog.error.setVisible(false);
    boolean valid = validateDataset().isOk() && validateSample().isOk()
        && !validateSamples().stream().filter(status -> !status.isOk()).findAny().isPresent();
    if (valid) {
      Dataset dataset = binder.getBean();
      dataset.setSamples(new ArrayList<>(samples));
      for (int i = dataset.getSamples().size() - 1; i >= 0; i--) {
        if (empty(dataset.getSamples().get(i))) {
          dataset.getSamples().remove(i);
        }
      }
      Sample from = sampleBinder.getBean();
      for (Sample sample : dataset.getSamples()) {
        copy(from, sample);
        if (sample.getId() == null) {
          sample.setDate(dataset.getDate());
        }
      }
      dataset.generateName();
      if (service.exists(dataset.getName()) && (dataset.getId() == null
          || !dataset.getName().equalsIgnoreCase(service.get(dataset.getId()).getName()))) {
        valid = false;
        AppResources datasetResources = new AppResources(Dataset.class, locale);
        dialog.error.setText(datasetResources.message(NAME_ALREADY_EXISTS, dataset.getName()));
        dialog.error.setVisible(true);
      }
    }
    return valid;
  }

  void save() {
    if (validate()) {
      Dataset dataset = binder.getBean();
      logger.debug("save dataset {}", dataset);
      service.save(dataset);
      AppResources resources = new AppResources(DatasetDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, dataset.getName()));
      dialog.close();
      dialog.fireSavedEvent();
    }
  }

  private boolean empty(Sample sample) {
    return sample.getSampleId() == null && sample.getReplicate() == null;
  }

  void delete() {
    Dataset dataset = binder.getBean();
    logger.debug("delete dataset {}", dataset);
    service.delete(dataset);
    AppResources resources = new AppResources(DatasetDialog.class, locale);
    dialog.showNotification(resources.message(DELETED, dataset.getName()));
    dialog.fireDeletedEvent();
    dialog.close();
  }

  void cancel() {
    dialog.close();
  }

  Dataset getDataset() {
    return binder.getBean();
  }

  void setDataset(Dataset dataset) {
    if (dataset == null) {
      dataset = new Dataset();
    }
    if (dataset.getTags() == null) {
      dataset.setTags(new HashSet<>());
    }
    if (dataset.getDate() == null) {
      dataset.setDate(LocalDate.now());
    }
    if (dataset.getSamples() == null) {
      dataset.setSamples(new ArrayList<>());
    }
    if (dataset.getId() == null) {
      while (dataset.getSamples().size() < 2) {
        Sample sample = new Sample();
        sample.setEditable(true);
        dataset.getSamples().add(sample);
      }
    }
    Sample sample = new Sample();
    copy(dataset.getSamples().stream().findFirst().orElse(new Sample()), sample);
    binder.setBean(dataset);
    sampleBinder.setBean(sample);
    samples = new ArrayList<>(dataset.getSamples());
    dialog.samples.setItems(samples);
    dataset.getSamples().forEach(s -> bindSampleFields(s));
    setReadOnly();
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

  private Validator<String> sampleRequiredValidator(Sample sample, String errorMessage) {
    return (value,
        context) -> (!dialog.sampleIdField(sample).isEmpty()
            || !dialog.sampleReplicateField(sample).isEmpty()) && value.isEmpty()
                ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
  }

  List<Sample> getSamples() {
    return samples;
  }
}
