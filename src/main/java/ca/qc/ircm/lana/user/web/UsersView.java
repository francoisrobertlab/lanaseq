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

import static ca.qc.ircm.lana.text.Strings.normalize;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.web.WebConstants.ALL;
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
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
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
  protected TextField emailFilter = new TextField();
  protected TextField laboratoryFilter = new TextField();
  protected Button add = new Button();
  @Inject
  protected UserDialog userDialog;
  @Inject
  protected LaboratoryDialog laboratoryDialog;
  @Inject
  private transient UsersViewPresenter presenter;

  public UsersView() {
  }

  protected UsersView(UsersViewPresenter presenter, UserDialog userDialog,
      LaboratoryDialog laboratoryDialog) {
    this.presenter = presenter;
    this.userDialog = userDialog;
    this.laboratoryDialog = laboratoryDialog;
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
    email = users.addColumn(new ComponentRenderer<>(user -> viewButton(user)), EMAIL).setKey(EMAIL)
        .setComparator((u1, u2) -> u1.getEmail().compareToIgnoreCase(u2.getEmail()));
    laboratory =
        users.addColumn(new ComponentRenderer<>(user -> viewLaboratoryButton(user)), LABORATORY)
            .setKey(LABORATORY).setComparator((u1, u2) -> normalize(u1.getLaboratory().getName())
                .compareToIgnoreCase(normalize(u2.getLaboratory().getName())));
    users.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = users.appendHeaderRow();
    filtersRow.getCell(email).setComponent(emailFilter);
    emailFilter.addValueChangeListener(e -> presenter.filterEmail(e.getValue()));
    emailFilter.setValueChangeMode(ValueChangeMode.EAGER);
    emailFilter.setSizeFull();
    filtersRow.getCell(laboratory).setComponent(laboratoryFilter);
    laboratoryFilter.addValueChangeListener(e -> presenter.filterLaboratory(e.getValue()));
    laboratoryFilter.setValueChangeMode(ValueChangeMode.EAGER);
    laboratoryFilter.setSizeFull();
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

  private Button viewLaboratoryButton(User user) {
    Button button = new Button();
    button.addClassName(LABORATORY);
    button.setText(user.getLaboratory().getName());
    button.addClickListener(e -> presenter.viewLaboratory(user.getLaboratory()));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource resources = new MessageResource(UsersView.class, getLocale());
    final MessageResource userResources = new MessageResource(User.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    header.setText(resources.message(HEADER));
    String emailHeader = userResources.message(EMAIL);
    email.setHeader(emailHeader).setFooter(emailHeader);
    String laboratoryHeader = userResources.message(LABORATORY);
    laboratory.setHeader(laboratoryHeader).setFooter(laboratoryHeader);
    emailFilter.setPlaceholder(webResources.message(ALL));
    laboratoryFilter.setPlaceholder(webResources.message(ALL));
    add.setText(resources.message(ADD));
    add.setIcon(VaadinIcon.PLUS.create());
  }

  @Override
  public String getPageTitle() {
    MessageResource resources = new MessageResource(UsersView.class, getLocale());
    MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    return resources.message(TITLE, webResources.message(APPLICATION_NAME));
  }
}
