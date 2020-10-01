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
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.user.UserProperties.ACTIVE;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.AttachEvent;
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
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Users view.
 */
@Route(value = UsersView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ MANAGER, ADMIN })
public class UsersView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle,
    AfterNavigationObserver, NotificationComponent {
  public static final String VIEW_NAME = "users";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String USERS = "users";
  public static final String USERS_REQUIRED = property(USERS, REQUIRED);
  public static final String SWITCH_USER = "switchUser";
  public static final String SWITCH_FAILED = "switchFailed";
  private static final long serialVersionUID = 2568742367790329628L;
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(UsersView.class);
  protected H2 header = new H2();
  protected Grid<User> users = new Grid<>();
  protected Column<User> email;
  protected Column<User> name;
  protected Column<User> active;
  protected TextField emailFilter = new TextField();
  protected TextField nameFilter = new TextField();
  protected Select<Optional<Boolean>> activeFilter = new Select<>();
  protected Div error = new Div();
  protected Button add = new Button();
  protected Button switchUser = new Button();
  private Map<User, Button> actives = new HashMap<>();
  @Autowired
  protected UserDialog userDialog;
  @Autowired
  private transient UsersViewPresenter presenter;

  public UsersView() {
  }

  protected UsersView(UsersViewPresenter presenter, UserDialog userDialog) {
    this.presenter = presenter;
    this.userDialog = userDialog;
  }

  @SuppressWarnings("unchecked")
  @PostConstruct
  void init() {
    logger.debug("users view");
    setId(ID);
    add(header, users, error, new HorizontalLayout(add, switchUser), userDialog);
    header.setId(HEADER);
    users.setId(USERS);
    users.addItemDoubleClickListener(e -> presenter.view(e.getItem()));
    email = users.addColumn(user -> user.getEmail(), EMAIL).setKey(EMAIL)
        .setComparator(NormalizedComparator.of(User::getEmail));
    name = users.addColumn(user -> user.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(User::getName));
    active = users.addColumn(new ComponentRenderer<>(user -> activeButton(user)), ACTIVE)
        .setKey(ACTIVE).setComparator((u1, u2) -> Boolean.compare(u1.isActive(), u2.isActive()));
    users.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = users.appendHeaderRow();
    filtersRow.getCell(email).setComponent(emailFilter);
    emailFilter.addValueChangeListener(e -> presenter.filterEmail(e.getValue()));
    emailFilter.setValueChangeMode(ValueChangeMode.EAGER);
    emailFilter.setSizeFull();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> presenter.filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(active).setComponent(activeFilter);
    activeFilter.setItems(Optional.empty(), Optional.of(false), Optional.of(true));
    activeFilter.addValueChangeListener(e -> presenter.filterActive(e.getValue().orElse(null)));
    activeFilter.setSizeFull();
    error.setId(ERROR_TEXT);
    add.setId(ADD);
    add.addClickListener(e -> presenter.add());
    switchUser.setId(SWITCH_USER);
    switchUser.addClickListener(e -> presenter.switchUser());
    presenter.init(this);
  }

  private Button activeButton(User user) {
    Button button = new Button();
    button.addClassName(ACTIVE);
    actives.put(user, button);
    updateActiveButton(button, user);
    button.addClickListener(e -> {
      presenter.toggleActive(user);
      updateActiveButton(button, user);
    });
    return button;
  }

  private void updateActiveButton(Button button, User user) {
    final AppResources userResources = new AppResources(User.class, getLocale());
    button.setIcon(user.isActive() ? VaadinIcon.EYE.create() : VaadinIcon.EYE_SLASH.create());
    button.setText(userResources.message(property(ACTIVE, user.isActive())));
    button
        .addThemeVariants(user.isActive() ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_ERROR);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(UsersView.class, getLocale());
    final AppResources userResources = new AppResources(User.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    String emailHeader = userResources.message(EMAIL);
    email.setHeader(emailHeader).setFooter(emailHeader);
    String nameHeader = userResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String activeHeader = userResources.message(ACTIVE);
    active.setHeader(activeHeader).setFooter(activeHeader);
    emailFilter.setPlaceholder(webResources.message(ALL));
    nameFilter.setPlaceholder(webResources.message(ALL));
    activeFilter.setItemLabelGenerator(value -> value
        .map(bv -> userResources.message(property(ACTIVE, bv))).orElse(webResources.message(ALL)));
    actives.entrySet().stream().forEach(entry -> entry.getValue()
        .setText(userResources.message(property(ACTIVE, entry.getKey().isActive()))));
    add.setText(webResources.message(ADD));
    add.setIcon(VaadinIcon.PLUS.create());
    switchUser.setText(resources.message(SWITCH_USER));
    switchUser.setIcon(VaadinIcon.BUG.create());
    presenter.localeChange(getLocale());
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    activeFilter.setValue(Optional.empty());
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(UsersView.class, getLocale());
    AppResources webResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, webResources.message(APPLICATION_NAME));
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    presenter.showError(event.getLocation().getQueryParameters().getParameters());
  }
}
