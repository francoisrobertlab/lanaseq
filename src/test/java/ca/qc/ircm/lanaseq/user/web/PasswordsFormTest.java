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

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.SpringConfiguration.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.ID;
import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.PASSWORD;
import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.PASSWORDS_NOT_MATCH;
import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.PASSWORD_CONFIRM;
import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link PasswordsForm}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PasswordsFormTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(PasswordsForm.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private PasswordsForm form;
  private Locale locale = Locale.ENGLISH;
  private String password = "test_password";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    form = new PasswordsForm();
  }

  private void fillForm() {
    form.password.setValue(password);
    form.passwordConfirm.setValue(password);
  }

  @Test
  public void styles() {
    assertEquals(ID, form.getId().orElse(""));
    assertEquals(id(PASSWORD), form.password.getId().orElse(""));
    assertEquals(id(PASSWORD_CONFIRM), form.passwordConfirm.getId().orElse(""));
  }

  @Test
  public void labels() {
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(form.getTranslation(MESSAGE_PREFIX + PASSWORD), form.password.getLabel());
    assertEquals(form.getTranslation(MESSAGE_PREFIX + PASSWORD_CONFIRM),
        form.passwordConfirm.getLabel());
  }

  @Test
  public void localeChange() {
    form.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(form.getTranslation(MESSAGE_PREFIX + PASSWORD), form.password.getLabel());
    assertEquals(form.getTranslation(MESSAGE_PREFIX + PASSWORD_CONFIRM),
        form.passwordConfirm.getLabel());
  }

  @Test
  public void getPassword() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();

    assertEquals(password, form.getPassword());
  }

  @Test
  public void getPassword_Empty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.password.setValue("");
    form.passwordConfirm.setValue("");

    assertNull(form.getPassword());
  }

  @Test
  public void required_Default() {
    form.localeChange(mock(LocaleChangeEvent.class));
    assertFalse(form.isRequired());
    assertFalse(form.password.isRequiredIndicatorVisible());
    assertFalse(form.passwordConfirm.isRequiredIndicatorVisible());
  }

  @Test
  public void required_False() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(true);
    form.setRequired(false);
    assertFalse(form.isRequired());
    assertFalse(form.password.isRequiredIndicatorVisible());
    assertFalse(form.passwordConfirm.isRequiredIndicatorVisible());
  }

  @Test
  public void required_True() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(true);
    assertTrue(form.isRequired());
    assertTrue(form.password.isRequiredIndicatorVisible());
    assertTrue(form.passwordConfirm.isRequiredIndicatorVisible());
  }

  @Test
  public void validate_PasswordEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.password.setValue("");

    BinderValidationStatus<Passwords> status = form.validate();

    assertTrue(status.isOk());
  }

  @Test
  public void validate_RequiredPasswordEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(true);
    fillForm();
    form.password.setValue("");

    BinderValidationStatus<Passwords> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.password);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(form.getTranslation(CONSTANTS_PREFIX + REQUIRED)), error.getMessage());
  }

  @Test
  public void validate_RequiredBeforeLocaleChangePasswordEmpty() {
    form.setRequired(true);
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.password.setValue("");

    BinderValidationStatus<Passwords> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.password);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(form.getTranslation(CONSTANTS_PREFIX + REQUIRED)), error.getMessage());
  }

  @Test
  public void validate_PasswordsNotMatch() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.password.setValue("test");
    form.passwordConfirm.setValue("test2");

    BinderValidationStatus<Passwords> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.password);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(form.getTranslation(MESSAGE_PREFIX + PASSWORDS_NOT_MATCH)),
        error.getMessage());
  }

  @Test
  public void validate_PasswordConfirmEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.passwordConfirm.setValue("");

    BinderValidationStatus<Passwords> status = form.validate();

    assertTrue(status.isOk());
  }

  @Test
  public void validate_RequiredPasswordConfirmEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(true);
    fillForm();
    form.passwordConfirm.setValue("");

    BinderValidationStatus<Passwords> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.passwordConfirm);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(form.getTranslation(CONSTANTS_PREFIX + REQUIRED)), error.getMessage());
  }

  @Test
  public void isValid_PasswordEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.password.setValue("");

    boolean valid = form.isValid();

    assertTrue(valid);
  }

  @Test
  public void isValid_RequiredPasswordEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(true);
    fillForm();
    form.password.setValue("");

    boolean valid = form.isValid();

    assertFalse(valid);
  }

  @Test
  public void isValid_RequiredBeforeLocaleChangePasswordEmpty() {
    form.setRequired(true);
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.password.setValue("");

    boolean valid = form.isValid();

    assertFalse(valid);
  }

  @Test
  public void isValid_PasswordsNotMatch() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.password.setValue("test");
    form.passwordConfirm.setValue("test2");

    boolean valid = form.isValid();

    assertFalse(valid);
  }

  @Test
  public void isValid_PasswordConfirmEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.passwordConfirm.setValue("");

    boolean valid = form.isValid();

    assertTrue(valid);
  }

  @Test
  public void isValid_RequiredPasswordConfirmEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(true);
    fillForm();
    form.passwordConfirm.setValue("");

    boolean valid = form.isValid();

    assertFalse(valid);
  }
}
