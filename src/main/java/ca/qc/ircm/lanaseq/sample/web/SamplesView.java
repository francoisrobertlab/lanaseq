package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TAGS;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.VaadinSort;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Samples view.
 */
@Route(value = SamplesView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class SamplesView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent {
  public static final String VIEW_NAME = "samples";
  public static final String ID = "samples-view";
  public static final String SAMPLES = "samples";
  public static final String MERGE = "merge";
  public static final String FILES = "files";
  public static final String ANALYZE = "analyze";
  public static final String MERGED = "merged";
  public static final String SAMPLES_REQUIRED = property(SAMPLES, "required");
  public static final String SAMPLES_MORE_THAN_ONE = property(SAMPLES, "moreThanOne");
  public static final String SAMPLES_CANNOT_WRITE = property(SAMPLES, "cannotWrite");
  public static final String MERGE_ERROR = property(MERGE, "error");
  public static final String EDIT_BUTTON =
      "<vaadin-button class='" + EDIT + "' theme='icon' @click='${edit}'>"
          + "<vaadin-icon icon='vaadin:edit' slot='prefix'></vaadin-icon>" + "</vaadin-button>";
  private static final String MESSAGE_PREFIX = messagePrefix(SamplesView.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = -6945706067250351889L;
  private static final Logger logger = LoggerFactory.getLogger(SamplesView.class);
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> tags;
  protected Column<Sample> protocol;
  protected Column<Sample> date;
  protected Column<Sample> owner;
  protected Column<Sample> edit;
  protected TextField nameFilter = new TextField();
  protected TextField tagsFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  protected Div error = new Div();
  protected Button add = new Button();
  protected Button merge = new Button();
  protected Button files = new Button();
  protected Button analyze = new Button();
  private WebSampleFilter filter = new WebSampleFilter();
  private transient ObjectFactory<SampleDialog> dialogFactory;
  private transient ObjectFactory<SampleFilesDialog> filesDialogFactory;
  private transient ObjectFactory<SamplesAnalysisDialog> analysisDialogFactory;
  private transient SampleService service;
  private transient DatasetService datasetService;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected SamplesView(ObjectFactory<SampleDialog> dialogFactory,
      ObjectFactory<SampleFilesDialog> filesDialogFactory,
      ObjectFactory<SamplesAnalysisDialog> analysisDialogFactory, SampleService service,
      DatasetService datasetService, AuthenticatedUser authenticatedUser) {
    this.dialogFactory = dialogFactory;
    this.filesDialogFactory = filesDialogFactory;
    this.analysisDialogFactory = analysisDialogFactory;
    this.service = service;
    this.datasetService = datasetService;
    this.authenticatedUser = authenticatedUser;
  }

  @PostConstruct
  void init() {
    logger.debug("samples view");
    setId(ID);
    setHeightFull();
    add(samples, error, new HorizontalLayout(add, merge, files, analyze));
    samples.setMinHeight("30em");
    expand(samples);
    samples.setId(SAMPLES);
    samples.setSelectionMode(SelectionMode.MULTI);
    name = samples.addColumn(sample -> sample.getName(), NAME).setKey(NAME).setSortProperty(NAME)
        .setComparator(NormalizedComparator.of(Sample::getName)).setFlexGrow(2);
    tags = samples
        .addColumn(sample -> sample.getTags().stream().collect(Collectors.joining(", ")), TAGS)
        .setKey(TAGS).setSortable(false).setFlexGrow(1);
    protocol = samples.addColumn(sample -> sample.getProtocol().getName(), PROTOCOL)
        .setKey(PROTOCOL).setSortProperty(PROTOCOL + "." + NAME)
        .setComparator(NormalizedComparator.of(sample -> sample.getProtocol().getName()))
        .setFlexGrow(1);
    date = samples
        .addColumn(new LocalDateRenderer<>(Sample::getDate, () -> DateTimeFormatter.ISO_LOCAL_DATE))
        .setKey(DATE).setSortProperty(DATE).setComparator(Comparator.comparing(Sample::getDate))
        .setFlexGrow(1);
    owner = samples.addColumn(sample -> sample.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setSortProperty(OWNER + "." + EMAIL)
        .setComparator(NormalizedComparator.of(p -> p.getOwner().getEmail())).setFlexGrow(1);
    edit = samples
        .addColumn(LitRenderer.<Sample>of(EDIT_BUTTON).withFunction("edit", sample -> view(sample)))
        .setKey(EDIT).setSortable(false).setFlexGrow(0);
    samples.sort(GridSortOrder.desc(date).build());
    samples.addItemDoubleClickListener(e -> view(e.getItem()));
    samples.addItemClickListener(e -> {
      if (e.isCtrlKey() || e.isMetaKey()) {
        viewFiles(e.getItem());
      }
    });
    samples.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = samples.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(tags).setComponent(tagsFilter);
    tagsFilter.addValueChangeListener(e -> filterTags(e.getValue()));
    tagsFilter.setValueChangeMode(ValueChangeMode.EAGER);
    tagsFilter.setSizeFull();
    filtersRow.getCell(protocol).setComponent(protocolFilter);
    protocolFilter.addValueChangeListener(e -> filterProtocol(e.getValue()));
    protocolFilter.setValueChangeMode(ValueChangeMode.EAGER);
    protocolFilter.setSizeFull();
    filtersRow.getCell(date).setComponent(dateFilter);
    dateFilter.addValueChangeListener(e -> filterDate(e.getValue()));
    dateFilter.setSizeFull();
    dateFilter.addThemeVariants(CustomFieldVariant.LUMO_SMALL);
    filtersRow.getCell(owner).setComponent(ownerFilter);
    ownerFilter.addValueChangeListener(e -> filterOwner(e.getValue()));
    ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
    ownerFilter.setSizeFull();
    error.setId(ERROR_TEXT);
    error.addClassName(ERROR_TEXT);
    error.setVisible(false);
    add.setId(ADD);
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> add());
    merge.setId(MERGE);
    merge.setIcon(VaadinIcon.CONNECT.create());
    merge.addClickListener(e -> merge());
    files.setId(FILES);
    files.setIcon(VaadinIcon.FILE_O.create());
    files.addClickListener(e -> viewFiles());
    analyze.setId(ANALYZE);
    analyze.addClickListener(e -> analyze());
    if (!authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      authenticatedUser.getUser().ifPresent(user -> ownerFilter.setValue(user.getEmail()));
    }
    loadSamples();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    String nameHeader = getTranslation(SAMPLE_PREFIX + NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String tagsHeader = getTranslation(SAMPLE_PREFIX + TAGS);
    tags.setHeader(tagsHeader).setFooter(tagsHeader);
    String protocolHeader = getTranslation(SAMPLE_PREFIX + PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = getTranslation(SAMPLE_PREFIX + DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = getTranslation(SAMPLE_PREFIX + OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    String editHeader = getTranslation(CONSTANTS_PREFIX + EDIT);
    edit.setHeader(editHeader).setFooter(editHeader);
    nameFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    tagsFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    protocolFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    ownerFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    add.setText(getTranslation(CONSTANTS_PREFIX + ADD));
    merge.setText(getTranslation(MESSAGE_PREFIX + MERGE));
    files.setText(getTranslation(MESSAGE_PREFIX + FILES));
    analyze.setText(getTranslation(MESSAGE_PREFIX + ANALYZE));
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  private void loadSamples() {
    CallbackDataProvider.FetchCallback<Sample, Void> fetchCallback = query -> {
      filter.sort = VaadinSort.springDataSort(query.getSortOrders());
      filter.page = query.getOffset() / samples.getPageSize();
      filter.size = query.getLimit();
      return service.all(filter).stream();
    };
    samples.setItems(fetchCallback);
  }

  void view(Sample sample) {
    showDialog(sample);
  }

  private void showDialog(Sample sample) {
    SampleDialog dialog = dialogFactory.getObject();
    dialog.setSampleId(sample != null ? sample.getId() : null);
    dialog.addSavedListener(e -> samples.getDataProvider().refreshAll());
    dialog.addDeletedListener(e -> samples.getDataProvider().refreshAll());
    dialog.open();
  }

  void viewFiles(Sample sample) {
    SampleFilesDialog filesDialog = filesDialogFactory.getObject();
    filesDialog.setSampleId(sample.getId());
    filesDialog.open();
  }

  void viewFiles() {
    List<Sample> samples = new ArrayList<>(this.samples.getSelectedItems());
    boolean error = false;
    if (samples.isEmpty()) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED));
      error = true;
    } else if (samples.size() > 1) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + SAMPLES_MORE_THAN_ONE));
      error = true;
    }
    this.error.setVisible(error);
    if (!error) {
      Sample sample = samples.iterator().next();
      viewFiles(sample);
    }
  }

  void analyze() {
    List<Sample> samples = new ArrayList<>(this.samples.getSelectedItems());
    boolean error = false;
    if (samples.isEmpty()) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED));
      error = true;
    }
    this.error.setVisible(error);
    if (!error) {
      SamplesAnalysisDialog analysisDialog = analysisDialogFactory.getObject();
      analysisDialog.setSampleIds(samples.stream().map(Sample::getId).collect(Collectors.toList()));
      analysisDialog.open();
    }
  }

  void add() {
    showDialog(null);
  }

  void merge() {
    List<Sample> samples = this.samples.getSelectedItems().stream()
        .sorted(Comparator.comparing(Sample::getId)).collect(Collectors.toList());
    Set<String> tags =
        samples.stream().flatMap(sample -> sample.getTags().stream()).collect(Collectors.toSet());
    boolean error = false;
    if (samples.isEmpty()) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED));
      error = true;
    } else if (!service.isMergable(samples)) {
      this.error.setText(getTranslation(MESSAGE_PREFIX + MERGE_ERROR));
      error = true;
    }
    this.error.setVisible(error);
    if (!error) {
      Dataset dataset = new Dataset();
      dataset.setSamples(samples);
      dataset.setTags(tags);
      dataset.setDate(samples.get(0).getDate());
      dataset.generateName();
      if (datasetService.exists(dataset.getName())) {
        this.error.setText(getTranslation(DATASET_PREFIX + NAME_ALREADY_EXISTS, dataset.getName()));
        this.error.setVisible(true);
      } else {
        datasetService.save(dataset);
        showNotification(getTranslation(MESSAGE_PREFIX + MERGED, dataset.getName()));
      }
    }
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    samples.getDataProvider().refreshAll();
  }

  void filterTags(String value) {
    filter.tagsContains = value.isEmpty() ? null : value;
    samples.getDataProvider().refreshAll();
  }

  void filterProtocol(String value) {
    filter.protocolContains = value.isEmpty() ? null : value;
    samples.getDataProvider().refreshAll();
  }

  void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    samples.getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    samples.getDataProvider().refreshAll();
  }

  WebSampleFilter filter() {
    return filter;
  }
}
