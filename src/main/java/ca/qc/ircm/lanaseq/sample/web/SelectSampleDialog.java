package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.SelectedEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Select sample dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectSampleDialog extends Dialog implements LocaleChangeObserver {
  public static final String ID = "select-sample-dialog";
  public static final String SAMPLES = "samples";
  private static final Logger logger = LoggerFactory.getLogger(SelectSampleDialog.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = -1701490833972618304L;
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> date;
  protected Column<Sample> owner;
  protected TextField nameFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  private WebSampleFilter filter = new WebSampleFilter();
  private transient SampleService service;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  SelectSampleDialog(SampleService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    setWidth("1280px");
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(samples);
    layout.setSizeFull();
    layout.expand(samples);
    samples.setId(id(SAMPLES));
    name = samples.addColumn(Sample::getName, NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(Sample::getName));
    date = samples
        .addColumn(new LocalDateRenderer<>(Sample::getDate, () -> DateTimeFormatter.ISO_LOCAL_DATE))
        .setKey(DATE).setSortProperty(DATE).setComparator(Comparator.comparing(Sample::getDate));
    owner = samples.addColumn(sample -> sample.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setComparator(NormalizedComparator.of(p -> p.getOwner().getEmail()));
    samples.addItemDoubleClickListener(e -> select(e.getItem()));
    samples.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = samples.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(date).setComponent(dateFilter);
    dateFilter.addValueChangeListener(e -> filterDate(e.getValue()));
    dateFilter.setSizeFull();
    dateFilter.addThemeVariants(CustomFieldVariant.LUMO_SMALL);
    filtersRow.getCell(owner).setComponent(ownerFilter);
    ownerFilter.addValueChangeListener(e -> filterOwner(e.getValue()));
    ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
    ownerFilter.setSizeFull();
    if (!authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      authenticatedUser.getUser().ifPresent(user -> ownerFilter.setValue(user.getEmail()));
    }
    loadSamples();
  }

  private void loadSamples() {
    GridListDataView<Sample> dataView = samples.setItems(service.all());
    dataView.setFilter(filter::test);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    logger.debug("localeChange called with locale {}", event.getLocale());
    String nameHeader = getTranslation(SAMPLE_PREFIX + NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String dateHeader = getTranslation(SAMPLE_PREFIX + DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = getTranslation(SAMPLE_PREFIX + OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    nameFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    ownerFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
  }

  /**
   * Adds listener to be informed when a sample was selected.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration addSelectedListener(
      ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> listener) {
    return addListener((Class) SelectedEvent.class, listener);
  }

  void fireSelectedEvent(Sample sample) {
    fireEvent(new SelectedEvent<>(this, true, sample));
  }

  void select(Sample sample) {
    logger.debug("selected sample {}", sample);
    fireSelectedEvent(sample);
    close();
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
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
