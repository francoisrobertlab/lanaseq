package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.KEYWORDS;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.text.Strings.normalizedCollator;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import com.google.common.collect.Range;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset grid.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetGrid extends Grid<Dataset> implements LocaleChangeObserver {

  public static final String ID = "datasets-grid";
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = -3052158575710045415L;
  protected Column<Dataset> name;
  protected Column<Dataset> keywords;
  protected Column<Dataset> protocol;
  protected Column<Dataset> date;
  protected Column<Dataset> owner;
  protected TextField nameFilter = new TextField();
  protected TextField keywordsFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  private final WebDatasetFilter filter = new WebDatasetFilter();
  private final transient DatasetService service;
  private final transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected DatasetGrid(DatasetService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  @PostConstruct
  void init() {
    setId(ID);
    name = addColumn(Dataset::getName, NAME).setKey(NAME).setSortProperty(NAME)
        .setComparator(Comparator.comparing(Dataset::getName, normalizedCollator())).setFlexGrow(3);
    keywords = addColumn(dataset -> String.join(", ", dataset.getKeywords()), KEYWORDS).setKey(
        KEYWORDS).setSortable(false).setFlexGrow(1);
    protocol = addColumn(dataset -> protocol(dataset).getName(), PROTOCOL).setKey(PROTOCOL)
        .setSortable(false).setFlexGrow(1);
    date = addColumn(
        new LocalDateRenderer<>(Dataset::getDate, () -> DateTimeFormatter.ISO_LOCAL_DATE)).setKey(
        DATE).setSortProperty(DATE).setFlexGrow(1);
    owner = addColumn(dataset -> dataset.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setSortProperty(OWNER + "." + EMAIL)
        .setComparator(Comparator.comparing(e -> e.getOwner().getEmail(), normalizedCollator()))
        .setFlexGrow(1);
    sort(GridSortOrder.desc(date).build());
    appendHeaderRow(); // Headers.
    HeaderRow filtersRow = appendHeaderRow();
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
    loadDataset();
    if (!authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      authenticatedUser.getUser().ifPresent(user -> ownerFilter.setValue(user.getEmail()));
    }
  }

  private Protocol protocol(Dataset dataset) {
    return !dataset.getSamples().isEmpty() ? dataset.getSamples().get(0).getProtocol()
        : new Protocol();
  }

  private void loadDataset() {
    CallbackDataProvider.FetchCallback<Dataset, Void> fetchCallback = query -> {
      filter.sort = VaadinSpringDataHelpers.toSpringDataSort(query);
      filter.page = query.getOffset() / getPageSize();
      filter.size = query.getLimit();
      return service.all(filter).stream();
    };
    setItems(fetchCallback);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    String nameHeader = getTranslation(DATASET_PREFIX + NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String keywordsHeader = getTranslation(DATASET_PREFIX + KEYWORDS);
    keywords.setHeader(keywordsHeader).setFooter(keywordsHeader);
    String protocolHeader = getTranslation(SAMPLE_PREFIX + PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = getTranslation(DATASET_PREFIX + DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = getTranslation(DATASET_PREFIX + OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    nameFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    keywordsFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    protocolFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    ownerFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
  }

  public void refreshDatasets() {
    getDataProvider().refreshAll();
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    getDataProvider().refreshAll();
  }

  void filterKeywords(String value) {
    filter.keywordsContains = value.isEmpty() ? null : value;
    getDataProvider().refreshAll();
  }

  void filterProtocol(String value) {
    filter.protocolContains = value.isEmpty() ? null : value;
    getDataProvider().refreshAll();
  }

  void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    getDataProvider().refreshAll();
  }

  WebDatasetFilter filter() {
    return filter;
  }
}
