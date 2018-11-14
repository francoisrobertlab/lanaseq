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

import static ca.qc.ircm.lana.user.web.SigninView.DISABLED;
import static ca.qc.ircm.lana.user.web.SigninView.EMAIL;
import static ca.qc.ircm.lana.user.web.SigninView.EXCESSIVE_ATTEMPTS;
import static ca.qc.ircm.lana.user.web.SigninView.FAIL;
import static ca.qc.ircm.lana.user.web.SigninView.PASSWORD;
import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;

import ca.qc.ircm.lana.security.AuthenticationService;
import ca.qc.ircm.lana.security.LdapConfiguration;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Sign in view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SigninViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(SigninViewPresenter.class);
  private SigninView view;
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  @Inject
  private AuthenticationService authenticationService;
  @Inject
  private LdapConfiguration ldapConfiguration;

  void init(SigninView view) {
    logger.debug("signin view");
    this.view = view;
    final MessageResource generalResources =
        new MessageResource(WebConstants.class, view.getLocale());
    view.email.addKeyDownListener(Key.ENTER, e -> sign());
    view.password.addKeyDownListener(Key.ENTER, e -> sign());
    view.signin.addClickListener(e -> sign());
    binder.forField(view.email).asRequired(generalResources.message(REQUIRED))
        .withValidator(emailValidator(generalResources.message(INVALID_EMAIL))).bind(EMAIL);
    binder.forField(view.password).asRequired(generalResources.message(REQUIRED)).bind(PASSWORD);
  }

  private Validator<String> emailValidator(String errorMessage) {
    if (ldapConfiguration.isEnabled()) {
      return Validator.alwaysPass();
    } else {
      return new EmailValidator(errorMessage);
    }
  }

  private void sign() {
    view.error.setVisible(false);
    MessageResource resources = new MessageResource(SigninView.class, view.getLocale());
    if (binder.isValid()) {
      try {
        authenticationService.sign(view.email.getValue(), view.password.getValue(), true);
        logger.debug("User {} signed successfully", view.email.getValue());
        view.navigate("");
      } catch (DisabledAccountException e) {
        logger.debug("Account disabled for user {}", view.email.getValue());
        view.error.setText(resources.message(DISABLED));
        view.error.setVisible(true);
      } catch (ExcessiveAttemptsException e) {
        logger.debug("Excessive attempts for user {}", view.email.getValue());
        view.error.setText(resources.message(EXCESSIVE_ATTEMPTS));
        view.error.setVisible(true);
      } catch (AuthenticationException e) {
        view.error.setText(resources.message(FAIL));
        view.error.setVisible(true);
      }
    }
  }
}
