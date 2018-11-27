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

import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

/**
 * Users form.
 */
public class UserForm extends Composite<VerticalLayout>
    implements LocaleChangeObserver, BaseComponent {
  private static final long serialVersionUID = 7602423333455397115L;
  public static final String CLASS_NAME = "user-form";
  protected TextField email = new TextField();
  protected TextField name = new TextField();
  private Binder<User> binder = new BeanValidationBinder<>(User.class);
  private boolean readOnly;

  /**
   * Creates a new {@link UserForm}.
   */
  public UserForm() {
    VerticalLayout root = getContent();
    root.setPadding(false);
    root.addClassName(CLASS_NAME);
    root.add(email);
    email.addClassName(EMAIL);
    root.add(name);
    name.addClassName(NAME);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource userResources = new MessageResource(User.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    email.setLabel(userResources.message(EMAIL));
    binder.forField(email).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .withValidator(new EmailValidator(webResources.message(INVALID_EMAIL))).bind(EMAIL);
    name.setLabel(userResources.message(NAME));
    binder.forField(name).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(NAME);
    setReadOnly(readOnly);
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Sets if form should be read only.
   *
   * @param readOnly
   *          read only
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    binder.setReadOnly(readOnly);
  }

  public User getUser() {
    return binder.getBean();
  }

  /**
   * Sets user.
   *
   * @param user
   *          user
   */
  public void setUser(User user) {
    binder.setBean(user);
  }

  /**
   * Validates form.
   *
   * @return validation status
   */
  public BinderValidationStatus<User> validate() {
    return binder.validate();
  }
}
