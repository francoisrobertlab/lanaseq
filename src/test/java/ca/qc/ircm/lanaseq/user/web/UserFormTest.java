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
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.user.UserProperties.ADMIN;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;
import static ca.qc.ircm.lanaseq.user.web.UserForm.ID;
import static ca.qc.ircm.lanaseq.user.web.UserForm.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link UserForm}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class UserFormTest extends SpringUIUnitTest {
  private UserForm form;
  @Autowired
  private UserRepository repository;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;
  private Locale locale = Locale.ENGLISH;
  private AppResources userResources = new AppResources(User.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private String email = "test@ircm.qc.ca";
  private String name = "Test User";
  private String password = "test_password";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    navigate(ProfileView.class);
    form = $(UserForm.class).first();
  }

  private void fillForm() {
    form.email.setValue(email);
    form.name.setValue(name);
  }

  private void mockPasswordForm() {
    form.passwords = mock(PasswordsForm.class);
    form.passwords.password = new PasswordField();
    form.passwords.passwordConfirm = new PasswordField();
    BinderValidationStatus<Passwords> passwordsValidationStatus =
        mock(BinderValidationStatus.class);
    when(form.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(true);
  }

  @Test
  public void styles() {
    assertEquals(ID, form.getId().orElse(""));
    assertEquals(id(EMAIL), form.email.getId().orElse(""));
    assertEquals(id(NAME), form.name.getId().orElse(""));
    assertEquals(id(ADMIN), form.admin.getId().orElse(""));
    assertEquals(id(MANAGER), form.manager.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(userResources.message(EMAIL), form.email.getLabel());
    assertEquals(userResources.message(NAME), form.name.getLabel());
    assertEquals(userResources.message(ADMIN), form.admin.getLabel());
    assertEquals(userResources.message(MANAGER), form.manager.getLabel());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources userResources = new AppResources(User.class, locale);
    UI.getCurrent().setLocale(locale);
    assertEquals(userResources.message(EMAIL), form.email.getLabel());
    assertEquals(userResources.message(NAME), form.name.getLabel());
    assertEquals(userResources.message(ADMIN), form.admin.getLabel());
    assertEquals(userResources.message(MANAGER), form.manager.getLabel());
  }

  @Test
  public void currentUser_User() {
    assertFalse(form.admin.isVisible());
    assertFalse(form.manager.isVisible());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void currentUser_Manager() {
    assertFalse(form.admin.isVisible());
    assertTrue(form.manager.isVisible());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void currentUser_Admin() {
    assertTrue(form.admin.isVisible());
    assertTrue(form.manager.isVisible());
  }

  @Test
  public void getUser() {
    User user = new User();
    form.setUser(user);
    assertEquals(user, form.getUser());
  }

  @Test
  public void setUser_NewUser() {
    User user = new User();
    mockPasswordForm();

    form.setUser(user);

    assertEquals("", form.email.getValue());
    assertFalse(form.email.isReadOnly());
    assertEquals("", form.name.getValue());
    assertFalse(form.name.isReadOnly());
    assertFalse(form.admin.getValue());
    assertFalse(form.admin.isReadOnly());
    assertFalse(form.manager.getValue());
    assertFalse(form.manager.isReadOnly());
    verify(form.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(form.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void setUser_NewUserAdmin() {
    User user = new User();
    mockPasswordForm();

    form.setUser(user);

    assertEquals("", form.email.getValue());
    assertFalse(form.email.isReadOnly());
    assertEquals("", form.name.getValue());
    assertFalse(form.name.isReadOnly());
    assertFalse(form.admin.getValue());
    assertFalse(form.admin.isReadOnly());
    assertFalse(form.manager.getValue());
    assertFalse(form.manager.isReadOnly());
    verify(form.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(form.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
  }

  @Test
  public void setUser_User() {
    User user = repository.findById(2L).get();
    mockPasswordForm();

    form.setUser(user);

    assertEquals(user.getEmail(), form.email.getValue());
    assertTrue(form.email.isReadOnly());
    assertEquals(user.getName(), form.name.getValue());
    assertTrue(form.name.isReadOnly());
    assertFalse(form.admin.getValue());
    assertTrue(form.admin.isReadOnly());
    assertTrue(form.manager.getValue());
    assertTrue(form.manager.isReadOnly());
    verify(form.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void setUser_UserCanWrite() {
    User user = repository.findById(2L).get();
    mockPasswordForm();

    form.setUser(user);

    assertEquals(user.getEmail(), form.email.getValue());
    assertFalse(form.email.isReadOnly());
    assertEquals(user.getName(), form.name.getValue());
    assertFalse(form.name.isReadOnly());
    assertFalse(form.admin.getValue());
    assertFalse(form.admin.isReadOnly());
    assertTrue(form.manager.getValue());
    assertFalse(form.manager.isReadOnly());
    verify(form.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(form.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void setUser_UserAdmin() {
    User user = repository.findById(2L).get();
    mockPasswordForm();

    form.setUser(user);

    assertEquals(user.getEmail(), form.email.getValue());
    assertFalse(form.email.isReadOnly());
    assertEquals(user.getName(), form.name.getValue());
    assertFalse(form.name.isReadOnly());
    assertFalse(form.admin.getValue());
    assertFalse(form.admin.isReadOnly());
    assertTrue(form.manager.getValue());
    assertFalse(form.manager.isReadOnly());
    verify(form.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(form.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
  }

  @Test
  public void setUser_Null() {
    mockPasswordForm();

    form.setUser(null);

    assertEquals("", form.email.getValue());
    assertEquals("", form.name.getValue());
    assertFalse(form.admin.getValue());
    assertFalse(form.manager.getValue());
    verify(form.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
  }

  @Test
  public void setUser_PasswordReset() {
    form.passwords.password.setValue("test");
    form.passwords.passwordConfirm.setValue("test");

    form.setUser(null);

    assertEquals("", form.passwords.password.getValue());
    assertEquals("", form.passwords.passwordConfirm.getValue());
  }

  @Test
  public void isValid_EmailEmpty() {
    fillForm();
    form.email.setValue("");

    assertFalse(form.isValid());

    BinderValidationStatus<User> status = form.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void isValid_EmailInvalid() {
    fillForm();
    form.email.setValue("test");

    assertFalse(form.isValid());

    BinderValidationStatus<User> status = form.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(INVALID_EMAIL)), error.getMessage());
  }

  @Test
  public void isValid_NameEmpty() {
    fillForm();
    form.name.setValue("");

    assertFalse(form.isValid());

    BinderValidationStatus<User> status = form.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void isValid_PasswordValidationFailed() {
    mockPasswordForm();
    when(form.passwords.validate().isOk()).thenReturn(false);
    fillForm();

    assertFalse(form.isValid());
    BinderValidationStatus<User> status = form.validateUser();
    assertTrue(status.isOk());
  }

  @Test
  public void isValid_NewUser() {
    mockPasswordForm();
    when(form.passwords.getPassword()).thenReturn(password);
    fillForm();

    assertTrue(form.isValid());

    User user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertEquals(password, form.getPassword());
  }

  @Test
  public void isValid_NewManager() {
    mockPasswordForm();
    when(form.passwords.getPassword()).thenReturn(password);
    fillForm();
    form.manager.setValue(true);

    assertTrue(form.isValid());

    User user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals(password, form.getPassword());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void isValid_UpdateUser() {
    mockPasswordForm();
    when(form.passwords.getPassword()).thenReturn(password);
    User user = repository.findById(2L).get();
    form.setUser(user);
    fillForm();

    assertTrue(form.isValid());

    user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals(password, form.getPassword());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void isValid_UpdateUserNoPassword() {
    User user = repository.findById(2L).get();
    form.setUser(user);
    fillForm();

    assertTrue(form.isValid());

    user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNull(form.getPassword());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void isValid_NewAdmin() {
    mockPasswordForm();
    when(form.passwords.getPassword()).thenReturn(password);
    form.setUser(new User());
    fillForm();
    form.admin.setValue(true);

    assertTrue(form.isValid());

    User user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertFalse(user.isManager());
    assertEquals(password, form.getPassword());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void isValid_UpdateAdmin() {
    mockPasswordForm();
    when(form.passwords.getPassword()).thenReturn(password);
    User user = repository.findById(1L).get();
    form.setUser(user);
    fillForm();

    assertTrue(form.isValid());

    user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals(password, form.getPassword());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void isValid_UpdateAdminNoPassword() {
    User user = repository.findById(1L).get();
    form.setUser(user);
    fillForm();

    assertTrue(form.isValid());

    user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
    assertNull(form.getPassword());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void isValid_UpdateAdmin_RemoveAdminAddManager() {
    User user = repository.findById(1L).get();
    form.setUser(user);
    fillForm();
    form.admin.setValue(false);
    form.manager.setValue(true);

    assertTrue(form.isValid());

    user = form.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNull(form.getPassword());
  }
}
