package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.DATE;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.OWNER;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
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
 * Protocols view.
 */
@Route(value = ProtocolsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class ProtocolsView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
  public static final String VIEW_NAME = "protocols";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String PROTOCOLS = "protocols";
  private static final long serialVersionUID = -2370599174391239721L;
  protected H2 header = new H2();
  protected Grid<Protocol> protocols = new Grid<>();
  protected Column<Protocol> name;
  protected Column<Protocol> date;
  protected Column<Protocol> owner;
  protected TextField nameFilter = new TextField();
  protected TextField ownerFilter = new TextField();
  protected Button add = new Button();
  @Autowired
  protected ProtocolDialog dialog;
  @Autowired
  private transient ProtocolsViewPresenter presenter;

  public ProtocolsView() {
  }

  ProtocolsView(ProtocolsViewPresenter presenter, ProtocolDialog dialog) {
    this.presenter = presenter;
    this.dialog = dialog;
  }

  @PostConstruct
  void init() {
    setId(ID);
    add(header, protocols, add);
    header.setId(HEADER);
    protocols.setId(PROTOCOLS);
    name = protocols.addColumn(protocol -> protocol.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(Protocol::getName));
    date = protocols
        .addColumn(new LocalDateTimeRenderer<>(Protocol::getDate, DateTimeFormatter.ISO_LOCAL_DATE),
            DATE)
        .setKey(DATE);
    owner = protocols.addColumn(protocol -> protocol.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setComparator(NormalizedComparator.of(p -> p.getOwner().getEmail()));
    protocols.addItemDoubleClickListener(e -> presenter.view(e.getItem()));
    protocols.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = protocols.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> presenter.filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
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
    AppResources resources = new AppResources(ProtocolsView.class, getLocale());
    AppResources protocolResources = new AppResources(Protocol.class, getLocale());
    AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    String nameHeader = protocolResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String dateHeader = protocolResources.message(DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = protocolResources.message(OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    nameFilter.setPlaceholder(webResources.message(ALL));
    ownerFilter.setPlaceholder(webResources.message(ALL));
    add.setText(webResources.message(ADD));
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(ProtocolsView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
