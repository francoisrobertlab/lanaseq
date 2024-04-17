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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Datasets view.
 */
@Route(value = DatasetsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class DatasetsView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent {
  public static final String VIEW_NAME = "datasets";
  public static final String ID = "datasets-view";
  public static final String HEADER = "header";
  public static final String DATASETS = "datasets";
  public static final String MERGE = "merge";
  public static final String FILES = "files";
  public static final String ANALYZE = "analyze";
  public static final String MERGE_ERROR = property(MERGE, "error");
  public static final String DATASETS_REQUIRED = property(DATASETS, REQUIRED);
  public static final String DATASETS_MORE_THAN_ONE = property(DATASETS, "moreThanOne");
  public static final String MERGED = "merged";
  private static final long serialVersionUID = 2568742367790329628L;
  private static final Logger logger = LoggerFactory.getLogger(DatasetsView.class);
  protected H2 header = new H2();
  protected Div error = new Div();
  protected Button merge = new Button();
  protected Button files = new Button();
  protected Button analyze = new Button();
  protected DatasetGrid datasets;
  protected ObjectFactory<DatasetDialog> dialogFactory;
  protected ObjectFactory<DatasetFilesDialog> filesDialogFactory;
  protected ObjectFactory<DatasetsAnalysisDialog> analysisDialogFactory;
  private transient DatasetService service;
  private transient SampleService sampleService;

  public DatasetsView() {
  }

  @Autowired
  protected DatasetsView(DatasetGrid datasets, ObjectFactory<DatasetDialog> dialogFactory,
      ObjectFactory<DatasetFilesDialog> filesDialogFactory,
      ObjectFactory<DatasetsAnalysisDialog> analysisDialogFactory, DatasetService service,
      SampleService sampleService) {
    this.datasets = datasets;
    this.dialogFactory = dialogFactory;
    this.filesDialogFactory = filesDialogFactory;
    this.analysisDialogFactory = analysisDialogFactory;
    this.service = service;
    this.sampleService = sampleService;
  }

  @PostConstruct
  void init() {
    logger.debug("datasets view");
    setId(ID);
    setHeightFull();
    add(header, datasets, error, new HorizontalLayout(merge, files, analyze));
    expand(datasets);
    header.setId(HEADER);
    datasets.setSelectionMode(SelectionMode.MULTI);
    datasets.addItemClickListener(e -> {
      if (e.isCtrlKey() || e.isMetaKey()) {
        viewFiles(e.getItem());
      }
    });
    datasets.addEditListener(e -> view(e.getItem()));
    datasets.addItemDoubleClickListener(e -> view(e.getItem()));
    error.setId(ERROR_TEXT);
    error.addClassName(ERROR_TEXT);
    merge.setId(MERGE);
    merge.setIcon(VaadinIcon.CONNECT.create());
    merge.addClickListener(e -> merge());
    files.setId(FILES);
    files.setIcon(VaadinIcon.FILE_O.create());
    files.addClickListener(e -> viewFiles());
    analyze.setId(ANALYZE);
    analyze.addClickListener(e -> analyze());
    clearError();
  }

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetsView.class, getLocale());
    header.setText(resources.message(HEADER));
    merge.setText(resources.message(MERGE));
    files.setText(resources.message(FILES));
    analyze.setText(resources.message(ANALYZE));
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(DatasetsView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }

  private void clearError() {
    error.setVisible(false);
  }

  void view(Dataset dataset) {
    clearError();
    DatasetDialog dialog = dialogFactory.getObject();
    dialog.setDataset(service.get(dataset.getId()).orElse(null));
    dialog.addSavedListener(e -> datasets.refreshDatasets());
    dialog.addDeletedListener(e -> datasets.refreshDatasets());
    dialog.open();
  }

  void viewFiles() {
    List<Dataset> datasets = new ArrayList<>(this.datasets.getSelectedItems());
    AppResources resources = new AppResources(DatasetsView.class, getLocale());
    boolean error = false;
    if (datasets.isEmpty()) {
      this.error.setText(resources.message(DATASETS_REQUIRED));
      error = true;
    } else if (datasets.size() > 1) {
      this.error.setText(resources.message(DATASETS_MORE_THAN_ONE));
      error = true;
    }
    this.error.setVisible(error);
    if (!error) {
      Dataset dataset = datasets.iterator().next();
      viewFiles(dataset);
    }
  }

  void viewFiles(Dataset dataset) {
    DatasetFilesDialog filesDialog = filesDialogFactory.getObject();
    filesDialog.setDataset(dataset);
    filesDialog.open();
  }

  void analyze() {
    List<Dataset> datasets = new ArrayList<>(this.datasets.getSelectedItems());
    AppResources resources = new AppResources(DatasetsView.class, getLocale());
    boolean error = false;
    if (datasets.isEmpty()) {
      this.error.setText(resources.message(DATASETS_REQUIRED));
      error = true;
    }
    this.error.setVisible(error);
    if (!error) {
      DatasetsAnalysisDialog analysisDialog = analysisDialogFactory.getObject();
      analysisDialog.setDatasets(datasets);
      analysisDialog.open();
    }
  }

  private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  void merge() {
    clearError();
    List<Dataset> datasets = this.datasets.getSelectedItems().stream()
        .sorted((d1, d2) -> d1.getId().compareTo(d2.getId())).collect(Collectors.toList());
    Set<String> tags = datasets.stream().flatMap(dataset -> dataset.getTags().stream())
        .collect(Collectors.toSet());
    List<Sample> samples = datasets.stream().flatMap(dataset -> dataset.getSamples().stream())
        .filter(distinctByKey(Sample::getId)).sorted((s1, s2) -> s1.getId().compareTo(s2.getId()))
        .collect(Collectors.toList());
    AppResources resources = new AppResources(DatasetsView.class, getLocale());
    boolean error = false;
    if (samples.isEmpty()) {
      this.error.setText(resources.message(DATASETS_REQUIRED));
      error = true;
    } else if (!sampleService.isMergable(samples)) {
      this.error.setText(resources.message(MERGE_ERROR));
      error = true;
    }
    this.error.setVisible(error);
    if (!error) {
      Dataset dataset = new Dataset();
      dataset.setTags(tags);
      dataset.setSamples(samples);
      dataset.setDate(datasets.get(0).getDate());
      dataset.generateName();
      if (service.exists(dataset.getName())) {
        AppResources datasetResources = new AppResources(Dataset.class, getLocale());
        this.error.setText(datasetResources.message(NAME_ALREADY_EXISTS, dataset.getName()));
        this.error.setVisible(true);
      } else {
        service.save(dataset);
        this.datasets.refreshDatasets();
        showNotification(resources.message(MERGED, dataset.getName()));
      }
    }
  }
}
