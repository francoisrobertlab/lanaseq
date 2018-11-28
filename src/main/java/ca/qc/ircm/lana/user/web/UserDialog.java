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

import static ca.qc.ircm.lana.text.Strings.styleName;
import static ca.qc.ircm.lana.user.UserProperties.ADMIN;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.MANAGER;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.web.WebConstants.BORDER;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryProperties;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRole;
import ca.qc.ircm.lana.web.SaveEvent;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Users form.
 */
public class UserDialog extends Dialog implements LocaleChangeObserver, BaseComponent {
  private static final Logger logger = LoggerFactory.getLogger(UserDialog.class);
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String CLASS_NAME = "user-dialog";
  public static final String HEADER = "header";
  public static final String LABORATORY_NAME = LaboratoryProperties.NAME;
  protected H2 header = new H2();
  protected TextField email = new TextField();
  protected TextField name = new TextField();
  protected Checkbox admin = new Checkbox();
  protected Checkbox manager = new Checkbox();
  protected PasswordForm passwordForm = new PasswordForm();
  protected VerticalLayout laboratoryLayout = new VerticalLayout();
  protected H6 laboratoryHeader = new H6();
  protected TextField laboratoryName = new TextField();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected Button cancel = new Button();
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  private Binder<Laboratory> laboratoryBinder = new BeanValidationBinder<>(Laboratory.class);
  private User user;
  private boolean readOnly;

  /**
   * Creates a new UserDialog.
   */
  public UserDialog(AuthorizationService authorizationService) {
    setId(CLASS_NAME);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(header);
    header.addClassName(HEADER);
    layout.add(email);
    email.addClassName(EMAIL);
    layout.add(name);
    name.addClassName(NAME);
    layout.add(admin);
    admin.addClassName(ADMIN);
    admin.setVisible(authorizationService.hasRole(UserRole.ADMIN));
    layout.add(manager);
    manager.addClassName(MANAGER);
    admin.setVisible(authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER));
    admin.addValueChangeListener(e -> updateAdmin());
    layout.add(passwordForm);
    passwordForm.setRequired(true);
    layout.add(laboratoryLayout);
    laboratoryLayout.addClassName(BORDER);
    laboratoryLayout.add(laboratoryHeader);
    laboratoryHeader.addClassName(LABORATORY);
    laboratoryLayout.add(laboratoryName);
    laboratoryName.addClassName(styleName(LABORATORY, LABORATORY_NAME));
    layout.add(buttonsLayout);
    buttonsLayout.add(save);
    save.addClassName(SAVE);
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    buttonsLayout.add(cancel);
    cancel.addClassName(CANCEL);
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> close());
    setUser(null);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource userResources = new MessageResource(User.class, getLocale());
    final MessageResource laboratoryResources = new MessageResource(Laboratory.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    updateHeader();
    laboratoryHeader.setText(userResources.message(LABORATORY));
    email.setLabel(userResources.message(EMAIL));
    binder.forField(email).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .withValidator(new EmailValidator(webResources.message(INVALID_EMAIL))).bind(EMAIL);
    name.setLabel(userResources.message(NAME));
    binder.forField(name).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(NAME);
    admin.setLabel(userResources.message(ADMIN));
    binder.forField(admin).bind(ADMIN);
    manager.setLabel(userResources.message(MANAGER));
    binder.forField(manager).bind(MANAGER);
    laboratoryName.setLabel(laboratoryResources.message(LABORATORY_NAME));
    laboratoryBinder.forField(laboratoryName).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("").bind(LABORATORY_NAME);
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    setReadOnly(readOnly);
  }

  private void updateAdmin() {
    manager.setVisible(!admin.getValue());
    laboratoryLayout.setVisible(!admin.getValue());
  }

  private void updateHeader() {
    final MessageResource resources = new MessageResource(UserDialog.class, getLocale());
    if (user != null && user.getId() != null) {
      header.setText(resources.message(HEADER, 1, user.getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
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
    valid = passwordForm.validate().isOk() && valid;
    if (!admin.getValue()) {
      valid = validateLaboratory().isOk() && valid;
    }
    return valid;
  }

  private void save() {
    if (validate()) {
      logger.debug("Fire save event for user {}", user);
      if (user.isAdmin()) {
        user.setManager(false);
        user.setLaboratory(null);
      }
      fireEvent(
          new SaveEvent<>(this, false, new UserWithPassword(user, passwordForm.getPassword())));
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addSaveListener(ComponentEventListener<SaveEvent<UserWithPassword>> listener) {
    return addListener((Class) SaveEvent.class, listener);
  }

  void fireClickSave() {
    save();
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
    passwordForm.setVisible(!readOnly);
    laboratoryBinder.setReadOnly(readOnly);
    buttonsLayout.setVisible(!readOnly);
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
    if (user.getLaboratory() == null) {
      user.setLaboratory(new Laboratory());
    }
    this.user = user;
    binder.setBean(user);
    if (user != null && user.getId() != null) {
      passwordForm.setRequired(false);
    } else {
      passwordForm.setRequired(true);
    }
    laboratoryBinder.setBean(user.getLaboratory());
    updateHeader();
  }
}
