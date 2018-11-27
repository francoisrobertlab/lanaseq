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

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.web.UserDialog.CLASS_NAME;
import static ca.qc.ircm.lana.user.web.UserDialog.HEADER;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.web.PasswordForm.Passwords;
import ca.qc.ircm.lana.web.SaveEvent;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import javax.inject.Inject;
import org.junit.Before;
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
  private BinderValidationStatus<User> userValidationStatus;
  @Mock
  private BinderValidationStatus<Passwords> passwordValidationStatus;
  @Mock
  private BinderValidationStatus<Laboratory> laboratoryValidationStatus;
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
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new UserDialog();
    dialog.userForm = mock(UserForm.class);
    dialog.passwordForm = mock(PasswordForm.class);
    dialog.laboratoryForm = mock(LaboratoryForm.class);
  }

  @Test
  public void styles() {
    assertEquals(CLASS_NAME, dialog.getId().orElse(""));
    assertTrue(dialog.header.getClassNames().contains(HEADER));
    assertTrue(dialog.laboratoryHeader.getClassNames().contains(LABORATORY));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertEquals(PRIMARY, dialog.save.getElement().getAttribute(THEME));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(LABORATORY), dialog.laboratoryHeader.getText());
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
    final MessageResource webResources = new MessageResource(WebConstants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(LABORATORY), dialog.laboratoryHeader.getText());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
  }

  @Test
  public void isReadOnly_Default() {
    assertFalse(dialog.isReadOnly());
  }

  @Test
  public void isReadOnly_False() {
    dialog.setReadOnly(false);
    assertFalse(dialog.isReadOnly());
  }

  @Test
  public void isReadOnly_True() {
    dialog.setReadOnly(true);
    assertTrue(dialog.isReadOnly());
  }

  @Test
  public void setReadOnly_False() {
    dialog.setReadOnly(false);
    verify(dialog.userForm).setReadOnly(false);
    verify(dialog.laboratoryForm).setReadOnly(false);
    verify(dialog.passwordForm).setVisible(true);
    assertTrue(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setReadOnly_True() {
    dialog.setReadOnly(true);
    verify(dialog.userForm).setReadOnly(true);
    verify(dialog.laboratoryForm).setReadOnly(true);
    verify(dialog.passwordForm).setVisible(false);
    assertFalse(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void getUser() {
    User user = new User();
    dialog.setUser(user);
    assertEquals(user, dialog.getUser());
  }

  @Test
  public void setUser_Never() {
    dialog = new UserDialog();
    dialog.localeChange(mock(LocaleChangeEvent.class));

    assertTrue(dialog.passwordForm.isRequired());
    assertTrue(dialog.laboratoryLayout.isVisible());
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setUser_NewUser() {
    User user = new User();
    Laboratory laboratory = new Laboratory();
    user.setLaboratory(laboratory);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(dialog.userForm).setUser(user);
    verify(dialog.passwordForm).setRequired(true);
    verify(dialog.laboratoryForm).setLaboratory(laboratory);
    assertTrue(dialog.laboratoryLayout.isVisible());
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setUser_User() {
    User user = userRepository.findById(2L).get();

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(dialog.userForm).setUser(user);
    verify(dialog.passwordForm).setRequired(false);
    verify(dialog.laboratoryForm).setLaboratory(user.getLaboratory());
    assertTrue(dialog.laboratoryLayout.isVisible());
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_NewAdmin() {
    User user = new User();
    user.setAdmin(true);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(dialog.userForm).setUser(user);
    verify(dialog.passwordForm).setRequired(true);
    verify(dialog.laboratoryForm).setLaboratory(null);
    assertFalse(dialog.laboratoryLayout.isVisible());
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setUser_Admin() {
    User user = userRepository.findById(1L).get();

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(dialog.userForm).setUser(user);
    verify(dialog.passwordForm).setRequired(false);
    verify(dialog.laboratoryForm).setLaboratory(null);
    assertFalse(dialog.laboratoryLayout.isVisible());
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_UserBeforeLocaleChange() {
    User user = userRepository.findById(2L).get();

    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(dialog.userForm).setUser(user);
    verify(dialog.passwordForm).setRequired(false);
    verify(dialog.laboratoryForm).setLaboratory(user.getLaboratory());
    assertTrue(dialog.laboratoryLayout.isVisible());
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(null);

    verify(dialog.userForm).setUser(null);
    verify(dialog.passwordForm).setRequired(true);
    verify(dialog.laboratoryForm).setLaboratory(null);
    assertTrue(dialog.laboratoryLayout.isVisible());
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void save_UserValidationFails() {
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(false);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_PasswordValidationFails() {
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(passwordValidationStatus.isOk()).thenReturn(false);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_LaboratoryValidationFails() {
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(false);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_AdminLaboratoryValidationFails() {
    User user = new User();
    user.setAdmin(true);
    dialog.setUser(user);
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(false);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(any());
  }

  @Test
  public void save_NewUser() {
    User user = new User();
    Laboratory laboratory = new Laboratory();
    user.setLaboratory(laboratory);
    dialog.setUser(user);
    String password = "test_password";
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(dialog.passwordForm.getPassword()).thenReturn(password);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(user, userWithPassword.user);
    assertEquals(laboratory, userWithPassword.user.getLaboratory());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateUser() {
    User user = userRepository.findById(2L).get();
    dialog.setUser(user);
    String password = "test_password";
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(dialog.passwordForm.getPassword()).thenReturn(password);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(user, userWithPassword.user);
    assertEquals(user.getLaboratory(), userWithPassword.user.getLaboratory());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateUserNoPassword() {
    User user = userRepository.findById(2L).get();
    dialog.setUser(user);
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(dialog.passwordForm.getPassword()).thenReturn(null);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(user, userWithPassword.user);
    assertEquals(user.getLaboratory(), userWithPassword.user.getLaboratory());
    assertNull(userWithPassword.password);
  }

  @Test
  public void save_NewAdmin() {
    User user = new User();
    user.setAdmin(true);
    dialog.setUser(user);
    String password = "test_password";
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(dialog.passwordForm.getPassword()).thenReturn(password);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(user, userWithPassword.user);
    assertEquals(null, userWithPassword.user.getLaboratory());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateAdmin() {
    User user = userRepository.findById(1L).get();
    dialog.setUser(user);
    String password = "test_password";
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(dialog.passwordForm.getPassword()).thenReturn(password);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(user, userWithPassword.user);
    assertEquals(null, userWithPassword.user.getLaboratory());
    assertEquals(password, userWithPassword.password);
  }

  @Test
  public void save_UpdateAdminNoPassword() {
    User user = userRepository.findById(1L).get();
    dialog.setUser(user);
    when(dialog.userForm.validate()).thenReturn(userValidationStatus);
    when(userValidationStatus.isOk()).thenReturn(true);
    when(dialog.passwordForm.validate()).thenReturn(passwordValidationStatus);
    when(dialog.passwordForm.getPassword()).thenReturn(null);
    when(passwordValidationStatus.isOk()).thenReturn(true);
    when(dialog.laboratoryForm.validate()).thenReturn(laboratoryValidationStatus);
    when(laboratoryValidationStatus.isOk()).thenReturn(true);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.addSaveListener(saveListener);

    dialog.fireClickSave();

    verify(saveListener).onComponentEvent(saveEventCaptor.capture());
    UserWithPassword userWithPassword = saveEventCaptor.getValue().getSavedObject();
    assertEquals(user, userWithPassword.user);
    assertEquals(null, userWithPassword.user.getLaboratory());
    assertEquals(null, userWithPassword.password);
  }
}
