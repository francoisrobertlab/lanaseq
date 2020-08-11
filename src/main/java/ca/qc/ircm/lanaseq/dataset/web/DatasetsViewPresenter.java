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

import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE_ERROR;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Datasets view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetsViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(DatasetsViewPresenter.class);
  private DatasetsView view;
  @Autowired
  private DatasetService service;
  @Autowired
  private ProtocolService protocolService;
  @Autowired
  private SampleService sampleService;

  protected DatasetsViewPresenter() {
  }

  protected DatasetsViewPresenter(DatasetService service, ProtocolService protocolService,
      SampleService sampleService) {
    this.service = service;
    this.protocolService = protocolService;
    this.sampleService = sampleService;
  }

  void init(DatasetsView view) {
    logger.debug("Datasets view");
    this.view = view;
    view.dialog.addSavedListener(e -> view.datasets.refreshDatasets());
    view.protocolDialog.addSavedListener(e -> view.datasets.refreshDatasets());
    clearError();
  }

  private void clearError() {
    view.error.setVisible(false);
  }

  void view(Dataset dataset) {
    clearError();
    view.dialog.setDataset(service.get(dataset.getId()));
    view.dialog.open();
  }

  void viewFiles(Locale locale) {
    List<Dataset> datasets = new ArrayList<>(view.datasets.getSelectedItems());
    AppResources resources = new AppResources(DatasetsView.class, locale);
    boolean error = false;
    if (datasets.isEmpty()) {
      view.error.setText(resources.message(DATASETS_REQUIRED));
      error = true;
    } else if (datasets.size() > 1) {
      view.error.setText(resources.message(DATASETS_MORE_THAN_ONE));
      error = true;
    }
    view.error.setVisible(error);
    if (!error) {
      Dataset dataset = datasets.iterator().next();
      viewFiles(dataset);
    }
  }

  void viewFiles(Dataset dataset) {
    view.filesDialog.setDataset(dataset);
    view.filesDialog.open();
  }

  void view(Protocol protocol) {
    clearError();
    view.protocolDialog.setProtocol(protocolService.get(protocol.getId()));
    view.protocolDialog.open();
  }

  void add() {
    clearError();
    view.dialog.setDataset(null);
    view.dialog.open();
  }

  private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  void merge(Locale locale) {
    clearError();
    List<Dataset> datasets = view.datasets.getSelectedItems().stream()
        .sorted((d1, d2) -> d1.getId().compareTo(d2.getId())).collect(Collectors.toList());
    Set<String> tags = datasets.stream().flatMap(dataset -> dataset.getTags().stream())
        .collect(Collectors.toSet());
    List<Sample> samples = datasets.stream().flatMap(dataset -> dataset.getSamples().stream())
        .filter(distinctByKey(Sample::getId)).sorted((s1, s2) -> s1.getId().compareTo(s2.getId()))
        .collect(Collectors.toList());
    AppResources resources = new AppResources(DatasetsView.class, locale);
    boolean error = false;
    if (samples.isEmpty()) {
      view.error.setText(resources.message(DATASETS_REQUIRED));
      error = true;
    } else if (!sampleService.isMergable(samples)) {
      view.error.setText(resources.message(MERGE_ERROR));
      error = true;
    }
    view.error.setVisible(error);
    if (!error) {
      Dataset dataset = new Dataset();
      dataset.setTags(tags);
      dataset.setSamples(samples);
      dataset.setDate(datasets.get(0).getDate());
      dataset.generateName();
      if (service.exists(dataset.getName())) {
        AppResources datasetResources = new AppResources(Dataset.class, locale);
        view.error.setText(datasetResources.message(NAME_ALREADY_EXISTS, dataset.getName()));
        view.error.setVisible(true);
      } else {
        service.save(dataset);
        view.showNotification(resources.message(MERGED, dataset.getName()));
      }
    }
  }
}
