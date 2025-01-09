package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.KEYWORDS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
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
import ca.qc.ircm.lanaseq.web.ErrorNotification;
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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
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
  private static final String MESSAGE_PREFIX = messagePrefix(SamplesView.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = -6945706067250351889L;
  private static final Logger logger = LoggerFactory.getLogger(SamplesView.class);
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> keywords;
  protected Column<Sample> protocol;
  protected Column<Sample> date;
  protected Column<Sample> owner;
  protected TextField nameFilter = new TextField();
  protected TextField keywordsFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  protected Button add = new Button();
  protected Button edit = new Button();
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
    VerticalLayout samplesLayout = new VerticalLayout();
    samplesLayout.setWidthFull();
    samplesLayout.setPadding(false);
    samplesLayout.setSpacing(false);
    samplesLayout.add(add, samples);
    samplesLayout.expand(samples);
    add(samplesLayout, new HorizontalLayout(edit, merge, files, analyze));
    expand(samplesLayout);
    samples.setId(SAMPLES);
    samples.setMinHeight("30em");
    samples.setSelectionMode(SelectionMode.MULTI);
    name = samples.addColumn(sample -> sample.getName(), NAME).setKey(NAME).setSortProperty(NAME)
        .setComparator(NormalizedComparator.of(Sample::getName)).setFlexGrow(2);
    keywords =
        samples.addColumn(sample -> sample.getKeywords().stream().collect(Collectors.joining(", ")),
            KEYWORDS).setKey(KEYWORDS).setSortable(false).setFlexGrow(1);
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
    samples.sort(GridSortOrder.desc(date).build());
    samples.addItemDoubleClickListener(e -> edit(e.getItem()));
    samples.addItemClickListener(e -> {
      if (e.isCtrlKey() || e.isMetaKey()) {
        viewFiles(e.getItem());
      }
    });
    samples.addSelectionListener(e -> {
      edit.setEnabled(e.getAllSelectedItems().size() == 1);
      merge.setEnabled(!e.getAllSelectedItems().isEmpty());
      files.setEnabled(e.getAllSelectedItems().size() == 1);
      analyze.setEnabled(!e.getAllSelectedItems().isEmpty());
    });
    samples.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = samples.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(keywords).setComponent(keywordsFilter);
    keywordsFilter.addValueChangeListener(e -> filterKeywords(e.getValue()));
    keywordsFilter.setValueChangeMode(ValueChangeMode.EAGER);
    keywordsFilter.setSizeFull();
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
    add.setId(ADD);
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> add());
    edit.setId(EDIT);
    edit.setEnabled(false);
    edit.setIcon(VaadinIcon.EDIT.create());
    edit.addClickListener(e -> edit());
    merge.setId(MERGE);
    merge.setEnabled(false);
    merge.setIcon(VaadinIcon.CONNECT.create());
    merge.addClickListener(e -> merge());
    files.setId(FILES);
    files.setEnabled(false);
    files.setIcon(VaadinIcon.FILE_O.create());
    files.addClickListener(e -> viewFiles());
    analyze.setId(ANALYZE);
    analyze.setEnabled(false);
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
    String keywordsHeader = getTranslation(SAMPLE_PREFIX + KEYWORDS);
    keywords.setHeader(keywordsHeader).setFooter(keywordsHeader);
    String protocolHeader = getTranslation(SAMPLE_PREFIX + PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = getTranslation(SAMPLE_PREFIX + DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = getTranslation(SAMPLE_PREFIX + OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    nameFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    keywordsFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    protocolFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    ownerFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    add.setText(getTranslation(MESSAGE_PREFIX + ADD));
    edit.setText(getTranslation(CONSTANTS_PREFIX + EDIT));
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

  void edit() {
    Set<Sample> samples = this.samples.getSelectedItems();
    if (samples.isEmpty()) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED)).open();
    } else if (samples.size() > 1) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + SAMPLES_MORE_THAN_ONE)).open();
    } else {
      Sample sample = samples.iterator().next();
      edit(sample);
    }
  }

  void edit(Sample sample) {
    showDialog(sample.getId());
  }

  private void showDialog(long sampleId) {
    SampleDialog dialog = dialogFactory.getObject();
    dialog.setSampleId(sampleId);
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
    Set<Sample> samples = this.samples.getSelectedItems();
    if (samples.isEmpty()) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED)).open();
    } else if (samples.size() > 1) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + SAMPLES_MORE_THAN_ONE)).open();
    } else {
      Sample sample = samples.iterator().next();
      viewFiles(sample);
    }
  }

  void analyze() {
    Set<Sample> samples = this.samples.getSelectedItems();
    if (samples.isEmpty()) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED)).open();
    } else {
      SamplesAnalysisDialog analysisDialog = analysisDialogFactory.getObject();
      analysisDialog.setSampleIds(samples.stream().map(Sample::getId).collect(Collectors.toList()));
      analysisDialog.open();
    }
  }

  void add() {
    showDialog(0);
  }

  void merge() {
    List<Sample> samples = this.samples.getSelectedItems().stream()
        .sorted(Comparator.comparing(Sample::getId)).collect(Collectors.toList());
    Set<String> keywords = samples.stream().flatMap(sample -> sample.getKeywords().stream())
        .collect(Collectors.toSet());
    if (samples.isEmpty()) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED)).open();
    } else if (!service.isMergable(samples)) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + MERGE_ERROR)).open();
    } else {
      Dataset dataset = new Dataset();
      dataset.setSamples(samples);
      dataset.setKeywords(keywords);
      dataset.setDate(samples.get(0).getDate());
      dataset.generateName();
      if (datasetService.exists(dataset.getName())) {
        new ErrorNotification(
            getTranslation(DATASET_PREFIX + NAME_ALREADY_EXISTS, dataset.getName())).open();
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

  void filterKeywords(String value) {
    filter.keywordsContains = value.isEmpty() ? null : value;
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
