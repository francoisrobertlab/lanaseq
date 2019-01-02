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

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.text.Strings.styleName;
import static ca.qc.ircm.lana.user.UserProperties.ADMIN;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.user.web.UserDialog.CLASS_NAME;
import static ca.qc.ircm.lana.user.web.UserDialog.HEADER;
import static ca.qc.ircm.lana.user.web.UserDialog.LABORATORY_NAME;
import static ca.qc.ircm.lana.user.web.UserDialog.MANAGER;
import static ca.qc.ircm.lana.user.web.UserDialog.PASSWORD;
import static ca.qc.ircm.lana.user.web.UserDialog.PASSWORDS_NOT_MATCH;
import static ca.qc.ircm.lana.user.web.UserDialog.PASSWORD_CONFIRM;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.web.UserDialog.Passwords;
import ca.qc.ircm.lana.web.SaveEvent;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserDialogTest extends AbstractViewTestCase {
  private UserDialog dialog;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private ComponentEventListener<SaveEvent<UserWithPassword>> saveListener;
  @Captor
  private ArgumentCaptor<SaveEvent<UserWithPassword>> saveEventCaptor;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;
  @Inject
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(UserDialog.class, locale);
  private MessageResource userResources = new MessageResource(User.class, locale);
  private MessageResource laboratoryResources = new MessageResource(Laboratory.class, locale);
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  private String email = "test@ircm.qc.ca";
  private String name = "Test User";
  private String password = "test_password";
  private String laboratoryName = "Test Laboratory";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new UserDialog(authorizationService);
  }

  private void fillForm() {
    dialog.email.setValue(email);
    dialog.name.setValue(name);
    dialog.password.setValue(password);
    dialog.passwordConfirm.setValue(password);
    dialog.laboratoryName.setValue(laboratoryName);
  }

  @Test
  public void styles() {
    assertEquals(CLASS_NAME, dialog.getId().orElse(""));
    assertTrue(dialog.header.getClassNames().contains(HEADER));
    assertTrue(dialog.email.getClassNames().contains(EMAIL));
    assertTrue(dialog.name.getClassNames().contains(NAME));
    assertTrue(dialog.admin.getClassNames().contains(ADMIN));
    assertTrue(dialog.manager.getClassNames().contains(MANAGER));
    assertTrue(dialog.password.getClassNames().contains(PASSWORD));
    assertTrue(dialog.passwordConfirm.getClassNames().contains(PASSWORD_CONFIRM));
    assertTrue(dialog.laboratoryHeader.getClassNames().contains(LABORATORY));
    assertTrue(
        dialog.laboratoryName.getClassNames().contains(styleName(LABORATORY, LABORATORY_NAME)));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertEquals(PRIMARY, dialog.save.getElement().getAttribute(THEME));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(LABORATORY), dialog.laboratoryHeader.getText());
    assertEquals(userResources.message(EMAIL), dialog.email.getLabel());
    assertEquals(userResources.message(NAME), dialog.name.getLabel());
    assertEquals(userResources.message(ADMIN), dialog.admin.getLabel());
    assertEquals(resources.message(MANAGER), dialog.manager.getLabel());
    assertEquals(resources.message(PASSWORD), dialog.password.getLabel());
    assertEquals(resources.message(PASSWORD_CONFIRM), dialog.passwordConfirm.getLabel());
    assertEquals(laboratoryResources.message(LABORATORY_NAME), dialog.laboratoryName.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save);
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel);
  }

  @Test
  public void localeChange() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources = new MessageResource(UserDialog.class, locale);
    final MessageResource userResources = new MessageResource(User.class, locale);
    final MessageResource laboratoryResources = new MessageResource(Laboratory.class, locale);
    final MessageResource webResources = new MessageResource(WebConstants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(LABORATORY), dialog.laboratoryHeader.getText());
    assertEquals(userResources.message(EMAIL), dialog.email.getLabel());
    assertEquals(userResources.message(NAME), dialog.name.getLabel());
    assertEquals(userResources.message(ADMIN), dialog.admin.getLabel());
    assertEquals(resources.message(MANAGER), dialog.manager.getLabel());
    assertEquals(resources.message(PASSWORD), dialog.password.getLabel());
    assertEquals(resources.message(PASSWORD_CONFIRM), dialog.passwordConfirm.getLabel());
    assertEquals(laboratoryResources.message(LABORATORY_NAME), dialog.laboratoryName.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
  }

  @Test
  public void isReadOnly_Default() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertFalse(dialog.isReadOnly());
  }

  @Test
  public void isReadOnly_False() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setReadOnly(false);
    assertFalse(dialog.isReadOnly());
  }

  @Test
  public void isReadOnly_True() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setReadOnly(true);
    assertTrue(dialog.isReadOnly());
  }

  @Test
  public void setReadOnly_False() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setReadOnly(false);
    assertFalse(dialog.email.isReadOnly());
    assertFalse(dialog.name.isReadOnly());
    assertFalse(dialog.admin.isReadOnly());
    assertFalse(dialog.manager.isReadOnly());
    assertTrue(dialog.password.isVisible());
    assertTrue(dialog.passwordConfirm.isVisible());
    assertFalse(dialog.laboratoryName.isReadOnly());
    assertTrue(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setReadOnly_True() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setReadOnly(true);
    assertTrue(dialog.email.isReadOnly());
    assertTrue(dialog.name.isReadOnly());
    assertTrue(dialog.admin.isReadOnly());
    assertTrue(dialog.manager.isReadOnly());
    assertFalse(dialog.password.isVisible());
    assertFalse(dialog.passwordConfirm.isVisible());
    assertTrue(dialog.laboratoryName.isReadOnly());
    assertFalse(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void checkAdmin() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.admin.setValue(true);
    assertFalse(dialog.manager.isVisible());
    assertFalse(dialog.laboratoryLayout.isVisible());
  }

  @Test
  public void uncheckAdmin() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.admin.setValue(true);
    dialog.admin.setValue(false);
    assertTrue(dialog.manager.isVisible());
    assertTrue(dialog.laboratoryLayout.isVisible());
  }

  @Test
  public void getUser() {
    User user = new User();
    dialog.setUser(user);
    assertEquals(user, dialog.getUser());
  }

  @Test
  public void setUser_NewUser() {
    User user = new User();
    Laboratory laboratory = new Laboratory();
    user.setLaboratory(laboratory);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    assertEquals("", dialog.email.getValue());
    assertEquals("", dialog.name.getValue());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.manager.getValue());
    assertTrue(dialog.password.isRequiredIndicatorVisible());
    assertTrue(dialog.passwordConfirm.isRequiredIndicatorVisible());
    assertEquals("", dialog.laboratoryName.getValue());
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setUser_User() {
    User user = userRepository.findById(2L).get();

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertEquals(user.getName(), dialog.name.getValue());
    assertFalse(dialog.admin.getValue());
    assertTrue(dialog.manager.getValue());
    assertFalse(dialog.password.isRequiredIndicatorVisible());
    assertFalse(dialog.passwordConfirm.isRequiredIndicatorVisible());
    assertEquals(user.getLaboratory().getName(), dialog.laboratoryName.getValue());
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_UserNoLaboratory() {
    User user = userRepository.findById(1L).get();

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertEquals(user.getName(), dialog.name.getValue());
    assertTrue(dialog.admin.getValue());
    assertFalse(dialog.manager.getValue());
    assertFalse(dialog.password.isRequiredIndicatorVisible());
    assertFalse(dialog.passwordConfirm.isRequiredIndicatorVisible());
    assertEquals("", dialog.laboratoryName.getValue());
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_UserBeforeLocaleChange() {
    User user = userRepository.findById(2L).get();

    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertEquals(user.getName(), dialog.name.getValue());
    assertFalse(dialog.admin.getValue());
    assertTrue(dialog.manager.getValue());
    assertFalse(dialog.password.isRequiredIndicatorVisible());
    assertFalse(dialog.passwordConfirm.isRequiredIndicatorVisible());
    assertEquals(user.getLaboratory().getName(), dialog.laboratoryName.getValue());
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(null);

    assertEquals("", dialog.email.getValue());
    assertEquals("", dialog.name.getValue());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.manager.getValue());
    assertTrue(dialog.password.isRequiredIndicatorVisible());
    assertTrue(dialog.passwordConfirm.isRequiredIndicatorVisible());
    assertEquals("", dialog.laboratoryName.getValue());
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void save_EmailEmpty() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.email.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();
    verify(saveListener, never()).onComponentEvent(any());

    BinderValidationStatus<User> status = dialog.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void save_EmailInvalid() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.email.setValue("test");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();
    verify(saveListener, never()).onComponentEvent(any());

    BinderValidationStatus<User> status = dialog.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(INVALID_EMAIL)), error.getMessage());
  }

  @Test
  public void save_NameEmpty() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.name.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();
    verify(saveListener, never()).onComponentEvent(any());

    BinderValidationStatus<User> status = dialog.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void save_PasswordEmpty() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.password.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();
    verify(saveListener, never()).onComponentEvent(any());

    BinderValidationStatus<Passwords> status = dialog.validatePassword();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.password);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void save_PasswordsNotMatch() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.password.setValue("test");
    dialog.passwordConfirm.setValue("test2");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();
    verify(saveListener, never()).onComponentEvent(any());

    BinderValidationStatus<Passwords> status = dialog.validatePassword();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.password);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(resources.message(PASSWORDS_NOT_MATCH)), error.getMessage());
  }

  @Test
  public void save_PasswordConfirmEmpty() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.passwordConfirm.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();
    verify(saveListener, never()).onComponentEvent(any());

    BinderValidationStatus<Passwords> status = dialog.validatePassword();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.passwordConfirm);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void save_LaboratoryNameEmpty() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.laboratoryName.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();
    verify(saveListener, never()).onComponentEvent(any());

    BinderValidationStatus<Laboratory> status = dialog.validateLaboratory();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.laboratoryName);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void save_AdminLaboratoryNameEmpty() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.admin.setValue(true);
    dialog.laboratoryName.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(any());
  }

  @Test
  @Ignore("cannot create regular users yet")
  public void save_NewUser() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertFalse(userWithPassword.user.isAdmin());
    assertNotNull(userWithPassword.user.getLaboratory());
    assertEquals((Long) 1L, userWithPassword.user.getLaboratory().getId());
    assertEquals(laboratoryName, userWithPassword.user.getLaboratory().getName());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_NewManager() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.manager.setValue(true);
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertFalse(userWithPassword.user.isAdmin());
    assertNotNull(userWithPassword.user.getLaboratory());
    assertNull(userWithPassword.user.getLaboratory().getId());
    assertEquals(laboratoryName, userWithPassword.user.getLaboratory().getName());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateUser() {
    User user = userRepository.findById(2L).get();
    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertFalse(userWithPassword.user.isAdmin());
    assertNotNull(userWithPassword.user.getLaboratory());
    assertEquals(laboratoryName, userWithPassword.user.getLaboratory().getName());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateUserNoPassword() {
    User user = userRepository.findById(2L).get();
    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.password.setValue("");
    dialog.passwordConfirm.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertFalse(userWithPassword.user.isAdmin());
    assertNotNull(userWithPassword.user.getLaboratory());
    assertEquals(laboratoryName, userWithPassword.user.getLaboratory().getName());
    assertNull(userWithPassword.password);
  }

  @Test
  public void save_NewAdmin() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.admin.setValue(true);
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertTrue(userWithPassword.user.isAdmin());
    assertNull(userWithPassword.user.getLaboratory());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateAdmin() {
    User user = userRepository.findById(1L).get();
    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertTrue(userWithPassword.user.isAdmin());
    assertNull(userWithPassword.user.getLaboratory());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateAdminNoPassword() {
    User user = userRepository.findById(1L).get();
    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.password.setValue("");
    dialog.passwordConfirm.setValue("");
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertTrue(userWithPassword.user.isAdmin());
    assertNull(userWithPassword.user.getLaboratory());
    assertNull(userWithPassword.password);
  }

  @Test
  @Ignore("cannot create regular users yet")
  public void save_UpdateAdmin_RemoveAdminAddManager() {
    User user = userRepository.findById(1L).get();
    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    dialog.password.setValue("");
    dialog.passwordConfirm.setValue("");
    dialog.admin.setValue(false);
    dialog.manager.setValue(true);
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(email, userWithPassword.user.getEmail());
    assertEquals(name, userWithPassword.user.getName());
    assertFalse(userWithPassword.user.isAdmin());
    assertNull(userWithPassword.user.getLaboratory().getId());
    assertEquals(laboratoryName, userWithPassword.user.getLaboratory().getName());
    assertNull(userWithPassword.password);
  }
}
