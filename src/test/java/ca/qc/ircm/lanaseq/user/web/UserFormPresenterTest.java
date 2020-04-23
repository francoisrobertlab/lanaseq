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
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryRepository;
import ca.qc.ircm.lanaseq.user.LaboratoryService;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox.CustomValueSetEvent;
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
public class UserFormPresenterTest extends AbstractViewTestCase {
  private UserFormPresenter presenter;
  @Mock
  private UserForm form;
  @Mock
  private LaboratoryService laboratoryService;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private BinderValidationStatus<Passwords> passwordsValidationStatus;
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
    presenter = new UserFormPresenter(laboratoryService, authorizationService);
    form.email = new TextField();
    form.name = new TextField();
    form.admin = new Checkbox();
    form.manager = new Checkbox();
    form.passwords = mock(PasswordsForm.class);
    form.laboratory = new ComboBox<>();
    form.createNewLaboratory = new Checkbox();
    form.newLaboratoryName = new TextField();
    currentUser = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
    laboratories = laboratoryRepository.findAll();
    when(laboratoryService.all()).thenReturn(laboratories);
    when(laboratoryService.get(any()))
        .thenAnswer(i -> laboratoryRepository.findById(i.getArgument(0)).orElse(null));
    laboratory = laboratoryRepository.findById(2L).orElse(null);
    when(form.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(true);
  }

  private void fillForm() {
    form.email.setValue(email);
    form.name.setValue(name);
    if (!items(form.laboratory).isEmpty()) {
      form.laboratory.setValue(laboratory);
    }
    form.newLaboratoryName.setValue(newLaboratoryName);
  }

  @Test
  public void currentUser_User() {
    presenter.init(form);
    presenter.localeChange(locale);
    assertFalse(form.admin.isVisible());
    assertFalse(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertTrue(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertFalse(form.createNewLaboratory.isVisible());
    assertFalse(form.newLaboratoryName.isVisible());
  }

  @Test
  public void currentUser_Manager() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    assertFalse(form.admin.isVisible());
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertTrue(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertFalse(form.createNewLaboratory.isVisible());
    assertFalse(form.newLaboratoryName.isVisible());
  }

  @Test
  public void currentUser_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    assertTrue(form.admin.isVisible());
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertFalse(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertTrue(form.createNewLaboratory.isVisible());
    assertFalse(form.createNewLaboratory.isEnabled());
    assertTrue(form.newLaboratoryName.isVisible());
    assertFalse(form.newLaboratoryName.isEnabled());
  }

  @Test
  public void laboratory() {
    presenter.init(form);
    presenter.localeChange(locale);
    assertFalse(form.laboratory.isAllowCustomValue());
    assertTrue(form.laboratory.isRequiredIndicatorVisible());
    List<Laboratory> values = items(form.laboratory);
    assertEquals(1, values.size());
    assertEquals(currentUser.getLaboratory(), values.get(0));
    assertEquals(laboratory.getName(), form.laboratory.getItemLabelGenerator().apply(laboratory));
  }

  @Test
  public void laboratory_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    assertFalse(form.laboratory.isAllowCustomValue());
    assertTrue(form.laboratory.isRequiredIndicatorVisible());
    List<Laboratory> values = items(form.laboratory);
    assertEquals(laboratories.size(), values.size());
    for (Laboratory laboratory : laboratories) {
      assertTrue(values.contains(laboratory));
      assertEquals(laboratory.getName(), form.laboratory.getItemLabelGenerator().apply(laboratory));
    }
  }

  @Test
  public void checkAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    form.admin.setValue(true);
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertFalse(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertTrue(form.createNewLaboratory.isVisible());
    assertFalse(form.createNewLaboratory.isEnabled());
    assertTrue(form.newLaboratoryName.isVisible());
    assertFalse(form.newLaboratoryName.isEnabled());
  }

  @Test
  public void uncheckAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    form.admin.setValue(true);
    form.admin.setValue(false);
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertFalse(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertTrue(form.createNewLaboratory.isVisible());
    assertFalse(form.createNewLaboratory.isEnabled());
    assertTrue(form.newLaboratoryName.isVisible());
    assertFalse(form.newLaboratoryName.isEnabled());
  }

  @Test
  public void checkManager_Manager() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    form.manager.setValue(true);
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertTrue(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertFalse(form.createNewLaboratory.isVisible());
    assertFalse(form.createNewLaboratory.isEnabled());
    assertFalse(form.newLaboratoryName.isVisible());
    assertFalse(form.newLaboratoryName.isEnabled());
  }

  @Test
  public void checkManager_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    form.manager.setValue(true);
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertFalse(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertTrue(form.createNewLaboratory.isVisible());
    assertTrue(form.createNewLaboratory.isEnabled());
    assertTrue(form.newLaboratoryName.isVisible());
    assertFalse(form.newLaboratoryName.isEnabled());
  }

  @Test
  public void checkManagerAndCheckCreateNewLaboratory_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    form.manager.setValue(true);
    form.createNewLaboratory.setValue(true);
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertFalse(form.laboratory.isReadOnly());
    assertFalse(form.laboratory.isEnabled());
    assertTrue(form.createNewLaboratory.isVisible());
    assertTrue(form.createNewLaboratory.isEnabled());
    assertTrue(form.newLaboratoryName.isVisible());
    assertTrue(form.newLaboratoryName.isEnabled());
  }

  @Test
  public void uncheckManager_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    form.manager.setValue(true);
    form.manager.setValue(false);
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertFalse(form.laboratory.isReadOnly());
    assertTrue(form.laboratory.isEnabled());
    assertTrue(form.createNewLaboratory.isVisible());
    assertFalse(form.createNewLaboratory.isEnabled());
    assertTrue(form.newLaboratoryName.isVisible());
    assertFalse(form.newLaboratoryName.isEnabled());
  }

  @Test
  public void uncheckManagerAndCheckCreateNewLaboratory_Admin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    form.manager.setValue(true);
    form.createNewLaboratory.setValue(true);
    form.manager.setValue(false);
    assertTrue(form.manager.isVisible());
    assertTrue(form.laboratory.isVisible());
    assertTrue(form.laboratory.isEnabled());
    assertTrue(form.createNewLaboratory.isVisible());
    assertFalse(form.createNewLaboratory.isEnabled());
    assertFalse(form.createNewLaboratory.getValue());
    assertTrue(form.newLaboratoryName.isVisible());
    assertFalse(form.newLaboratoryName.isEnabled());
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
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
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
    assertEquals(laboratory.getId(), form.laboratory.getValue().getId());
    assertTrue(form.laboratory.isReadOnly());
  }

  @Test
  public void setUser_NewUserAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
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
    assertEquals((Long) 1L, form.laboratory.getValue().getId());
    assertFalse(form.laboratory.isReadOnly());
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
    assertEquals(user.getLaboratory().getId(), form.laboratory.getValue().getId());
    assertTrue(form.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserCanWrite() {
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);

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
    assertEquals(user.getLaboratory().getId(), form.laboratory.getValue().getId());
    assertTrue(form.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    System.out.println(user.getLaboratory());

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
    System.out.println(user.getLaboratory());
    System.out.println(form.laboratory.getValue());
    assertEquals(user.getLaboratory().getId(), form.laboratory.getValue().getId());
    assertFalse(form.laboratory.isReadOnly());
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
    assertEquals(user.getLaboratory().getId(), form.laboratory.getValue().getId());
    assertTrue(form.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserCanWriteBeforeLocaleChange() {
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);

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
    assertEquals(user.getLaboratory().getId(), form.laboratory.getValue().getId());
    assertTrue(form.laboratory.isReadOnly());
  }

  @Test
  public void setUser_UserAdminBeforeLocaleChange() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
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
    assertEquals(user.getLaboratory().getId(), form.laboratory.getValue().getId());
    assertFalse(form.laboratory.isReadOnly());
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
    assertEquals(laboratory.getId(), form.laboratory.getValue().getId());
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
  public void isValid_LaboratoryEmpty() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(laboratoryService.all()).thenReturn(new ArrayList<>());
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();

    assertFalse(presenter.isValid());

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.laboratory);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void isValid_AdminLaboratoryEmpty() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(laboratoryService.all()).thenReturn(new ArrayList<>());
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.admin.setValue(true);

    assertFalse(presenter.isValid());

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.laboratory);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void isValid_NewLaboratoryNameEmpty() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.manager.setValue(true);
    form.createNewLaboratory.setValue(true);
    form.newLaboratoryName.setValue("");

    assertFalse(presenter.isValid());

    BinderValidationStatus<Laboratory> status = presenter.validateLaboratory();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.newLaboratoryName);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
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
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
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
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
  }

  @Test
  public void isValid_NewManagerNewLaboratory() {
    when(form.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    presenter.localeChange(locale);
    fillForm();
    form.manager.setValue(true);
    form.createNewLaboratory.setValue(true);

    assertTrue(presenter.isValid());

    User user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNotNull(user.getLaboratory());
    assertNull(user.getLaboratory().getId());
    assertEquals(newLaboratoryName, user.getLaboratory().getName());
  }

  @Test
  public void isValid_UpdateUser() {
    when(form.passwords.getPassword()).thenReturn(password);
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
  }

  @Test
  public void isValid_UpdateUserLaboratory() {
    when(form.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(6L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
  }

  @Test
  public void isValid_UpdateUserNoPassword() {
    presenter.init(form);
    User user = userRepository.findById(2L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertTrue(user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
  }

  @Test
  public void isValid_NewAdmin() {
    when(form.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
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
    assertNotNull(user.getLaboratory());
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
  }

  @Test
  public void isValid_UpdateAdmin() {
    when(form.passwords.getPassword()).thenReturn(password);
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(1L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals((Long) 2L, user.getLaboratory().getId());
  }

  @Test
  public void isValid_UpdateAdminNoPassword() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(1L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.setUser(user);
    presenter.localeChange(locale);
    fillForm();

    assertTrue(presenter.isValid());

    user = presenter.getUser();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(user.isAdmin());
    assertTrue(user.isManager());
    assertEquals((Long) 2L, user.getLaboratory().getId());
  }

  @Test
  public void isValid_UpdateAdmin_RemoveAdminAddManager() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);
    when(authorizationService.hasRole(any())).thenReturn(true);
    presenter.init(form);
    User user = userRepository.findById(1L).get();
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
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
    assertEquals(laboratory.getId(), user.getLaboratory().getId());
  }
}
