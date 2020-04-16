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
import static ca.qc.ircm.lanaseq.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;
import static ca.qc.ircm.lanaseq.user.web.UserForm.LABORATORY_NAME;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryService;
import ca.qc.ircm.lanaseq.user.User;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.acls.domain.BasePermission;

/**
 * User form presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserFormPresenter {
  private UserForm form;
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  private Binder<Laboratory> laboratoryBinder = new BeanValidationBinder<>(Laboratory.class);
  private ListDataProvider<Laboratory> laboratoriesDataProvider;
  private User user;
  @Autowired
  private LaboratoryService laboratoryService;
  @Autowired
  private AuthorizationService authorizationService;

  protected UserFormPresenter() {
  }

  protected UserFormPresenter(LaboratoryService laboratoryService,
      AuthorizationService authorizationService) {
    this.laboratoryService = laboratoryService;
    this.authorizationService = authorizationService;
  }

  void init(UserForm form) {
    this.form = form;
    form.admin.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    form.manager.setVisible(authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER));
    form.manager.addValueChangeListener(e -> updateManager());
    form.laboratory.setReadOnly(!authorizationService.hasRole(UserRole.ADMIN));
    form.laboratory.setEnabled(authorizationService.hasRole(UserRole.ADMIN));
    form.laboratory.setItemLabelGenerator(lab -> lab.getName());
    if (authorizationService.hasRole(UserRole.ADMIN)) {
      laboratoriesDataProvider = new LaboratoryDataProvider(laboratoryService.all());
    } else {
      laboratoriesDataProvider =
          new LaboratoryDataProvider(Stream.of(authorizationService.currentUser().getLaboratory())
              .collect(Collectors.toCollection(ArrayList::new)));
    }
    form.laboratory.setDataProvider(laboratoriesDataProvider);
    form.createNewLaboratory.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    form.createNewLaboratory.setEnabled(false);
    form.createNewLaboratory.addValueChangeListener(e -> updateCreateNewLaboratory());
    form.newLaboratoryName.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    form.newLaboratoryName.setEnabled(false);
    setUser(null);
    laboratoryBinder.setBean(new Laboratory());
  }

  void localeChange(Locale locale) {
    final AppResources userResources = new AppResources(User.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    binder.forField(form.email).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(new EmailValidator(webResources.message(INVALID_EMAIL))).bind(EMAIL);
    binder.forField(form.name).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(NAME);
    binder.forField(form.admin).bind(ADMIN);
    binder.forField(form.manager).bind(MANAGER);
    form.laboratory.setLabel(userResources.message(LABORATORY));
    binder.forField(form.laboratory)
        .withValidator(laboratoryRequiredValidator(webResources.message(REQUIRED)))
        .withNullRepresentation(null).bind(LABORATORY);
    laboratoryBinder.forField(form.newLaboratoryName).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(LABORATORY_NAME);
    updateReadOnly();
  }

  private Validator<Laboratory> laboratoryRequiredValidator(String errorMessage) {
    return (value, context) -> !form.createNewLaboratory.getValue() && value == null
        ? ValidationResult.error(errorMessage)
        : ValidationResult.ok();
  }

  private void updateReadOnly() {
    boolean readOnly =
        user.getId() != null && !authorizationService.hasPermission(user, BasePermission.WRITE);
    binder.setReadOnly(readOnly);
    form.laboratory.setReadOnly(!authorizationService.hasRole(UserRole.ADMIN));
    form.laboratory.setEnabled(
        !authorizationService.hasRole(UserRole.ADMIN) || !form.createNewLaboratory.getValue());
    form.passwords.setVisible(!readOnly);
  }

  private void updateManager() {
    if (authorizationService.hasRole(UserRole.ADMIN)) {
      form.createNewLaboratory.setEnabled(form.manager.getValue());
      if (!form.manager.getValue()) {
        form.createNewLaboratory.setValue(false);
        form.laboratory.setEnabled(true);
        form.newLaboratoryName.setEnabled(false);
      }
    }
  }

  private void updateCreateNewLaboratory() {
    form.laboratory.setEnabled(!form.createNewLaboratory.getValue());
    form.newLaboratoryName.setEnabled(form.createNewLaboratory.getValue());
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  BinderValidationStatus<Laboratory> validateLaboratory() {
    return laboratoryBinder.validate();
  }

  boolean isValid() {
    boolean valid = true;
    valid = validateUser().isOk() && valid;
    valid = form.passwords.validate().isOk() && valid;
    if (form.createNewLaboratory.getValue()) {
      valid = validateLaboratory().isOk() && valid;
    }
    return valid;
  }

  String getPassword() {
    return form.passwords.getPassword();
  }

  User getUser() {
    if (form.laboratory.getValue() != null
        && (!form.createNewLaboratory.isEnabled() || !form.createNewLaboratory.getValue())) {
      user.getLaboratory().setId(form.laboratory.getValue().getId());
      user.getLaboratory().setName(form.laboratory.getValue().getName());
    } else {
      user.getLaboratory().setId(null);
      user.getLaboratory().setName(form.newLaboratoryName.getValue());
    }
    return user;
  }

  void setUser(User user) {
    if (user == null) {
      user = new User();
    }
    if (user.getLaboratory() == null && !laboratoriesDataProvider.getItems().isEmpty()) {
      user.setLaboratory(laboratoriesDataProvider.getItems().iterator().next());
    }
    this.user = user;
    binder.setBean(user);
    form.passwords.setRequired(user.getId() == null);
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
