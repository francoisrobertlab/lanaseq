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

import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGE_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES_REQUIRED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Samples view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SamplesViewPresenter {
  private SamplesView view;
  private ListDataProvider<Sample> samplesDataProvider;
  private WebSampleFilter filter = new WebSampleFilter();
  private Locale locale;
  private SampleService service;
  private ProtocolService protocolService;
  private DatasetService datasetService;
  private AuthorizationService authorizationService;

  @Autowired
  SamplesViewPresenter(SampleService service, ProtocolService protocolService,
      DatasetService datasetService, AuthorizationService authorizationService) {
    this.service = service;
    this.protocolService = protocolService;
    this.datasetService = datasetService;
    this.authorizationService = authorizationService;
  }

  void init(SamplesView view) {
    this.view = view;
    if (!authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      view.ownerFilter.setValue(authorizationService.getCurrentUser().getEmail());
    }
    loadSamples();
    view.dialog.addSavedListener(e -> loadSamples());
    view.dialog.addDeletedListener(e -> loadSamples());
    view.protocolDialog.addSavedListener(e -> loadSamples());
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  private void loadSamples() {
    samplesDataProvider = DataProvider.ofCollection(service.all());
    ConfigurableFilterDataProvider<Sample, Void, SerializablePredicate<Sample>> dataProvider =
        samplesDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    view.samples.setDataProvider(dataProvider);
  }

  void view(Sample sample) {
    view.dialog.setSample(service.get(sample.getId()));
    view.dialog.open();
  }

  void viewFiles(Sample sample) {
    view.filesDialog.setSample(sample);
    view.filesDialog.open();
  }

  void viewFiles() {
    List<Sample> samples = new ArrayList<>(view.samples.getSelectedItems());
    AppResources resources = new AppResources(SamplesView.class, locale);
    boolean error = false;
    if (samples.isEmpty()) {
      view.error.setText(resources.message(SAMPLES_REQUIRED));
      error = true;
    } else if (samples.size() > 1) {
      view.error.setText(resources.message(SAMPLES_MORE_THAN_ONE));
      error = true;
    }
    view.error.setVisible(error);
    if (!error) {
      Sample sample = samples.iterator().next();
      viewFiles(sample);
    }
  }

  void viewProtocol(Protocol protocol) {
    view.protocolDialog.setProtocol(protocolService.get(protocol.getId()));
    view.protocolDialog.open();
  }

  void add() {
    view.dialog.setSample(null);
    view.dialog.open();
  }

  void merge() {
    List<Sample> samples = view.samples.getSelectedItems().stream()
        .sorted((s1, s2) -> s1.getId().compareTo(s2.getId())).collect(Collectors.toList());
    AppResources resources = new AppResources(SamplesView.class, locale);
    boolean error = false;
    if (samples.isEmpty()) {
      view.error.setText(resources.message(SAMPLES_REQUIRED));
      error = true;
    } else if (!service.isMergable(samples)) {
      view.error.setText(resources.message(MERGE_ERROR));
      error = true;
    }
    view.error.setVisible(error);
    if (!error) {
      Dataset dataset = new Dataset();
      dataset.setSamples(samples);
      dataset.setTags(new HashSet<>());
      dataset.setDate(samples.get(0).getDate());
      dataset.generateName();
      if (datasetService.exists(dataset.getName())) {
        AppResources datasetResources = new AppResources(Dataset.class, locale);
        view.error.setText(datasetResources.message(NAME_ALREADY_EXISTS, dataset.getName()));
        view.error.setVisible(true);
      } else {
        datasetService.save(dataset);
        view.showNotification(resources.message(MERGED, dataset.getName()));
      }
    }
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    view.samples.getDataProvider().refreshAll();
  }

  void filterProtocol(String value) {
    filter.protocolContains = value.isEmpty() ? null : value;
    view.samples.getDataProvider().refreshAll();
  }

  void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    view.samples.getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    view.samples.getDataProvider().refreshAll();
  }

  WebSampleFilter filter() {
    return filter;
  }
}
