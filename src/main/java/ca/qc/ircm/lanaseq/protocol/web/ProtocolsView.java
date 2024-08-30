package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.CREATION_DATE;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.OWNER;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.ErrorNotification;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.google.common.collect.Range;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Protocols view.
 */
@Route(value = ProtocolsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class ProtocolsView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
  public static final String VIEW_NAME = "protocols";
  public static final String ID = "protocols-view";
  public static final String PROTOCOLS = "protocols";
  public static final String EDIT_BUTTON =
      "<vaadin-button class='" + EDIT + "' theme='icon' @click='${edit}'>"
          + "<vaadin-icon icon='vaadin:edit' slot='prefix'></vaadin-icon>" + "</vaadin-button>";
  public static final String HISTORY = "history";
  public static final String PROTOCOLS_REQUIRED = property(PROTOCOLS, "required");
  public static final String PROTOCOLS_MORE_THAN_ONE = property(PROTOCOLS, "moreThanOne");
  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolsView.class);
  private static final String PROTOCOL_PREFIX = messagePrefix(Protocol.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = -2370599174391239721L;
  private static final Logger logger = LoggerFactory.getLogger(ProtocolsView.class);
  protected Grid<Protocol> protocols = new Grid<>();
  protected Column<Protocol> name;
  protected Column<Protocol> date;
  protected Column<Protocol> owner;
  protected Column<Protocol> edit;
  protected TextField nameFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  protected Button add = new Button();
  protected Button history = new Button();
  private WebProtocolFilter filter = new WebProtocolFilter();
  private transient ObjectFactory<ProtocolDialog> dialogFactory;
  private transient ObjectFactory<ProtocolHistoryDialog> historyDialogFactory;
  private transient ProtocolService service;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected ProtocolsView(ObjectFactory<ProtocolDialog> dialogFactory,
      ObjectFactory<ProtocolHistoryDialog> historyDialogFactory, ProtocolService service,
      AuthenticatedUser authenticatedUser) {
    this.dialogFactory = dialogFactory;
    this.historyDialogFactory = historyDialogFactory;
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  @PostConstruct
  void init() {
    logger.debug("protocols view");
    setId(ID);
    setHeightFull();
    VerticalLayout protocolsLayout = new VerticalLayout();
    protocolsLayout.setWidthFull();
    protocolsLayout.setPadding(false);
    protocolsLayout.setSpacing(false);
    protocolsLayout.add(add, protocols);
    protocolsLayout.expand(protocols);
    add(protocolsLayout, history);
    expand(protocolsLayout);
    protocols.setId(PROTOCOLS);
    protocols.setMinHeight("30em");
    name = protocols.addColumn(protocol -> protocol.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(Protocol::getName));
    date = protocols
        .addColumn(new LocalDateTimeRenderer<>(Protocol::getCreationDate,
            () -> DateTimeFormatter.ISO_LOCAL_DATE))
        .setKey(CREATION_DATE).setSortProperty(CREATION_DATE);
    owner = protocols.addColumn(protocol -> protocol.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setComparator(NormalizedComparator.of(p -> p.getOwner().getEmail()));
    edit = protocols
        .addColumn(
            LitRenderer.<Protocol>of(EDIT_BUTTON).withFunction("edit", protocol -> edit(protocol)))
        .setKey(EDIT).setSortable(false).setFlexGrow(0);
    protocols.addItemDoubleClickListener(e -> edit(e.getItem()));
    protocols.addItemClickListener(e -> {
      if (e.isAltKey()) {
        history(e.getItem());
      }
    });
    protocols.addSelectionListener(e -> history.setEnabled(e.getAllSelectedItems().size() == 1));
    protocols.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = protocols.appendHeaderRow();
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
    add.setId(ADD);
    add.addClassName("right");
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> add());
    history.setId(HISTORY);
    history.setIcon(VaadinIcon.ARCHIVE.create());
    history.addClickListener(e -> history());
    history.setEnabled(false);
    history.setVisible(authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER));
    loadProtocols();
  }

  private void loadProtocols() {
    GridListDataView dataView = protocols.setItems(service.all());
    dataView.setFilter(filter);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    String nameHeader = getTranslation(PROTOCOL_PREFIX + NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String dateHeader = getTranslation(PROTOCOL_PREFIX + CREATION_DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = getTranslation(PROTOCOL_PREFIX + OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    String editHeader = getTranslation(CONSTANTS_PREFIX + EDIT);
    edit.setHeader(editHeader).setFooter(editHeader);
    nameFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    ownerFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    add.setText(getTranslation(CONSTANTS_PREFIX + ADD));
    history.setText(getTranslation(MESSAGE_PREFIX + HISTORY));
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  void edit(Protocol protocol) {
    showDialog(protocol);
  }

  private void showDialog(Protocol protocol) {
    ProtocolDialog dialog = dialogFactory.getObject();
    dialog.setProtocolId(protocol.getId());
    dialog.addSavedListener(e -> loadProtocols());
    dialog.addDeletedListener(e -> loadProtocols());
    dialog.open();
  }

  void history() {
    Protocol protocol = protocols.getSelectedItems().stream().findFirst().orElse(null);
    if (protocol == null) {
      new ErrorNotification(getTranslation(MESSAGE_PREFIX + PROTOCOLS_REQUIRED)).open();
    } else {
      history(protocol);
    }
  }

  void history(Protocol protocol) {
    if (authenticatedUser.hasAnyRole(UserRole.MANAGER, UserRole.ADMIN)) {
      ProtocolHistoryDialog historyDialog = historyDialogFactory.getObject();
      historyDialog.setProtocolId(protocol.getId());
      historyDialog.open();
    }
  }

  void add() {
    showDialog(new Protocol());
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    protocols.getDataProvider().refreshAll();
  }

  void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    protocols.getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    protocols.getDataProvider().refreshAll();
  }

  WebProtocolFilter filter() {
    return filter;
  }
}
