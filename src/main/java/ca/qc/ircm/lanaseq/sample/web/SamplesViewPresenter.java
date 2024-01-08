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
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleFilter;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.web.VaadinSort;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback;
import com.vaadin.flow.data.provider.DataProvider;
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
  private DataProvider<Sample, SampleFilter> samplesDataProvider;
  private WebSampleFilter filter = new WebSampleFilter();
  private Locale locale;
  private SampleService service;
  private DatasetService datasetService;
  private AuthenticatedUser authenticatedUser;

  @Autowired
  SamplesViewPresenter(SampleService service, DatasetService datasetService,
      AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.datasetService = datasetService;
    this.authenticatedUser = authenticatedUser;
  }

  void init(SamplesView view) {
    this.view = view;
    if (!authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      authenticatedUser.getUser().ifPresent(user -> view.ownerFilter.setValue(user.getEmail()));
    }
    loadSamples();
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  private void loadSamples() {
    FetchCallback<Sample, Void> fetchCallback = query -> {
      filter.sort = VaadinSort.springDataSort(query.getSortOrders());
      filter.page = query.getOffset() / view.samples.getPageSize();
      filter.size = query.getLimit();
      return service.all(filter).stream();
    };
    view.samples.setItems(fetchCallback);
  }

  void view(Sample sample) {
    showDialog(service.get(sample.getId()).orElse(null));
  }

  private void showDialog(Sample sample) {
    SampleDialog dialog = view.dialogFactory.getObject();
    dialog.setSample(sample);
    dialog.addSavedListener(e -> view.samples.getDataProvider().refreshAll());
    dialog.addDeletedListener(e -> view.samples.getDataProvider().refreshAll());
    dialog.open();
  }

  void viewFiles(Sample sample) {
    SampleFilesDialog filesDialog = view.filesDialogFactory.getObject();
    filesDialog.setSample(sample);
    filesDialog.open();
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

  void analyze() {
    List<Sample> samples = new ArrayList<>(view.samples.getSelectedItems());
    AppResources resources = new AppResources(SamplesView.class, locale);
    boolean error = false;
    if (samples.isEmpty()) {
      view.error.setText(resources.message(SAMPLES_REQUIRED));
      error = true;
    }
    view.error.setVisible(error);
    if (!error) {
      SamplesAnalysisDialog analysisDialog = view.analysisDialogFactory.getObject();
      analysisDialog.setSamples(samples);
      analysisDialog.open();
    }
  }

  void add() {
    showDialog(null);
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
