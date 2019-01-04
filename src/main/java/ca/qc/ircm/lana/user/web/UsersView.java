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

package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.ViewLayout;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

/**
 * Users view.
 */
@Route(value = UsersView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ ADMIN, MANAGER })
public class UsersView extends Composite<VerticalLayout>
    implements LocaleChangeObserver, HasDynamicTitle, BaseComponent {
  public static final String VIEW_NAME = "users";
  public static final String HEADER = "header";
  public static final String USERS = "users";
  public static final String ADD = "add";
  private static final long serialVersionUID = 2568742367790329628L;
  protected H2 header = new H2();
  protected Grid<User> users = new Grid<>();
  protected Column<User> email;
  protected Column<User> laboratory;
  protected Button add = new Button();
  @Inject
  protected UserDialog userDialog;
  @Inject
  private transient UsersViewPresenter presenter;

  public UsersView() {
  }

  protected UsersView(UsersViewPresenter presenter, UserDialog userDialog) {
    this.presenter = presenter;
    this.userDialog = userDialog;
  }

  @PostConstruct
  void init() {
    VerticalLayout root = getContent();
    root.setId(VIEW_NAME);
    root.add(header);
    header.addClassName(HEADER);
    root.add(users);
    users.addClassName(USERS);
    users.setSelectionMode(SelectionMode.MULTI);
    email = users.addComponentColumn(user -> viewButton(user)).setKey(EMAIL);
    laboratory =
        users.addColumn(user -> user.getLaboratory() != null ? user.getLaboratory().getName() : "",
            LABORATORY).setKey(LABORATORY);
    HorizontalLayout buttonsLayout = new HorizontalLayout();
    root.add(buttonsLayout);
    buttonsLayout.add(add);
    add.addClassName(ADD);
    add.addClickListener(e -> presenter.add());
    presenter.init(this);
  }

  private Button viewButton(User user) {
    Button button = new Button();
    button.addClassName(EMAIL);
    button.setText(user.getEmail());
    button.addClickListener(e -> presenter.view(user));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    MessageResource resources = new MessageResource(UsersView.class, getLocale());
    MessageResource userResources = new MessageResource(User.class, getLocale());
    header.setText(resources.message(HEADER));
    String emailHeader = userResources.message(EMAIL);
    email.setHeader(emailHeader).setFooter(emailHeader);
    String laboratoryHeader = userResources.message(LABORATORY);
    laboratory.setHeader(laboratoryHeader).setFooter(laboratoryHeader);
    add.setText(resources.message(ADD));
    add.setIcon(VaadinIcon.PLUS.create());
  }

  @Override
  public String getPageTitle() {
    MessageResource resources = new MessageResource(UsersView.class, getLocale());
    MessageResource generalResources = new MessageResource(WebConstants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
