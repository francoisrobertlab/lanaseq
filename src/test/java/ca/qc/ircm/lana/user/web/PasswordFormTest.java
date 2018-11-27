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
import static ca.qc.ircm.lana.user.web.PasswordForm.CLASS_NAME;
import static ca.qc.ircm.lana.user.web.PasswordForm.PASSWORD;
import static ca.qc.ircm.lana.user.web.PasswordForm.PASSWORDS_NOT_MATCH;
import static ca.qc.ircm.lana.user.web.PasswordForm.PASSWORD_CONFIRM;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.web.PasswordForm.Passwords;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class PasswordFormTest extends AbstractViewTestCase {
  private PasswordForm form;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(PasswordForm.class, locale);
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  private String password = "test_password";

  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    form = new PasswordForm();
  }

  private void fillForm() {
    form.password.setValue(password);
    form.passwordConfirm.setValue(password);
  }

  @Test
  public void styles() {
    assertTrue(form.getContent().getClassNames().contains(CLASS_NAME));
    assertTrue(form.password.getClassNames().contains(PASSWORD));
    assertTrue(form.passwordConfirm.getClassNames().contains(PASSWORD_CONFIRM));
  }

  @Test
  public void labels() {
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(PASSWORD), form.password.getLabel());
    assertEquals(resources.message(PASSWORD_CONFIRM), form.passwordConfirm.getLabel());
  }

  @Test
  public void localeChange() {
    form.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources = new MessageResource(PasswordForm.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(PASSWORD), form.password.getLabel());
    assertEquals(resources.message(PASSWORD_CONFIRM), form.passwordConfirm.getLabel());
  }

  @Test
  public void isRequired_Default() {
    form.localeChange(mock(LocaleChangeEvent.class));
    assertFalse(form.isRequired());
    assertFalse(form.password.isRequiredIndicatorVisible());
    assertFalse(form.passwordConfirm.isRequiredIndicatorVisible());
  }

  @Test
  public void isRequired_False() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(false);
    assertFalse(form.password.isRequiredIndicatorVisible());
    assertFalse(form.passwordConfirm.isRequiredIndicatorVisible());
  }

  @Test
  public void isRequired_True() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(true);
    assertTrue(form.password.isRequiredIndicatorVisible());
    assertTrue(form.passwordConfirm.isRequiredIndicatorVisible());
  }

  @Test
  public void getPassword() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    assertEquals(password, form.getPassword());
  }

  @Test
  public void getPassword_Default() {
    form.localeChange(mock(LocaleChangeEvent.class));
    assertNull(form.getPassword());
  }

  @Test
  public void validate_PasswordEmpty() {
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
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void validate_PasswordsNotMatch() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.password.setValue("test");

    BinderValidationStatus<Passwords> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.password);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(resources.message(PASSWORDS_NOT_MATCH)), error.getMessage());
  }

  @Test
  public void validate_PasswordConfirmEmpty() {
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
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void validate_PasswordAndConfirmEmptyNotRequired() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setRequired(false);

    BinderValidationStatus<Passwords> status = form.validate();

    assertTrue(status.isOk());
  }

  @Test
  public void validate_Valid() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();

    BinderValidationStatus<Passwords> status = form.validate();

    assertTrue(status.isOk());
  }
}
