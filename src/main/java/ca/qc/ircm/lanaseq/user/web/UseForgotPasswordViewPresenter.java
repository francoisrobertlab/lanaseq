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

import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.INVALID;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SEPARATOR;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Use forgot password view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UseForgotPasswordViewPresenter {
  private static final Logger logger =
      LoggerFactory.getLogger(UseForgotPasswordViewPresenter.class);
  private UseForgotPasswordView view;
  private ForgotPassword forgotPassword;
  private Locale locale;
  private ForgotPasswordService service;

  @Autowired
  protected UseForgotPasswordViewPresenter(ForgotPasswordService service) {
    this.service = service;
  }

  void init(UseForgotPasswordView view) {
    this.view = view;
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  void save() {
    if (view.form.isValid()) {
      String password = view.form.getPassword();
      logger.debug("save new password for user {}", forgotPassword.getUser());
      service.updatePassword(forgotPassword, password);
      final AppResources resources = new AppResources(UseForgotPasswordView.class, locale);
      view.showNotification(resources.message(SAVED));
      UI.getCurrent().navigate(SigninView.class);
    }
  }

  private boolean validateParameter(String parameter, Locale locale) {
    final AppResources resources = new AppResources(UseForgotPasswordView.class, locale);
    if (parameter == null) {
      view.showNotification(resources.message(INVALID));
      return false;
    }

    String[] parameters = parameter.split(SEPARATOR, -1);
    boolean valid = true;
    if (parameters.length < 2) {
      valid = false;
    } else {
      try {
        long id = Long.parseLong(parameters[0]);
        String confirmNumber = parameters[1];
        if (!service.get(id, confirmNumber).isPresent()) {
          valid = false;
        }
      } catch (NumberFormatException e) {
        valid = false;
      }
    }
    if (!valid) {
      view.showNotification(resources.message(INVALID));
    }
    return valid;
  }

  void setParameter(String parameter, Locale locale) {
    if (validateParameter(parameter, locale)) {
      String[] parameters = parameter.split(SEPARATOR, -1);
      long id = Long.parseLong(parameters[0]);
      String confirmNumber = parameters[1];
      forgotPassword = service.get(id, confirmNumber).orElse(null);
    } else {
      view.save.setEnabled(false);
      view.form.setEnabled(false);
    }
  }
}
