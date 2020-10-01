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

import static ca.qc.ircm.lanaseq.Constants.INVALID_EMAIL;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Forgot password view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ForgotPasswordViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordViewPresenter.class);
  private ForgotPasswordView view;
  private Binder<User> binder = new BeanValidationBinder<User>(User.class);
  private Locale locale;
  private ForgotPasswordService service;
  private UserService userService;

  @Autowired
  protected ForgotPasswordViewPresenter(ForgotPasswordService service, UserService userService) {
    this.service = service;
    this.userService = userService;
  }

  void init(ForgotPasswordView view) {
    this.view = view;
    binder.setBean(new User());
  }

  void localeChange(Locale locale) {
    this.locale = locale;
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(view.email).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(new EmailValidator(webResources.message(INVALID_EMAIL))).bind(EMAIL);
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  boolean validate() {
    return validateUser().isOk();
  }

  void save() {
    if (validate()) {
      String email = view.email.getValue();
      logger.debug("create new forgot password for user {}", view.email.getValue());
      if (userService.exists(email)) {
        service.insert(email, (fp, fplocale) -> view.getUrl(UseForgotPasswordView.VIEW_NAME) + "/"
            + fp.getId() + UseForgotPasswordView.SEPARATOR + fp.getConfirmNumber());
      }
      final AppResources resources = new AppResources(ForgotPasswordView.class, locale);
      view.showNotification(resources.message(SAVED, email));
      UI.getCurrent().navigate(SigninView.class);
    }
  }
}
