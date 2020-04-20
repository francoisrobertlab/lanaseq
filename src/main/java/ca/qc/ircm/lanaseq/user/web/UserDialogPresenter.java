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

import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * User dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(UserDialogPresenter.class);
  private UserDialog dialog;
  private UserService userService;

  protected UserDialogPresenter() {
  }

  @Autowired
  protected UserDialogPresenter(UserService userService) {
    this.userService = userService;
  }

  void init(UserDialog dialog) {
    this.dialog = dialog;
  }

  void save(Locale locale) {
    if (dialog.form.isValid()) {
      User user = dialog.form.getUser();
      String password = dialog.form.getPassword();
      logger.debug("save user {} in laboratory {}", user, user.getLaboratory());
      userService.save(user, password);
      final AppResources resources = new AppResources(UserDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, user.getEmail()));
      dialog.close();
      dialog.fireSavedEvent();
    }
  }

  void cancel() {
    dialog.close();
  }
}
