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

import static ca.qc.ircm.lana.user.UserRole.ADMIN;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryService;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserService;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.List;
import javax.inject.Inject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Users view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UsersViewPresenter {
  private UsersView view;
  @Inject
  private UserService userService;
  @Inject
  private LaboratoryService laboratoryService;
  @Inject
  private AuthorizationService authorizationService;
  private ListDataProvider<User> usersDataProvider;
  private WebUserFilter filter = new WebUserFilter();

  protected UsersViewPresenter() {
  }

  protected UsersViewPresenter(UserService userService, LaboratoryService laboratoryService,
      AuthorizationService authorizationService) {
    this.userService = userService;
    this.laboratoryService = laboratoryService;
    this.authorizationService = authorizationService;
  }

  void init(UsersView view) {
    this.view = view;
    loadUsers();
    view.userDialog.addSavedListener(e -> loadUsers());
    view.laboratoryDialog.addSavedListener(e -> loadUsers());
  }

  private void loadUsers() {
    List<User> users = authorizationService.hasRole(ADMIN) ? userService.all()
        : userService.all(authorizationService.currentUser().getLaboratory());
    usersDataProvider = new ListDataProvider<>(users);
    ConfigurableFilterDataProvider<User, Void, SerializablePredicate<User>> dataProvider =
        usersDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    view.users.setDataProvider(dataProvider);
  }

  void filterEmail(String value) {
    filter.emailContains = value.isEmpty() ? null : value;
    view.users.getDataProvider().refreshAll();
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    view.users.getDataProvider().refreshAll();
  }

  void filterLaboratory(String value) {
    filter.laboratoryNameContains = value.isEmpty() ? null : value;
    view.users.getDataProvider().refreshAll();
  }

  void filterActive(Boolean value) {
    filter.active = value;
    view.users.getDataProvider().refreshAll();
  }

  void view(User user) {
    view.userDialog.setUser(userService.get(user.getId()));
    view.userDialog.open();
  }

  void viewLaboratory(Laboratory laboratory) {
    view.laboratoryDialog.setLaboratory(laboratoryService.get(laboratory.getId()));
    view.laboratoryDialog.open();
  }

  void toggleActive(User user) {
    user.setActive(!user.isActive());
    userService.save(user, null);
  }

  void add() {
    view.userDialog.setUser(new User());
    view.userDialog.open();
  }

  WebUserFilter filter() {
    return filter;
  }
}
