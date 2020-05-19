package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.time.format.DateTimeFormatter;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Samples view.
 */
@Route(value = SamplesView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class SamplesView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
  public static final String VIEW_NAME = "samples";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String SAMPLES = "samples";
  private static final long serialVersionUID = -6945706067250351889L;
  protected H2 header = new H2();
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> protocol;
  protected Column<Sample> date;
  protected Column<Sample> owner;
  protected TextField nameFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  protected Button add = new Button();
  @Autowired
  protected SampleDialog dialog;
  @Autowired
  protected ProtocolDialog protocolDialog;
  @Autowired
  private transient SamplesViewPresenter presenter;

  public SamplesView() {
  }

  SamplesView(SamplesViewPresenter presenter, SampleDialog dialog) {
    this.presenter = presenter;
    this.dialog = dialog;
  }

  @PostConstruct
  void init() {
    setId(ID);
    add(header, samples, add);
    header.setId(HEADER);
    samples.setId(SAMPLES);
    name = samples.addColumn(sample -> sample.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(Sample::getName));
    protocol =
        samples.addColumn(sample -> sample.getProtocol().getName(), PROTOCOL).setKey(PROTOCOL)
            .setComparator(NormalizedComparator.of(sample -> sample.getProtocol().getName()));
    date = samples
        .addColumn(new LocalDateTimeRenderer<>(Sample::getDate, DateTimeFormatter.ISO_LOCAL_DATE),
            DATE)
        .setKey(DATE);
    owner = samples.addColumn(sample -> sample.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setComparator(NormalizedComparator.of(p -> p.getOwner().getEmail()));
    samples.addItemDoubleClickListener(e -> {
      if (e.getColumn() == protocol && e.getItem().getProtocol() != null) {
        presenter.view(e.getItem().getProtocol());
      } else {
        presenter.view(e.getItem());
      }
    });
    samples.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = samples.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> presenter.filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(protocol).setComponent(protocolFilter);
    protocolFilter.addValueChangeListener(e -> presenter.filterProtocol(e.getValue()));
    protocolFilter.setValueChangeMode(ValueChangeMode.EAGER);
    protocolFilter.setSizeFull();
    filtersRow.getCell(date).setComponent(dateFilter);
    dateFilter.addValueChangeListener(e -> presenter.filterDate(e.getValue()));
    dateFilter.setSizeFull();
    filtersRow.getCell(owner).setComponent(ownerFilter);
    ownerFilter.addValueChangeListener(e -> presenter.filterOwner(e.getValue()));
    ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
    ownerFilter.setSizeFull();
    add.setId(ADD);
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> presenter.add());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(SamplesView.class, getLocale());
    AppResources sampleResources = new AppResources(Sample.class, getLocale());
    AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    String nameHeader = sampleResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String protocolHeader = sampleResources.message(PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = sampleResources.message(DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = sampleResources.message(OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    nameFilter.setPlaceholder(webResources.message(ALL));
    protocolFilter.setPlaceholder(webResources.message(ALL));
    ownerFilter.setPlaceholder(webResources.message(ALL));
    add.setText(webResources.message(ADD));
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(SamplesView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}