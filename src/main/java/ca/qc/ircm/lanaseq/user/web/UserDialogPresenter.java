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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.INVALID_EMAIL;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.user.UserProperties.ADMIN;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.LABORATORY_NAME;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryService;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.acls.domain.BasePermission;

/**
 * User dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(UserDialogPresenter.class);
  private UserDialog dialog;
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  private Binder<Laboratory> laboratoryBinder = new BeanValidationBinder<>(Laboratory.class);
  private ListDataProvider<Laboratory> laboratoriesDataProvider;
  private User user;
  @Autowired
  private UserService userService;
  @Autowired
  private LaboratoryService laboratoryService;
  @Autowired
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
    dialog.laboratory.setReadOnly(!authorizationService.hasRole(UserRole.ADMIN));
    dialog.laboratory.setEnabled(authorizationService.hasRole(UserRole.ADMIN));
    dialog.laboratory.setItemLabelGenerator(lab -> lab.getName());
    if (authorizationService.hasRole(UserRole.ADMIN)) {
      laboratoriesDataProvider = new LaboratoryDataProvider(laboratoryService.all());
    } else {
      laboratoriesDataProvider =
          new LaboratoryDataProvider(Stream.of(authorizationService.currentUser().getLaboratory())
              .collect(Collectors.toCollection(ArrayList::new)));
    }
    dialog.laboratory.setDataProvider(laboratoriesDataProvider);
    dialog.createNewLaboratory.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    dialog.createNewLaboratory.setEnabled(false);
    dialog.createNewLaboratory.addValueChangeListener(e -> updateCreateNewLaboratory());
    dialog.newLaboratoryName.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    dialog.newLaboratoryName.setEnabled(false);
    setUser(null);
    laboratoryBinder.setBean(new Laboratory());
  }

  void localeChange(Locale locale) {
    final AppResources userResources = new AppResources(User.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(dialog.email).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(new EmailValidator(webResources.message(INVALID_EMAIL))).bind(EMAIL);
    binder.forField(dialog.name).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(NAME);
    binder.forField(dialog.admin).bind(ADMIN);
    binder.forField(dialog.manager).bind(MANAGER);
    dialog.laboratory.setLabel(userResources.message(LABORATORY));
    binder.forField(dialog.laboratory)
        .withValidator(laboratoryRequiredValidator(webResources.message(REQUIRED)))
        .withNullRepresentation(null).bind(LABORATORY);
    laboratoryBinder.forField(dialog.newLaboratoryName).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(LABORATORY_NAME);
    dialog.save.setText(webResources.message(SAVE));
    dialog.cancel.setText(webResources.message(CANCEL));
    updateReadOnly();
  }

  private Validator<Laboratory> laboratoryRequiredValidator(String errorMessage) {
    return (value, context) -> !dialog.createNewLaboratory.getValue() && value == null
        ? ValidationResult.error(errorMessage)
        : ValidationResult.ok();
  }

  private void updateReadOnly() {
    boolean readOnly =
        user.getId() != null && !authorizationService.hasPermission(user, BasePermission.WRITE);
    binder.setReadOnly(readOnly);
    dialog.laboratory.setReadOnly(!authorizationService.hasRole(UserRole.ADMIN));
    dialog.laboratory.setEnabled(
        !authorizationService.hasRole(UserRole.ADMIN) || !dialog.createNewLaboratory.getValue());
    dialog.passwords.setVisible(!readOnly);
  }

  private void updateManager() {
    if (authorizationService.hasRole(UserRole.ADMIN)) {
      dialog.createNewLaboratory.setEnabled(dialog.manager.getValue());
      if (!dialog.manager.getValue()) {
        dialog.createNewLaboratory.setValue(false);
        dialog.laboratory.setEnabled(true);
        dialog.newLaboratoryName.setEnabled(false);
      }
    }
  }

  private void updateCreateNewLaboratory() {
    dialog.laboratory.setEnabled(!dialog.createNewLaboratory.getValue());
    dialog.newLaboratoryName.setEnabled(dialog.createNewLaboratory.getValue());
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  BinderValidationStatus<Laboratory> validateLaboratory() {
    return laboratoryBinder.validate();
  }

  private boolean validate() {
    boolean valid = true;
    valid = validateUser().isOk() && valid;
    valid = dialog.passwords.validate().isOk() && valid;
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
      String password = dialog.passwords.getPassword();
      logger.debug("save user {} in laboratory {}", user, user.getLaboratory());
      userService.save(user, password);
      dialog.close();
      dialog.fireSavedEvent();
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
    dialog.passwords.setRequired(user.getId() == null);
    updateReadOnly();
  }

  ListDataProvider<Laboratory> laboratoryDataProvider() {
    return laboratoriesDataProvider;
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
