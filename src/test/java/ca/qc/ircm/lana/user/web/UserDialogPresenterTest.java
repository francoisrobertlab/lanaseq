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

import static ca.qc.ircm.lana.Constants.INVALID_EMAIL;
import static ca.qc.ircm.lana.Constants.REQUIRED;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.Constants;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.LaboratoryService;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox.CustomValueSetEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserDialogPresenterTest extends AbstractViewTestCase {
  private UserDialogPresenter presenter;
  @Mock
  private UserDialog dialog;
  @Mock
  private UserService userService;
  @Mock
  private LaboratoryService laboratoryService;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private BinderValidationStatus<Passwords> passwordsValidationStatus;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;
  @Captor
  @SuppressWarnings("checkstyle:linelength")
  private ArgumentCaptor<ComponentEventListener<CustomValueSetEvent<ComboBox<Laboratory>>>> laboratoryComponentEventListenerCaptor;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private LaboratoryRepository laboratoryRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources webResources = new AppResources(Constants.class, locale);
  private String email = "test@ircm.qc.ca";
  private String name = "Test User";
  private String password = "test_password";
  private String newLaboratoryName = "New Test Laboratory";
  private User currentUser;
  private List<Laboratory> laboratories;
  private Laboratory laboratory;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new UserDialogPresenter(userService, laboratoryService, authorizationService);
    dialog.header = new H2();
    dialog.email = new TextField();
    dialog.name = new TextField();
    dialog.admin = new Checkbox();
    dialog.manager = new Checkbox();
    dialog.createNewLaboratory = new Checkbox();
    dialog.passwords = mock(PasswordsForm.class);
    dialog.laboratory = new ComboBox<>();
    dialog.newLaboratoryLayout = new VerticalLayout();
    dialog.newLaboratoryHeader = new H6();
    dialog.newLaboratoryName = new TextField();
    dialog.buttonsLayout = new HorizontalLayout();
    dialog.save = new Button();
    dialog.cancel = new Button();
    currentUser = userRepository.findById(2L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(currentUser);
    laboratories = laboratoryRepository.findAll();
    when(laboratoryService.all()).thenReturn(laboratories);
    laboratory = laboratoryRepository.findById(2L).orElse(null);
    when(dialog.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(true);
  }

  private void fillForm() {
    dialog.email.setValue(email);
    dialog.name.setValue(name);
    if (!items(dialog.laboratory).isEmpty()) {
      dialog.laboratory.setValue(laboratory);
    }
    dialog.newLaboratoryName.setValue(newLaboratoryName);
  }

  @Test
  public void currentUser_User() {
    presenter.init(dialog);
    presenter.localeChange(locale);
    assertFalse(dialog.admin.isVisible());
    assertFalse(dialog.manager.isVisible());
    assertFalse(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.laboratory.isVisible());
    assertTrue(dialog.laboratory.isReadOnly());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void currentUser_Manager() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    assertFalse(dialog.admin.isVisible());
    assertTrue(dialog.manager.isVisible());
    assertFalse(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.laboratory.isVisible());
    assertTrue(dialog.laboratory.isReadOnly());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void currentUser_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    assertTrue(dialog.admin.isVisible());
    assertTrue(dialog.manager.isVisible());
    assertTrue(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.createNewLaboratory.isReadOnly());
    assertTrue(dialog.laboratory.isVisible());
    assertFalse(dialog.laboratory.isReadOnly());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void laboratory() {
    presenter.init(dialog);
    presenter.localeChange(locale);
    assertFalse(dialog.laboratory.isAllowCustomValue());
    List<Laboratory> values = items(dialog.laboratory);
    assertEquals(1, values.size());
    assertEquals(currentUser.getLaboratory(), values.get(0));
    assertEquals(laboratory.getName(), dialog.laboratory.getItemLabelGenerator().apply(laboratory));
  }

  @Test
  public void laboratory_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    assertFalse(dialog.laboratory.isAllowCustomValue());
    List<Laboratory> values = items(dialog.laboratory);
    assertEquals(laboratories.size(), values.size());
    for (Laboratory laboratory : laboratories) {
      assertTrue(values.contains(laboratory));
      assertEquals(laboratory.getName(),
          dialog.laboratory.getItemLabelGenerator().apply(laboratory));
    }
  }

  @Test
  public void checkAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    dialog.admin.setValue(true);
    assertTrue(dialog.manager.isVisible());
    assertTrue(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.laboratory.isVisible());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void uncheckAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    dialog.admin.setValue(true);
    dialog.admin.setValue(false);
    assertTrue(dialog.manager.isVisible());
    assertTrue(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.laboratory.isVisible());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void checkManager_Manager() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    dialog.manager.setValue(true);
    assertFalse(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.laboratory.isVisible());
    assertTrue(dialog.laboratory.isReadOnly());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void checkManager_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    dialog.manager.setValue(true);
    assertTrue(dialog.createNewLaboratory.isVisible());
    assertFalse(dialog.createNewLaboratory.isReadOnly());
    assertTrue(dialog.laboratory.isVisible());
    assertFalse(dialog.laboratory.isReadOnly());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void checkManagerAndCheckCreateNewLaboratory_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    dialog.manager.setValue(true);
    dialog.createNewLaboratory.setValue(true);
    assertTrue(dialog.createNewLaboratory.isVisible());
    assertFalse(dialog.createNewLaboratory.isReadOnly());
    assertFalse(dialog.laboratory.isVisible());
    assertFalse(dialog.laboratory.isReadOnly());
    assertTrue(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void uncheckManager_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    dialog.manager.setValue(true);
    dialog.manager.setValue(false);
    assertTrue(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.createNewLaboratory.isReadOnly());
    assertTrue(dialog.laboratory.isVisible());
    assertFalse(dialog.laboratory.isReadOnly());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void uncheckManagerAndCheckCreateNewLaboratory_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    dialog.manager.setValue(true);
    dialog.createNewLaboratory.setValue(true);
    dialog.manager.setValue(false);
    assertTrue(dialog.createNewLaboratory.isVisible());
    assertTrue(dialog.createNewLaboratory.isReadOnly());
    assertTrue(dialog.laboratory.isVisible());
    assertFalse(dialog.laboratory.isReadOnly());
    assertFalse(dialog.newLaboratoryLayout.isVisible());
  }

  @Test
  public void getUser() {
    presenter.init(dialog);
    User user = new User();
    presenter.setUser(user);
    assertEquals(user, presenter.getUser());
  }

  @Test
  public void setUser_NewUser() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    User user = new User();

    presenter.localeChange(locale);
    presenter.setUser(user);

    assertEquals("", dialog.email.getValue());
    assertFalse(dialog.email.isReadOnly());
    assertEquals("", dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.admin.isReadOnly());
    assertFalse(dialog.manager.getValue());
    assertFalse(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(dialog.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    assertEquals(laboratory.getId(), dialog.laboratory.getValue().getId());
    assertTrue(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_NewUserAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    User user = new User();

    presenter.localeChange(locale);
    presenter.setUser(user);

    assertEquals("", dialog.email.getValue());
    assertFalse(dialog.email.isReadOnly());
    assertEquals("", dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.admin.isReadOnly());
    assertFalse(dialog.manager.getValue());
    assertFalse(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(dialog.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    assertEquals((Long) 1L, dialog.laboratory.getValue().getId());
    assertFalse(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_User() {
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setUser(user);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertTrue(dialog.email.isReadOnly());
    assertEquals(user.getName(), dialog.name.getValue());
    assertTrue(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertTrue(dialog.admin.isReadOnly());
    assertTrue(dialog.manager.getValue());
    assertTrue(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
    assertEquals(user.getLaboratory(), dialog.laboratory.getValue());
    assertTrue(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserCanWrite() {
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);

    presenter.localeChange(locale);
    presenter.setUser(user);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertFalse(dialog.email.isReadOnly());
    assertEquals(user.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.admin.isReadOnly());
    assertTrue(dialog.manager.getValue());
    assertFalse(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(dialog.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
    assertEquals(user.getLaboratory(), dialog.laboratory.getValue());
    assertTrue(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setUser(user);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertFalse(dialog.email.isReadOnly());
    assertEquals(user.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.admin.isReadOnly());
    assertTrue(dialog.manager.getValue());
    assertFalse(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(dialog.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
    assertEquals(user.getLaboratory(), dialog.laboratory.getValue());
    assertFalse(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserBeforeLocaleChange() {
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();

    presenter.setUser(user);
    presenter.localeChange(locale);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertTrue(dialog.email.isReadOnly());
    assertEquals(user.getName(), dialog.name.getValue());
    assertTrue(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertTrue(dialog.admin.isReadOnly());
    assertTrue(dialog.manager.getValue());
    assertTrue(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
    assertEquals(user.getLaboratory(), dialog.laboratory.getValue());
    assertTrue(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserCanWriteBeforeLocaleChange() {
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);

    presenter.setUser(user);
    presenter.localeChange(locale);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertFalse(dialog.email.isReadOnly());
    assertEquals(user.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.admin.isReadOnly());
    assertTrue(dialog.manager.getValue());
    assertFalse(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(dialog.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
    assertEquals(user.getLaboratory(), dialog.laboratory.getValue());
    assertTrue(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserAdminBeforeLocaleChange() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();

    presenter.setUser(user);
    presenter.localeChange(locale);

    assertEquals(user.getEmail(), dialog.email.getValue());
    assertFalse(dialog.email.isReadOnly());
    assertEquals(user.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.admin.isReadOnly());
    assertTrue(dialog.manager.getValue());
    assertFalse(dialog.manager.isReadOnly());
    verify(dialog.passwords, atLeastOnce()).setVisible(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    verify(dialog.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());
    assertEquals(user.getLaboratory(), dialog.laboratory.getValue());
    assertFalse(dialog.laboratory.isReadOnly());
  }

  @Test
  public void setUser_Null() {
    presenter.init(dialog);
    presenter.localeChange(locale);
    presenter.setUser(null);

    assertEquals("", dialog.email.getValue());
    assertEquals("", dialog.name.getValue());
    assertFalse(dialog.admin.getValue());
    assertFalse(dialog.manager.getValue());
    verify(dialog.passwords, atLeastOnce()).setRequired(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
    assertEquals(laboratory.getId(), dialog.laboratory.getValue().getId());
  }

  @Test
  public void save_EmailEmpty() {
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.email.setValue("");

    presenter.save();

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_EmailInvalid() {
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.email.setValue("test");

    presenter.save();

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(INVALID_EMAIL)), error.getMessage());
    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NameEmpty() {
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.name.setValue("");

    presenter.save();

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_PasswordValidationFailed() {
    when(passwordsValidationStatus.isOk()).thenReturn(false);
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_LaboratoryEmpty() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(laboratoryService.all()).thenReturn(new ArrayList<>());
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.laboratory);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_AdminLaboratoryEmpty() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(laboratoryService.all()).thenReturn(new ArrayList<>());
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.admin.setValue(true);

    presenter.save();

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.laboratory);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NewLaboratoryNameEmpty() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.manager.setValue(true);
    dialog.createNewLaboratory.setValue(true);
    dialog.newLaboratoryName.setValue("");

    presenter.save();

    BinderValidationStatus<Laboratory> status = presenter.validateLaboratory();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.newLaboratoryName);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NewUser() {
    when(dialog.passwords.getPassword()).thenReturn(password);
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(password));
    User user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NewManager() {
    when(dialog.passwords.getPassword()).thenReturn(password);
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.manager.setValue(true);

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(password));
    User user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NewManagerNewLaboratory() {
    when(dialog.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.manager.setValue(true);
    dialog.createNewLaboratory.setValue(true);

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(password));
    User user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNotNull(user.getLaboratory());
    assertNull(user.getLaboratory().getId());
    assertEquals(newLaboratoryName, user.getLaboratory().getName());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateUser() {
    when(dialog.passwords.getPassword()).thenReturn(password);
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();
    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(password));
    user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateUserLaboratory() {
    when(dialog.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    User user = userRepository.findById(6L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();
    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(password));
    user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateUserNoPassword() {
    presenter.init(dialog);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(null));
    user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NewAdmin() {
    when(dialog.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.admin.setValue(true);

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(password));
    User user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertFalse(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateAdmin() {
    when(dialog.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    User user = userRepository.findById(1L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(password));
    user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals((Long) 2L, user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateAdminNoPassword() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    User user = userRepository.findById(1L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(null));
    user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals((Long) 2L, user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateAdmin_RemoveAdminAddManager() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(dialog);
    User user = userRepository.findById(1L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();
    dialog.admin.setValue(false);
    dialog.manager.setValue(true);

    presenter.save();

    verify(userService).save(userCaptor.capture(), eq(null));
    user = userCaptor.getValue();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.cancel();

    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
  }
}
