package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.OWNER;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.EditEvent;
import ca.qc.ircm.lanaseq.web.VaadinSort;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
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
  public static final String EDIT_BUTTON =
      "<vaadin-button class='" + EDIT + "' theme='icon' @click='${edit}'>"
          + "<vaadin-icon icon='vaadin:edit' slot='prefix'></vaadin-icon>" + "</vaadin-button>";
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = -3052158575710045415L;
  protected Column<Dataset> name;
  protected Column<Dataset> tags;
  protected Column<Dataset> protocol;
  protected Column<Dataset> date;
  protected Column<Dataset> owner;
  protected Column<Dataset> edit;
  protected TextField nameFilter = new TextField();
  protected TextField tagsFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  private WebDatasetFilter filter = new WebDatasetFilter();
  private transient DatasetService service;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected DatasetGrid(DatasetService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  @PostConstruct
  void init() {
    setId(ID);
    name = addColumn(dataset -> dataset.getName(), NAME).setKey(NAME).setSortProperty(NAME)
        .setComparator(NormalizedComparator.of(Dataset::getName)).setFlexGrow(3);
    tags = addColumn(dataset -> dataset.getTags().stream().collect(Collectors.joining(", ")), TAGS)
        .setKey(TAGS).setSortable(false).setFlexGrow(1);
    protocol = addColumn(dataset -> protocol(dataset).getName(), PROTOCOL).setKey(PROTOCOL)
        .setSortable(false).setFlexGrow(1);
    date =
        addColumn(new LocalDateRenderer<>(Dataset::getDate, () -> DateTimeFormatter.ISO_LOCAL_DATE))
            .setKey(DATE).setSortProperty(DATE).setFlexGrow(1);
    owner = addColumn(dataset -> dataset.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setSortProperty(OWNER + "." + EMAIL)
        .setComparator(NormalizedComparator.of(e -> e.getOwner().getEmail())).setFlexGrow(1);
    edit = addColumn(LitRenderer.<Dataset>of(EDIT_BUTTON).withFunction("edit",
        dataset -> fireEvent(new EditEvent(this, false, dataset)))).setKey(EDIT).setSortable(false)
            .setFlexGrow(0);
    edit.setVisible(false);
    sort(GridSortOrder.desc(date).build());
    appendHeaderRow(); // Headers.
    HeaderRow filtersRow = appendHeaderRow();
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
    loadDataset();
    if (!authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      authenticatedUser.getUser().ifPresent(user -> ownerFilter.setValue(user.getEmail()));
    }
  }

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  private void loadDataset() {
    CallbackDataProvider.FetchCallback<Dataset, Void> fetchCallback = query -> {
      filter.sort = VaadinSort.springDataSort(query.getSortOrders());
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
    String tagsHeader = getTranslation(DATASET_PREFIX + TAGS);
    tags.setHeader(tagsHeader).setFooter(tagsHeader);
    String protocolHeader = getTranslation(SAMPLE_PREFIX + PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = getTranslation(DATASET_PREFIX + DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = getTranslation(DATASET_PREFIX + OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    String editHeader = getTranslation(CONSTANTS_PREFIX + EDIT);
    edit.setHeader(editHeader).setFooter(editHeader);
    nameFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    tagsFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    protocolFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    ownerFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
  }

  /**
   * Adds listener to be informed when a dataset is to be edited.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addEditListener(ComponentEventListener<EditEvent<DatasetGrid, Dataset>> listener) {
    edit.setVisible(true);
    Registration hideEdit = Registration.once(() -> {
      if (!this.getEventBus().hasListener(EditEvent.class)) {
        edit.setVisible(false);
      }
    });
    return Registration.combine(addListener((Class) EditEvent.class, listener), hideEdit);
  }

  public void refreshDatasets() {
    getDataProvider().refreshAll();
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    getDataProvider().refreshAll();
  }

  void filterTags(String value) {
    filter.tagsContains = value.isEmpty() ? null : value;
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
