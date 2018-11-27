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

import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.web.WebConstants.BORDER;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.SaveEvent;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
  protected H2 header = new H2();
  protected UserForm userForm = new UserForm();
  protected PasswordForm passwordForm = new PasswordForm();
  protected VerticalLayout laboratoryLayout = new VerticalLayout();
  protected H6 laboratoryHeader = new H6();
  protected LaboratoryForm laboratoryForm = new LaboratoryForm();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected Button cancel = new Button();
  private User user;
  private boolean readOnly;

  /**
   * Creates a new UserDialog.
   */
  public UserDialog() {
    setId(CLASS_NAME);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(header);
    header.addClassName(HEADER);
    layout.add(userForm);
    layout.add(passwordForm);
    passwordForm.setRequired(true);
    layout.add(laboratoryLayout);
    laboratoryLayout.addClassName(BORDER);
    laboratoryLayout.add(laboratoryHeader);
    laboratoryHeader.addClassName(LABORATORY);
    laboratoryLayout.add(laboratoryForm);
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
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource userResources = new MessageResource(User.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    updateHeader();
    laboratoryHeader.setText(userResources.message(LABORATORY));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
  }

  private void updateHeader() {
    final MessageResource resources = new MessageResource(UserDialog.class, getLocale());
    if (user != null && user.getId() != null) {
      header.setText(resources.message(HEADER, 1, user.getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
  }

  private boolean isAdmin() {
    return user != null && user.isAdmin();
  }

  private boolean validate() {
    boolean valid = true;
    valid = userForm.validate().isOk() && valid;
    valid = passwordForm.validate().isOk() && valid;
    if (!isAdmin()) {
      valid = laboratoryForm.validate().isOk() && valid;
    }
    return valid;
  }

  private void save() {
    if (validate()) {
      logger.debug("Fire save event for user {}", user);
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
    userForm.setReadOnly(readOnly);
    passwordForm.setVisible(!readOnly);
    laboratoryForm.setReadOnly(readOnly);
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
    this.user = user;
    userForm.setUser(user);
    if (user != null && user.getId() != null) {
      passwordForm.setRequired(false);
    } else {
      passwordForm.setRequired(true);
    }
    laboratoryForm.setLaboratory(user != null ? user.getLaboratory() : null);
    laboratoryLayout.setVisible(user == null || !user.isAdmin());
    updateHeader();
  }
}
