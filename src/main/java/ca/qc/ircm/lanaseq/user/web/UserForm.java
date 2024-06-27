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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.INVALID_EMAIL;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.user.UserProperties.ADMIN;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * User form.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserForm extends FormLayout implements LocaleChangeObserver {
  private static final String USER_PREFIX = messagePrefix(User.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String ID = "user-form";
  protected TextField email = new TextField();
  protected TextField name = new TextField();
  protected Checkbox admin = new Checkbox();
  protected Checkbox manager = new Checkbox();
  protected PasswordsForm passwords = new PasswordsForm();
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  private User user;
  private transient UserService service;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected UserForm(UserService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  /**
   * Initializes user dialog.
   */
  @PostConstruct
  protected void init() {
    setId(ID);
    add(new FormLayout(email, name, admin, manager, passwords));
    email.setId(id(EMAIL));
    name.setId(id(NAME));
    admin.setId(id(ADMIN));
    admin.setVisible(authenticatedUser.hasRole(UserRole.ADMIN));
    manager.setId(id(MANAGER));
    manager.setVisible(authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER));
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    email.setLabel(getTranslation(USER_PREFIX + EMAIL));
    name.setLabel(getTranslation(USER_PREFIX + NAME));
    admin.setLabel(getTranslation(USER_PREFIX + ADMIN));
    manager.setLabel(getTranslation(USER_PREFIX + MANAGER));
    binder.forField(email).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("")
        .withValidator(new EmailValidator(getTranslation(CONSTANTS_PREFIX + INVALID_EMAIL)))
        .withValidator(emailExists()).bind(EMAIL);
    binder.forField(name).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("").bind(NAME);
    binder.forField(admin).bind(ADMIN);
    binder.forField(manager).bind(MANAGER);
    updateReadOnly();
  }

  private Validator<String> emailExists() {
    return (value, context) -> {
      if (service.exists(value) && (user.getId() == null
          || !value.equalsIgnoreCase(service.get(user.getId()).map(User::getEmail).orElse("")))) {
        return ValidationResult.error(getTranslation(CONSTANTS_PREFIX + ALREADY_EXISTS));
      }
      return ValidationResult.ok();
    };
  }

  private void updateReadOnly() {
    boolean readOnly =
        user.getId() != null && !authenticatedUser.hasPermission(user, Permission.WRITE);
    binder.setReadOnly(readOnly);
    passwords.setVisible(!readOnly);
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  boolean isValid() {
    boolean valid = true;
    valid = validateUser().isOk() && valid;
    valid = passwords.validate().isOk() && valid;
    return valid;
  }

  String getPassword() {
    return passwords.getPassword();
  }

  User getUser() {
    return user;
  }

  void setUser(User user) {
    if (user == null) {
      user = new User();
    }
    this.user = user;
    binder.setBean(user);
    passwords.password.setValue("");
    passwords.passwordConfirm.setValue("");
    passwords.setRequired(user.getId() == null);
    updateReadOnly();
  }
}
