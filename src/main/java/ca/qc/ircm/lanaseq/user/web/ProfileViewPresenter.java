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
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
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
 * Profile view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProfileViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(ProfileViewPresenter.class);
  private ProfileView view;
  private Locale locale;
  private UserService service;
  private AuthenticatedUser authenticatedUser;

  protected ProfileViewPresenter() {
  }

  @Autowired
  protected ProfileViewPresenter(UserService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  void init(ProfileView view) {
    this.view = view;
    view.form.setUser(authenticatedUser.getUser().orElse(null));
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  void save() {
    if (view.form.isValid()) {
      User user = view.form.getUser();
      String password = view.form.getPassword();
      logger.debug("save user {}", user);
      service.save(user, password);
      final AppResources resources = new AppResources(ProfileView.class, locale);
      view.showNotification(resources.message(SAVED, user.getEmail()));
    }
  }
}
