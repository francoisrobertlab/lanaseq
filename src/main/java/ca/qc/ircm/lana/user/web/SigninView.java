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

import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;

import ca.qc.ircm.lana.security.AuthenticationService;
import ca.qc.ircm.lana.security.LdapConfiguration;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserProperties;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import javax.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sign in view.
 */
@Route(value = SigninView.VIEW_NAME)
public class SigninView extends Composite<VerticalLayout>
    implements LocaleChangeObserver, BaseComponent {
  public static final String VIEW_NAME = "signin";
  public static final String HEADER = "header";
  public static final String EMAIL = UserProperties.EMAIL;
  public static final String PASSWORD = UserProperties.HASHED_PASSWORD;
  public static final String SIGNIN = "signin";
  public static final String FAIL = "fail";
  public static final String DISABLED = "disabled";
  public static final String EXCESSIVE_ATTEMPTS = "excessiveAttempts";
  private static final long serialVersionUID = 638443368018456019L;
  private static final Logger logger = LoggerFactory.getLogger(SigninView.class);
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  protected H2 header = new H2();
  protected TextField email = new TextField();
  protected PasswordField password = new PasswordField();
  protected Button signin = new Button();
  protected Div error = new Div();
  @Inject
  private AuthenticationService authenticationService;
  @Inject
  private LdapConfiguration ldapConfiguration;

  /**
   * Creates {@link SigninView}.
   */
  public SigninView() {
    VerticalLayout root = getContent();
    root.add(header);
    header.setId(HEADER);
    root.add(email);
    email.setId(EMAIL);
    email.addKeyDownListener(Key.ENTER, e -> sign());
    root.add(password);
    password.setId(PASSWORD);
    password.addKeyDownListener(Key.ENTER, e -> sign());
    root.add(signin);
    signin.setId(SIGNIN);
    signin.addClickListener(e -> sign());
    root.add(error);
    error.setVisible(false);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource resources = new MessageResource(getClass(), getLocale());
    final MessageResource generalResources = new MessageResource(WebConstants.class, getLocale());
    header.setText(resources.message(HEADER));
    email.setLabel(resources.message(EMAIL));
    password.setLabel(resources.message(PASSWORD));
    signin.setText(resources.message(SIGNIN));
    binder.forField(email).asRequired(generalResources.message(REQUIRED))
        .withValidator(emailValidator(generalResources.message(INVALID_EMAIL))).bind(EMAIL);
    binder.forField(password).asRequired(generalResources.message(REQUIRED)).bind(PASSWORD);
  }

  private Validator<String> emailValidator(String errorMessage) {
    if (ldapConfiguration.isEnabled()) {
      return Validator.alwaysPass();
    } else {
      return new EmailValidator(errorMessage);
    }
  }

  private void sign() {
    error.setVisible(false);
    MessageResource resources = new MessageResource(getClass(), getLocale());
    if (binder.isValid()) {
      try {
        authenticationService.sign(email.getValue(), password.getValue(), true);
        logger.debug("User {} signed successfully", email.getValue());
        navigate("");
      } catch (DisabledAccountException e) {
        logger.debug("Account disabled for user {}", email.getValue());
        error.setText(resources.message(DISABLED));
        error.setVisible(true);
      } catch (ExcessiveAttemptsException e) {
        logger.debug("Excessive attempts for user {}", email.getValue());
        error.setText(resources.message(EXCESSIVE_ATTEMPTS));
        error.setVisible(true);
      } catch (AuthenticationException e) {
        error.setText(resources.message(FAIL));
        error.setVisible(true);
      }
    }
  }
}
