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

import static ca.qc.ircm.lana.text.Strings.property;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;

import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

/**
 * Password form.
 */
public class PasswordForm extends Composite<VerticalLayout>
    implements LocaleChangeObserver, BaseComponent {
  private static final long serialVersionUID = 7602423333455397115L;
  public static final String CLASS_NAME = "password-form";
  public static final String PASSWORD = "password";
  public static final String PASSWORD_CONFIRM = PASSWORD + "Confirm";
  public static final String PASSWORDS_NOT_MATCH = property(PASSWORD, "notMatch");
  protected PasswordField password = new PasswordField();
  protected PasswordField passwordConfirm = new PasswordField();
  private Binder<Passwords> binder = new BeanValidationBinder<>(Passwords.class);
  private boolean required;

  /**
   * Creates a new {@link PasswordForm}.
   */
  public PasswordForm() {
    VerticalLayout root = getContent();
    root.setPadding(false);
    root.addClassName(CLASS_NAME);
    root.add(password);
    password.addClassName(PASSWORD);
    root.add(passwordConfirm);
    passwordConfirm.addClassName(PASSWORD_CONFIRM);
    binder.setBean(new Passwords());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource resources = new MessageResource(PasswordForm.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    password.setLabel(resources.message(PASSWORD));
    binder.forField(password).withValidator(requiredValidator(webResources.message(REQUIRED)))
        .withNullRepresentation("").withValidator(password -> {
          String confirmPassword = passwordConfirm.getValue();
          return password == null || password.isEmpty() || confirmPassword == null
              || confirmPassword.isEmpty() || password.equals(confirmPassword);
        }, resources.message(PASSWORDS_NOT_MATCH))
        .bind(Passwords::getPassword, Passwords::setPassword);
    passwordConfirm.setLabel(resources.message(PASSWORD_CONFIRM));
    binder.forField(passwordConfirm)
        .withValidator(requiredValidator(webResources.message(REQUIRED))).withNullRepresentation("")
        .bind(Passwords::getConfirmPassword, Passwords::setConfirmPassword);
  }

  private Validator<String> requiredValidator(String errorMessage) {
    return (value, context) -> !required || !value.isEmpty() ? ValidationResult.ok()
        : ValidationResult.error(errorMessage);
  }

  public boolean isRequired() {
    return required;
  }

  /**
   * Sets if a password is required.
   *
   * @param required
   *          true if a password is required, false otherwise
   */
  public void setRequired(boolean required) {
    this.required = required;
    password.setRequiredIndicatorVisible(required);
    passwordConfirm.setRequiredIndicatorVisible(required);
  }

  public String getPassword() {
    return binder.getBean().getPassword();
  }

  /**
   * Validates form.
   *
   * @return validation status
   */
  public BinderValidationStatus<Passwords> validate() {
    return binder.validate();
  }

  public static class Passwords {
    private String password;
    private String confirmPassword;

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getConfirmPassword() {
      return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
      this.confirmPassword = confirmPassword;
    }
  }
}
