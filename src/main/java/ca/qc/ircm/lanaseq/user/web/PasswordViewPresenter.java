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

import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Change password view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PasswordViewPresenter {
  private Logger logger = LoggerFactory.getLogger(PasswordViewPresenter.class);
  private PasswordView view;
  @Autowired
  private UserService service;
  @Autowired
  private AuthorizationService authorizationService;
  private Locale locale;

  protected PasswordViewPresenter() {
  }

  protected PasswordViewPresenter(UserService service, AuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  void init(PasswordView view) {
    this.view = view;
    view.passwords.setRequired(true);
  }

  public void localeChange(Locale locale) {
    this.locale = locale;
  }

  private boolean validate() {
    return view.passwords.validate().isOk();
  }

  void save() {
    if (validate()) {
      User user = authorizationService.getCurrentUser().orElse(null);
      String password = view.passwords.getPassword();
      logger.debug("save new password for user {}", user);
      service.save(password);
      final AppResources resources = new AppResources(PasswordView.class, locale);
      view.showNotification(resources.message(SAVED));
      UI.getCurrent().navigate(MainView.class);
    }
  }
}
