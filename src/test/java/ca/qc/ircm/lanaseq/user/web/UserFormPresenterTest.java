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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link UserFormPresenter}.
 */
@ServiceTestAnnotations
public class UserFormPresenterTest extends AbstractKaribuTestCase {
  private UserFormPresenter presenter;
  @Mock
  private UserForm form;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private BinderValidationStatus<Passwords> passwordsValidationStatus;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;
  @Autowired
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources webResources = new AppResources(Constants.class, locale);
  private String email = "test@ircm.qc.ca";
  private String name = "Test User";
  private String password = "test_password";
  private User currentUser;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    presenter = new UserFormPresenter(authenticatedUser);
    form.email = new TextField();
    form.name = new TextField();
    form.admin = new Checkbox();
    form.manager = new Checkbox();
    form.passwords = mock(PasswordsForm.class);
    form.passwords.password = new PasswordField();
    form.passwords.passwordConfirm = new PasswordField();
    currentUser = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(currentUser));
    when(form.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(true);
  }

  private void fillForm() {
    form.email.setValue(email);
    form.name.setValue(name);
  }

  @Test
  public void currentUser_User() {
    presenter.init(form);
    presenter.localeChange(locale);
    assertFalse(form.admin.isVisible());
    assertFalse(form.manager.isVisible());
  }

  @Test
  public void currentUser_Manager() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    assertFalse(form.admin.isVisible());
    assertTrue(form.manager.isVisible());
  }

  @Test
  public void currentUser_Admin() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    assertTrue(form.admin.isVisible());
    assertTrue(form.manager.isVisible());
  }

  @Test
  public void getUser() {
    presenter.init(form);
    User user = new User();
    presenter.setUser(user);
    assertEquals(user, presenter.getUser());
  }

  @Test
  public void setUser_NewUser() {
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    User user = new User();

    presenter.localeChange(locale);
    presenter.setUser(user);

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
  public void setUser_NewUserAdmin() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    User user = new User();

    presenter.localeChange(locale);
    presenter.setUser(user);

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
    presenter.init(form);
    User user = userRepository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setUser(user);

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
  public void setUser_UserCanWrite() {
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);

    presenter.localeChange(locale);
    presenter.setUser(user);

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
  public void setUser_UserAdmin() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setUser(user);

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
  public void setUser_UserBeforeLocaleChange() {
    presenter.init(form);
    User user = userRepository.findById(2L).get();

    presenter.setUser(user);
    presenter.localeChange(locale);

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
  public void setUser_UserCanWriteBeforeLocaleChange() {
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);

    presenter.setUser(user);
    presenter.localeChange(locale);

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
  public void setUser_UserAdminBeforeLocaleChange() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(2L).get();

    presenter.setUser(user);
    presenter.localeChange(locale);

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
    presenter.init(form);
    presenter.localeChange(locale);
    presenter.setUser(null);

    assertEquals("", form.email.getValue());
    assertEquals("", form.name.getValue());
    assertFalse(form.admin.getValue());
    assertFalse(form.manager.getValue());
    verify(form.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
  }

  @Test
  public void setUser_PasswordReset() {
    presenter.init(form);
    presenter.localeChange(locale);
    form.passwords.password.setValue("test");
    form.passwords.passwordConfirm.setValue("test");
    presenter.setUser(null);

    assertEquals("", form.passwords.password.getValue());
    assertEquals("", form.passwords.passwordConfirm.getValue());
  }

  @Test
  public void isValid_EmailEmpty() {
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.email.setValue("");

    assertFalse(presenter.isValid());

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void isValid_EmailInvalid() {
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.email.setValue("test");

    assertFalse(presenter.isValid());

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(INVALID_EMAIL)), error.getMessage());
  }

  @Test
  public void isValid_NameEmpty() {
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.name.setValue("");

    assertFalse(presenter.isValid());

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void isValid_PasswordValidationFailed() {
    when(passwordsValidationStatus.isOk()).thenReturn(false);
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();

    assertFalse(presenter.isValid());
  }

  @Test
  public void isValid_NewUser() {
    when(form.passwords.getPassword()).thenReturn(password);
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    User user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
  }

  @Test
  public void isValid_NewManager() {
    when(form.passwords.getPassword()).thenReturn(password);
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.manager.setValue(true);

    assertTrue(presenter.isValid());

    User user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
  }

  @Test
  public void isValid_UpdateUser() {
    when(form.passwords.getPassword()).thenReturn(password);
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
  }

  @Test
  public void isValid_UpdateUserLaboratory() {
    when(form.passwords.getPassword()).thenReturn(password);
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(6L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
  }

  @Test
  public void isValid_UpdateUserNoPassword() {
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
  }

  @Test
  public void isValid_NewAdmin() {
    when(form.passwords.getPassword()).thenReturn(password);
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.admin.setValue(true);

    assertTrue(presenter.isValid());

    User user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertFalse(user.isManager());
  }

  @Test
  public void isValid_UpdateAdmin() {
    when(form.passwords.getPassword()).thenReturn(password);
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(1L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
  }

  @Test
  public void isValid_UpdateAdminNoPassword() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(1L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
  }

  @Test
  public void isValid_UpdateAdmin_RemoveAdminAddManager() {
    when(authenticatedUser.hasAnyRole(any())).thenReturn(true);
    when(authenticatedUser.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(1L).get();
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();
    form.admin.setValue(false);
    form.manager.setValue(true);

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
  }
}
