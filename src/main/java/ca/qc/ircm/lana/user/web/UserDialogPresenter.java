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
import static ca.qc.ircm.lana.user.web.UserDialog.LABORATORY_NAME;
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
  private Binder<Laboratory> laboratoryBinder = new BeanValidationBinder<>(Laboratory.class);
  private ListDataProvider<Laboratory> laboratoriesDataProvider;
  private User user;
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
    dialog.manager.setVisible(authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER));
    dialog.manager.addValueChangeListener(e -> updateManager());
    dialog.createNewLaboratory.setReadOnly(true);
    dialog.createNewLaboratory.addValueChangeListener(e -> updateCreateNewLaboratory());
    if (authorizationService.hasRole(UserRole.ADMIN)) {
      laboratoriesDataProvider = new LaboratoryDataProvider(laboratoryService.all());
    } else {
      laboratoriesDataProvider =
          new LaboratoryDataProvider(Stream.of(authorizationService.currentUser().getLaboratory())
              .collect(Collectors.toCollection(ArrayList::new)));
    }
    dialog.laboratory.setDataProvider(laboratoriesDataProvider);
    dialog.laboratory.setItemLabelGenerator(lab -> lab.getName());
    dialog.newLaboratoryLayout.setVisible(false);
    setUser(null);
    passwordBinder.setBean(new Passwords());
    laboratoryBinder.setBean(new Laboratory());
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
    binder.forField(dialog.manager).bind(MANAGER);
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
    laboratoryBinder.forField(dialog.newLaboratoryName).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(LABORATORY_NAME);
    dialog.save.setText(webResources.message(SAVE));
    dialog.cancel.setText(webResources.message(CANCEL));
  }

  private Validator<String> passwordRequiredValidator(String errorMessage) {
    return (value, context) -> isNewUser() && value.isEmpty() ? ValidationResult.error(errorMessage)
        : ValidationResult.ok();
  }

  private Validator<Laboratory> laboratoryRequiredValidator(String errorMessage) {
    return (value, context) -> !dialog.createNewLaboratory.getValue() && value == null
        ? ValidationResult.error(errorMessage)
        : ValidationResult.ok();
  }

  private boolean isNewUser() {
    return user.getId() == null;
  }

  private void updateManager() {
    if (authorizationService.hasRole(UserRole.ADMIN)) {
      dialog.createNewLaboratory.setReadOnly(!dialog.manager.getValue());
      if (!dialog.manager.getValue()) {
        dialog.createNewLaboratory.setValue(false);
        dialog.laboratory.setVisible(true);
        dialog.newLaboratoryLayout.setVisible(false);
      }
    }
  }

  private void updateCreateNewLaboratory() {
    dialog.laboratory.setVisible(!dialog.createNewLaboratory.getValue());
    dialog.newLaboratoryLayout.setVisible(dialog.createNewLaboratory.getValue());
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  BinderValidationStatus<Passwords> validatePassword() {
    return passwordBinder.validate();
  }

  BinderValidationStatus<Laboratory> validateLaboratory() {
    return laboratoryBinder.validate();
  }

  private boolean validate() {
    boolean valid = true;
    valid = validateUser().isOk() && valid;
    valid = validatePassword().isOk() && valid;
    if (dialog.createNewLaboratory.getValue()) {
      valid = validateLaboratory().isOk() && valid;
    }
    return valid;
  }

  void save() {
    if (validate()) {
      if (dialog.createNewLaboratory.getValue()) {
        user.setLaboratory(laboratoryBinder.getBean());
      }
      String password = passwordBinder.getBean().getPassword();
      logger.debug("save user {} in laboratory {}", user, user.getLaboratory());
      userService.save(user, password);
      dialog.close();
    }
  }

  void cancel() {
    dialog.close();
  }

  User getUser() {
    return user;
  }

  /**
   * Sets user.
   *
   * @param user
   *          user
   */
  void setUser(User user) {
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
