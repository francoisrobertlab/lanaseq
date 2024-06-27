package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;

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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
  private static final String MESSAGE_PREFIX = messagePrefix(DatasetsView.class);
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = 2568742367790329628L;
  private static final Logger logger = LoggerFactory.getLogger(DatasetsView.class);
  protected H2 header = new H2();
  protected Div error = new Div();
  protected Button merge = new Button();
  protected Button files = new Button();
  protected Button analyze = new Button();
  protected DatasetGrid datasets;
  private transient ObjectFactory<DatasetDialog> dialogFactory;
  private transient ObjectFactory<DatasetFilesDialog> filesDialogFactory;
  private transient ObjectFactory<DatasetsAnalysisDialog> analysisDialogFactory;
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
    datasets.setMinHeight("30em");
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
    header.setText(getTranslation(MESSAGE_PREFIX + HEADER));
    merge.setText(getTranslation(MESSAGE_PREFIX + MERGE));
    files.setText(getTranslation(MESSAGE_PREFIX + FILES));
    analyze.setText(getTranslation(MESSAGE_PREFIX + ANALYZE));
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  private void clearError() {
    error.setVisible(false);
  }

  void view(Dataset dataset) {
    clearError();
    DatasetDialog dialog = dialogFactory.getObject();
    dialog.setDatasetId(dataset.getId());
    dialog.addSavedListener(e -> datasets.refreshDatasets());
    dialog.addDeletedListener(e -> datasets.refreshDatasets());
    dialog.open();
  }

  void viewFiles() {
    List<Dataset> datasets = new ArrayList<>(this.datasets.getSelectedItems());
    boolean error = false;
    if (datasets.isEmpty()) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + DATASETS_REQUIRED));
      error = true;
    } else if (datasets.size() > 1) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + DATASETS_MORE_THAN_ONE));
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
    filesDialog.setDatasetId(dataset.getId());
    filesDialog.open();
  }

  void analyze() {
    List<Dataset> datasets = new ArrayList<>(this.datasets.getSelectedItems());
    boolean error = false;
    if (datasets.isEmpty()) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + DATASETS_REQUIRED));
      error = true;
    }
    this.error.setVisible(error);
    if (!error) {
      DatasetsAnalysisDialog analysisDialog = analysisDialogFactory.getObject();
      analysisDialog
          .setDatasetIds(datasets.stream().map(Dataset::getId).collect(Collectors.toList()));
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
    boolean error = false;
    if (samples.isEmpty()) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + DATASETS_REQUIRED));
      error = true;
    } else if (!sampleService.isMergable(samples)) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + MERGE_ERROR));
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
        this.error.setText(getTranslation(DATASET_PREFIX + NAME_ALREADY_EXISTS, dataset.getName()));
        this.error.setVisible(true);
      } else {
        service.save(dataset);
        this.datasets.refreshDatasets();
        showNotification(getTranslation(MESSAGE_PREFIX + MERGED, dataset.getName()));
      }
    }
  }
}
