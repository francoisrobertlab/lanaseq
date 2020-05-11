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
import static ca.qc.ircm.lanaseq.user.UserProperties.ADMIN;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.Permission;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * User form presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserFormPresenter {
  private UserForm form;
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  private User user;
  @Autowired
  private AuthorizationService authorizationService;

  protected UserFormPresenter() {
  }

  protected UserFormPresenter(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  void init(UserForm form) {
    this.form = form;
    form.admin.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    form.manager.setVisible(authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER));
    setUser(null);
  }

  void localeChange(Locale locale) {
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(form.email).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(new EmailValidator(webResources.message(INVALID_EMAIL))).bind(EMAIL);
    binder.forField(form.name).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(NAME);
    binder.forField(form.admin).bind(ADMIN);
    binder.forField(form.manager).bind(MANAGER);
    updateReadOnly();
  }

  private void updateReadOnly() {
    boolean readOnly =
        user.getId() != null && !authorizationService.hasPermission(user, Permission.WRITE);
    binder.setReadOnly(readOnly);
    form.passwords.setVisible(!readOnly);
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  boolean isValid() {
    boolean valid = true;
    valid = validateUser().isOk() && valid;
    valid = form.passwords.validate().isOk() && valid;
    return valid;
  }

  String getPassword() {
    return form.passwords.getPassword();
  }

  User getUser() {
    return user;
  }

  void setUser(User user) {
    if (user == null) {
      user = new User();
    }
    if (user.getLaboratory() == null) {
      user.setLaboratory(authorizationService.getCurrentUser().getLaboratory());
    }
    this.user = user;
    binder.setBean(user);
    form.passwords.password.setValue("");
    form.passwords.passwordConfirm.setValue("");
    form.passwords.setRequired(user.getId() == null);
    updateReadOnly();
  }
}
