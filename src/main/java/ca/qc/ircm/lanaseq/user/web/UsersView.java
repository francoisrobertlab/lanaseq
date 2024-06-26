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

package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.ACTIVE;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.MainView;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Users view.
 */
@Route(value = UsersView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ MANAGER, ADMIN })
public class UsersView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent, UrlComponent {
  public static final String VIEW_NAME = "users";
  public static final String ID = "users-view";
  public static final String HEADER = "header";
  public static final String USERS = "users";
  public static final String USERS_REQUIRED = property(USERS, REQUIRED);
  public static final String SWITCH_USER = "switchUser";
  public static final String SWITCH_FAILED = "switchFailed";
  private static final String MESSAGE_PREFIX = messagePrefix(UsersView.class);
  private static final String USER_PREFIX = messagePrefix(User.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = 2568742367790329628L;
  private static final Logger logger = LoggerFactory.getLogger(UsersView.class);
  protected H2 header = new H2();
  protected Grid<User> users = new Grid<>();
  protected Column<User> email;
  protected Column<User> name;
  protected Column<User> active;
  protected Column<User> edit;
  protected TextField emailFilter = new TextField();
  protected TextField nameFilter = new TextField();
  protected Select<Optional<Boolean>> activeFilter = new Select<>();
  protected Div error = new Div();
  protected Button add = new Button();
  protected Button switchUser = new Button();
  private WebUserFilter filter = new WebUserFilter();
  private Map<User, Button> actives = new HashMap<>();
  private transient ObjectFactory<UserDialog> dialogFactory;
  private transient UserService service;
  private transient SwitchUserService switchUserService;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected UsersView(UserService service, SwitchUserService switchUserService,
      AuthenticatedUser authenticatedUser, ObjectFactory<UserDialog> dialogFactory) {
    this.service = service;
    this.switchUserService = switchUserService;
    this.authenticatedUser = authenticatedUser;
    this.dialogFactory = dialogFactory;
  }

  @SuppressWarnings("unchecked")
  @PostConstruct
  void init() {
    logger.debug("users view");
    setId(ID);
    setHeightFull();
    add(header, users, error, new HorizontalLayout(add, switchUser));
    expand(users);
    header.setId(HEADER);
    users.setId(USERS);
    users.addItemDoubleClickListener(e -> view(e.getItem()));
    email = users.addColumn(user -> user.getEmail(), EMAIL).setKey(EMAIL)
        .setComparator(NormalizedComparator.of(User::getEmail));
    name = users.addColumn(user -> user.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(User::getName));
    active = users.addColumn(new ComponentRenderer<>(user -> activeButton(user))).setKey(ACTIVE)
        .setSortProperty(ACTIVE)
        .setComparator((u1, u2) -> Boolean.compare(u1.isActive(), u2.isActive()));
    active.setVisible(authenticatedUser.hasAnyRole(ADMIN, MANAGER));
    edit = users.addColumn(new ComponentRenderer<>(user -> editButton(user))).setKey(EDIT)
        .setSortable(false).setFlexGrow(0);
    users.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = users.appendHeaderRow();
    filtersRow.getCell(email).setComponent(emailFilter);
    emailFilter.addValueChangeListener(e -> filterEmail(e.getValue()));
    emailFilter.setValueChangeMode(ValueChangeMode.EAGER);
    emailFilter.setSizeFull();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(active).setComponent(activeFilter);
    activeFilter.setItems(Optional.empty(), Optional.of(false), Optional.of(true));
    activeFilter.addValueChangeListener(e -> filterActive(e.getValue().orElse(null)));
    activeFilter.setSizeFull();
    error.setId(ERROR_TEXT);
    error.setVisible(false);
    add.setId(ADD);
    add.setVisible(authenticatedUser.hasAnyRole(ADMIN, MANAGER));
    add.addClickListener(e -> add());
    switchUser.setId(SWITCH_USER);
    switchUser.setVisible(authenticatedUser.hasRole(ADMIN));
    switchUser.addClickListener(e -> switchUser());
    loadUsers();
  }

  private Button activeButton(User user) {
    Button button = new Button();
    button.addClassName(ACTIVE);
    actives.put(user, button);
    updateActiveButton(button, user);
    button.addClickListener(e -> {
      toggleActive(user);
      updateActiveButton(button, user);
    });
    return button;
  }

  private Button editButton(User user) {
    Button button = new Button();
    button.addClassName(EDIT);
    button.addThemeVariants(ButtonVariant.LUMO_ICON);
    button.setIcon(VaadinIcon.EDIT.create());
    button.addClickListener(e -> view(user));
    return button;
  }

  private void updateActiveButton(Button button, User user) {
    button.setIcon(user.isActive() ? VaadinIcon.EYE.create() : VaadinIcon.EYE_SLASH.create());
    button.setText(getTranslation(USER_PREFIX + property(ACTIVE, user.isActive())));
    button
        .addThemeVariants(user.isActive() ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_ERROR);
  }

  private void loadUsers() {
    users.setItems(service.all());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    header.setText(getTranslation(MESSAGE_PREFIX + HEADER));
    String emailHeader = getTranslation(USER_PREFIX + EMAIL);
    email.setHeader(emailHeader).setFooter(emailHeader);
    String nameHeader = getTranslation(USER_PREFIX + NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String activeHeader = getTranslation(USER_PREFIX + ACTIVE);
    active.setHeader(activeHeader).setFooter(activeHeader);
    String editHeader = getTranslation(CONSTANTS_PREFIX + EDIT);
    edit.setHeader(editHeader).setFooter(editHeader);
    emailFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    nameFilter.setPlaceholder(getTranslation(CONSTANTS_PREFIX + ALL));
    activeFilter.setItemLabelGenerator(
        value -> value.map(bv -> getTranslation(USER_PREFIX + property(ACTIVE, bv)))
            .orElse(getTranslation(CONSTANTS_PREFIX + ALL)));
    actives.entrySet().stream().forEach(entry -> entry.getValue()
        .setText(getTranslation(USER_PREFIX + property(ACTIVE, entry.getKey().isActive()))));
    add.setText(getTranslation(CONSTANTS_PREFIX + ADD));
    add.setIcon(VaadinIcon.PLUS.create());
    switchUser.setText(getTranslation(MESSAGE_PREFIX + SWITCH_USER));
    switchUser.setIcon(VaadinIcon.BUG.create());
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    activeFilter.setValue(Optional.empty());
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  void filterEmail(String value) {
    filter.emailContains = value.isEmpty() ? null : value;
    users.getDataProvider().refreshAll();
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    users.getDataProvider().refreshAll();
  }

  void filterActive(Boolean value) {
    filter.active = value;
    users.getDataProvider().refreshAll();
  }

  private void clearError() {
    error.setVisible(false);
  }

  void view(User user) {
    clearError();
    showDialog(user.getId());
  }

  private void showDialog(Long id) {
    UserDialog dialog = dialogFactory.getObject();
    dialog.setUserId(id);
    dialog.addSavedListener(e -> loadUsers());
    dialog.open();
  }

  void toggleActive(User user) {
    clearError();
    user.setActive(!user.isActive());
    service.save(user, null);
  }

  void switchUser() {
    clearError();
    User user = users.getSelectedItems().stream().findFirst().orElse(null);
    if (user == null) {
      error.setText(getTranslation(MESSAGE_PREFIX + USERS_REQUIRED));
      error.setVisible(true);
    } else {
      switchUserService.switchUser(user, VaadinServletRequest.getCurrent());
      UI.getCurrent().getPage().setLocation(getUrl(MainView.VIEW_NAME));
    }
  }

  void add() {
    showDialog(null);
  }

  WebUserFilter filter() {
    return filter;
  }
}
