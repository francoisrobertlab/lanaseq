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

import static ca.qc.ircm.lana.user.UserProperties.ADMIN;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.MANAGER;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.user.web.UserDialog.NOT_MANAGER_NEW_LABORATORY;
import static ca.qc.ircm.lana.user.web.UserDialog.PASSWORDS_NOT_MATCH;
import static ca.qc.ircm.lana.user.web.UserDialog.PASSWORD_CONFIRM;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryService;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRole;
import ca.qc.ircm.lana.user.UserService;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Users dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(UserDialogPresenter.class);
  private UserDialog dialog;
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  private Binder<Passwords> passwordBinder = new BeanValidationBinder<>(Passwords.class);
  private ListDataProvider<Laboratory> laboratoriesDataProvider;
  private User user;
  private Laboratory newLaboratory = new Laboratory();
  private boolean readOnly;
  @Inject
  private UserService userService;
  @Inject
  private LaboratoryService laboratoryService;
  @Inject
  private AuthorizationService authorizationService;

  protected UserDialogPresenter() {
  }

  protected UserDialogPresenter(UserService userService, LaboratoryService laboratoryService,
      AuthorizationService authorizationService) {
    this.userService = userService;
    this.laboratoryService = laboratoryService;
    this.authorizationService = authorizationService;
  }

  void init(UserDialog dialog) {
    this.dialog = dialog;
    dialog.admin.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    dialog.admin.addValueChangeListener(e -> updateAdmin());
    dialog.manager.setVisible(authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER));
    if (authorizationService.hasRole(UserRole.ADMIN)) {
      laboratoriesDataProvider = new LaboratoryDataProvider(laboratoryService.all());
    } else {
      laboratoriesDataProvider =
          new LaboratoryDataProvider(Stream.of(authorizationService.currentUser().getLaboratory())
              .collect(Collectors.toCollection(ArrayList::new)));
    }
    dialog.laboratory.setDataProvider(laboratoriesDataProvider);
    dialog.laboratory.setItemLabelGenerator(lab -> lab.getName());
    dialog.laboratory.addCustomValueSetListener(e -> addNewLaboratory(e.getDetail()));
    dialog.laboratory.setAllowCustomValue(authorizationService.hasRole(UserRole.ADMIN));
    setUser(null);
    passwordBinder.setBean(new Passwords());
  }

  void localeChange(Locale locale) {
    final MessageResource resources = new MessageResource(UserDialog.class, locale);
    final MessageResource userResources = new MessageResource(User.class, locale);
    final MessageResource webResources = new MessageResource(WebConstants.class, locale);
    binder.forField(dialog.email).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(new EmailValidator(webResources.message(INVALID_EMAIL))).bind(EMAIL);
    binder.forField(dialog.name).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(NAME);
    binder.forField(dialog.admin).bind(ADMIN);
    binder.forField(dialog.manager)
        .withValidator(managerNewLaboratoryValidator(resources.message(NOT_MANAGER_NEW_LABORATORY)))
        .bind(MANAGER);
    passwordBinder.forField(dialog.password)
        .withValidator(passwordRequiredValidator(webResources.message(REQUIRED)))
        .withNullRepresentation("").withValidator(password -> {
          String confirmPassword = dialog.passwordConfirm.getValue();
          return password == null || password.isEmpty() || confirmPassword == null
              || confirmPassword.isEmpty() || password.equals(confirmPassword);
        }, resources.message(PASSWORDS_NOT_MATCH))
        .bind(Passwords::getPassword, Passwords::setPassword);
    dialog.passwordConfirm.setLabel(resources.message(PASSWORD_CONFIRM));
    passwordBinder.forField(dialog.passwordConfirm)
        .withValidator(passwordRequiredValidator(webResources.message(REQUIRED)))
        .withNullRepresentation("")
        .bind(Passwords::getConfirmPassword, Passwords::setConfirmPassword);
    dialog.laboratory.setLabel(userResources.message(LABORATORY));
    binder.forField(dialog.laboratory)
        .withValidator(laboratoryRequiredValidator(webResources.message(REQUIRED)))
        .withNullRepresentation(null).bind(LABORATORY);
    dialog.save.setText(webResources.message(SAVE));
    dialog.cancel.setText(webResources.message(CANCEL));
    setReadOnly(readOnly);
  }

  private Validator<String> passwordRequiredValidator(String errorMessage) {
    return (value, context) -> isNewUser() && value.isEmpty() ? ValidationResult.error(errorMessage)
        : ValidationResult.ok();
  }

  private Validator<Laboratory> laboratoryRequiredValidator(String errorMessage) {
    return (value,
        context) -> !dialog.admin.getValue()
            && (value == null || value.getName() == null || value.getName().trim().isEmpty())
                ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
  }

  private Validator<Boolean> managerNewLaboratoryValidator(String errorMessage) {
    return (value, context) -> !value
        && (dialog.laboratory.getValue() != null && dialog.laboratory.getValue().getId() == null)
            ? ValidationResult.error(errorMessage)
            : ValidationResult.ok();
  }

  private boolean isNewUser() {
    return user.getId() == null;
  }

  private void updateAdmin() {
    dialog.manager.setVisible(!dialog.admin.getValue());
    dialog.laboratory.setVisible(!dialog.admin.getValue());
  }

  void addNewLaboratory(String name) {
    if (!laboratoriesDataProvider.getItems().contains(newLaboratory)) {
      laboratoriesDataProvider.getItems().add(newLaboratory);
    }
    newLaboratory.setName(name);
    laboratoriesDataProvider.refreshItem(newLaboratory);
    dialog.laboratory.setValue(newLaboratory);
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  BinderValidationStatus<Passwords> validatePassword() {
    return passwordBinder.validate();
  }

  private boolean validate() {
    boolean valid = true;
    valid = validateUser().isOk() && valid;
    valid = validatePassword().isOk() && valid;
    return valid;
  }

  void save() {
    if (validate()) {
      user.setLaboratory(dialog.laboratory.getValue());
      if (user.isAdmin()) {
        user.setLaboratory(null);
      }
      String password = passwordBinder.getBean().getPassword();
      logger.debug("save user {} in laboratory {}", user, user.getLaboratory());
      userService.save(user, password);
      laboratoriesDataProvider.getItems().remove(newLaboratory);
    }
  }

  void cancel() {
    dialog.close();
    laboratoriesDataProvider.getItems().remove(newLaboratory);
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Sets if dialog should be read only.
   *
   * @param readOnly
   *          read only
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    binder.setReadOnly(readOnly);
    dialog.laboratory.setReadOnly(readOnly || !authorizationService.hasRole(UserRole.ADMIN));
    dialog.manager.setReadOnly(readOnly);
    dialog.password.setVisible(!readOnly);
    dialog.passwordConfirm.setVisible(!readOnly);
    dialog.buttonsLayout.setVisible(!readOnly);
  }

  public User getUser() {
    return user;
  }

  /**
   * Sets user.
   *
   * @param user
   *          user
   */
  public void setUser(User user) {
    if (user == null) {
      user = new User();
    }
    if (user.getLaboratory() == null && !laboratoriesDataProvider.getItems().isEmpty()) {
      user.setLaboratory(laboratoriesDataProvider.getItems().iterator().next());
    }
    this.user = user;
    binder.setBean(user);
    if (user != null && user.getId() != null) {
      dialog.password.setRequiredIndicatorVisible(false);
      dialog.passwordConfirm.setRequiredIndicatorVisible(false);
    } else {
      dialog.password.setRequiredIndicatorVisible(true);
      dialog.passwordConfirm.setRequiredIndicatorVisible(true);
    }
  }

  ListDataProvider<Laboratory> laboratoryDataProvider() {
    return laboratoriesDataProvider;
  }

  static class Passwords {
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

  @SuppressWarnings("serial")
  private static class LaboratoryDataProvider extends ListDataProvider<Laboratory> {
    public LaboratoryDataProvider(Collection<Laboratory> items) {
      super(items);
    }

    @Override
    public Object getId(Laboratory item) {
      return item.getId();
    }
  }
}
