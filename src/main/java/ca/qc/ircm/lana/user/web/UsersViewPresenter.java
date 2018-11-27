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

import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserService;
import com.vaadin.flow.spring.annotation.SpringComponent;
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

  protected UsersViewPresenter() {
  }

  protected UsersViewPresenter(UserService userService) {
    this.userService = userService;
  }

  void init(UsersView view) {
    this.view = view;
    view.users.setItems(userService.all());
    view.userDialog.addSaveListener(e -> save(e.getSavedObject()));
  }

  void view(User user) {
    view.userDialog.setUser(userService.get(user.getId()));
    view.userDialog.open();
  }

  void add() {
    User user = new User();
    user.setLaboratory(new Laboratory());
    view.userDialog.setUser(user);
    view.userDialog.open();
  }

  private void save(UserWithPassword userWithPassword) {
    userService.save(userWithPassword.user, userWithPassword.password);
  }
}
