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
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NOTE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.Permission;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  private Locale locale = Constants.DEFAULT_LOCALE;
  private Binder<Dataset> binder = new BeanValidationBinder<>(Dataset.class);
  private List<Sample> samples = new ArrayList<>();
  private DatasetService service;
  private AuthorizationService authorizationService;

  @Autowired
  protected DatasetDialogPresenter(DatasetService service,
      AuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  void init(DatasetDialog dialog) {
    this.dialog = dialog;
    dialog.tags.setTagSuggestions(service.topTags(50));
    dialog.selectSampleDialog.addSelectedListener(e -> addSample(e.getSelection()));
    dialog.error.setVisible(false);
    localeChange(Constants.DEFAULT_LOCALE);
    setDataset(null);
    dialog.protocol.setReadOnly(true);
    dialog.assay.setReadOnly(true);
    dialog.type.setReadOnly(true);
    dialog.target.setReadOnly(true);
    dialog.strain.setReadOnly(true);
    dialog.strainDescription.setReadOnly(true);
    dialog.treatment.setReadOnly(true);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
    final AppResources webResources = new AppResources(Constants.class, locale);
    DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
    binder.forField(dialog.namePrefix).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withConverter(
            namePrefix -> namePrefix + dialog.date.getOptionalValue()
                .map(date -> "_" + dateFormatter.format(date)).orElse(""),
            name -> nameToNamePrefix(name))
        .bind(NAME);
    dialog.date.addValueChangeListener(e -> {
      // Force update of dataset name.
      String namePrefix = dialog.namePrefix.getValue();
      dialog.namePrefix.setValue("");
      dialog.namePrefix.setValue(namePrefix);
    });
    binder.forField(dialog.tags).bind(TAGS);
    binder.forField(dialog.date).asRequired(webResources.message(REQUIRED)).bind(DATE);
    binder.forField(dialog.note).withNullRepresentation("").bind(NOTE);
    updateSamplesFields();
  }

  private String nameToNamePrefix(String name) {
    Pattern namePattern = Pattern.compile("(?:(.*)_)?\\d{8}");
    return Optional.ofNullable(name).map(namePattern::matcher).filter(Matcher::matches)
        .map(matcher -> matcher.group(1)).orElse("");
  }

  private boolean isReadOnly(Dataset dataset) {
    return !authorizationService.hasPermission(dataset, Permission.WRITE)
        || (dataset.getId() != null && !dataset.isEditable());
  }

  private void setReadOnly() {
    Dataset dataset = binder.getBean();
    binder.setReadOnly(isReadOnly(dataset));
  }

  private void updateSamplesInBinderBean() {
    Dataset dataset = binder.getBean();
    dataset.setSamples(new ArrayList<>(samples));
    for (int i = dataset.getSamples().size() - 1; i >= 0; i--) {
      if (empty(dataset.getSamples().get(i))) {
        dataset.getSamples().remove(i);
      }
    }
  }

  void generateName() {
    updateSamplesInBinderBean();
    Dataset dataset = binder.getBean();
    dataset.generateName();
    dialog.namePrefix.setValue(nameToNamePrefix(dataset.getName()));
  }

  void addSample() {
    dialog.selectSampleDialog.open();
  }

  private void addSample(Sample sample) {
    if (!samples.stream().filter(sa -> sa.getId() != null && sa.getId().equals(sample.getId()))
        .findAny().isPresent()) {
      samples.add(sample);
      refreshSamplesDataProvider();
    }
  }

  void removeSample(Sample sample) {
    samples.remove(sample);
    refreshSamplesDataProvider();
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

  private boolean validate() {
    dialog.error.setVisible(false);
    boolean valid = validateDataset().isOk();
    if (valid) {
      Dataset dataset = binder.getBean();
      if (service.exists(dataset.getName()) && (dataset.getId() == null || !dataset.getName()
          .equalsIgnoreCase(service.get(dataset.getId()).map(Dataset::getName).orElse("")))) {
        valid = false;
        AppResources datasetResources = new AppResources(Dataset.class, locale);
        dialog.error.setText(datasetResources.message(NAME_ALREADY_EXISTS, dataset.getName()));
        dialog.error.setVisible(true);
      }
    }
    return valid;
  }

  void save() {
    updateSamplesInBinderBean();
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
    binder.setBean(dataset);
    setReadOnly();
    updateSamplesFields();
  }

  private void updateSamplesFields() {
    samples =
        Optional.ofNullable(binder.getBean()).map(Dataset::getSamples).orElse(new ArrayList<>());
    dialog.samples.setItems(samples);
    dialog.protocol.setValue(samples.stream().map(Sample::getProtocol).filter(Objects::nonNull)
        .map(Protocol::getName).distinct().collect(Collectors.joining(", ")));
    dialog.assay.setValue(samples.stream().map(Sample::getAssay).filter(Objects::nonNull).distinct()
        .map(assay -> assay.getLabel(locale)).collect(Collectors.joining(", ")));
    dialog.type.setValue(samples.stream().map(Sample::getType).filter(Objects::nonNull).distinct()
        .map(type -> type.getLabel(locale)).collect(Collectors.joining(", ")));
    dialog.target.setValue(samples.stream().map(Sample::getTarget).filter(Objects::nonNull)
        .distinct().collect(Collectors.joining(", ")));
    dialog.strain.setValue(samples.stream().map(Sample::getStrain).filter(Objects::nonNull)
        .distinct().collect(Collectors.joining(", ")));
    dialog.strainDescription.setValue(samples.stream().map(Sample::getStrainDescription)
        .filter(Objects::nonNull).distinct().collect(Collectors.joining(", ")));
    dialog.treatment.setValue(samples.stream().map(Sample::getTreatment).filter(Objects::nonNull)
        .distinct().collect(Collectors.joining(", ")));
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

  List<Sample> getSamples() {
    return samples;
  }
}
