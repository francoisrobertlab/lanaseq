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
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.user.web.UserForm.CLASS_NAME;
import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserFormTest extends AbstractViewTestCase {
  private UserForm form;
  private Locale locale = Locale.ENGLISH;
  private MessageResource userResources = new MessageResource(User.class, locale);
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  @Inject
  private UserRepository userRepository;
  private String email = "test@ircm.qc.ca";
  private String name = "Test User";

  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    form = new UserForm();
  }

  private void fillForm() {
    form.email.setValue(email);
    form.name.setValue(name);
  }

  @Test
  public void styles() {
    assertTrue(form.getContent().getClassNames().contains(CLASS_NAME));
    assertTrue(form.email.getClassNames().contains(EMAIL));
    assertTrue(form.name.getClassNames().contains(NAME));
  }

  @Test
  public void labels() {
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(userResources.message(EMAIL), form.email.getLabel());
    assertEquals(userResources.message(NAME), form.name.getLabel());
  }

  @Test
  public void localeChange() {
    form.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource userResources = new MessageResource(User.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(userResources.message(EMAIL), form.email.getLabel());
    assertEquals(userResources.message(NAME), form.name.getLabel());
  }

  @Test
  public void isReadOnly_Default() {
    assertFalse(form.isReadOnly());
  }

  @Test
  public void isReadOnly_False() {
    form.setReadOnly(false);
    assertFalse(form.isReadOnly());
  }

  @Test
  public void isReadOnly_True() {
    form.setReadOnly(true);
    assertTrue(form.isReadOnly());
  }

  @Test
  public void setReadOnly_False() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setReadOnly(false);
    assertFalse(form.email.isReadOnly());
    assertFalse(form.name.isReadOnly());
  }

  @Test
  public void setReadOnly_FalseBeforeLocaleChange() {
    form.setReadOnly(false);
    form.localeChange(mock(LocaleChangeEvent.class));
    assertFalse(form.email.isReadOnly());
    assertFalse(form.name.isReadOnly());
  }

  @Test
  public void setReadOnly_True() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setReadOnly(true);
    assertTrue(form.email.isReadOnly());
    assertTrue(form.name.isReadOnly());
  }

  @Test
  public void setReadOnly_TrueBeforeLocaleChange() {
    form.setReadOnly(true);
    form.localeChange(mock(LocaleChangeEvent.class));
    assertTrue(form.email.isReadOnly());
    assertTrue(form.name.isReadOnly());
  }

  @Test
  public void getUser() {
    User user = userRepository.findById(2L).get();
    form.setUser(user);
    assertEquals(user, form.getUser());
  }

  @Test
  public void setUser_User() {
    User user = userRepository.findById(2L).get();

    form.localeChange(mock(LocaleChangeEvent.class));
    form.setUser(user);

    assertEquals(user.getEmail(), form.email.getValue());
    assertEquals(user.getName(), form.name.getValue());
  }

  @Test
  public void setUser_UserBeforeLocaleChange() {
    User user = userRepository.findById(2L).get();

    form.setUser(user);
    form.localeChange(mock(LocaleChangeEvent.class));

    assertEquals(user.getEmail(), form.email.getValue());
    assertEquals(user.getName(), form.name.getValue());
  }

  @Test
  public void setUser_UserUpdate() {
    User user = userRepository.findById(2L).get();

    form.localeChange(mock(LocaleChangeEvent.class));
    form.setUser(user);
    fillForm();

    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
  }

  @Test
  public void setUser_Null() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setUser(null);

    assertEquals("", form.email.getValue());
    assertEquals("", form.name.getValue());
  }

  @Test
  public void validate_EmailEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.email.setValue("");

    BinderValidationStatus<User> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void validate_EmailInvalid() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.email.setValue("test");

    BinderValidationStatus<User> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(INVALID_EMAIL)), error.getMessage());
  }

  @Test
  public void validate_NameEmpty() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();
    form.name.setValue("");

    BinderValidationStatus<User> status = form.validate();

    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, form.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void validate_Valid() {
    form.localeChange(mock(LocaleChangeEvent.class));
    fillForm();

    BinderValidationStatus<User> status = form.validate();

    assertTrue(status.isOk());
  }
}
