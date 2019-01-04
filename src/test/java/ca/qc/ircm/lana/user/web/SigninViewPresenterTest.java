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

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lana.user.web.SigninView.DISABLED;
import static ca.qc.ircm.lana.user.web.SigninView.EXCESSIVE_ATTEMPTS;
import static ca.qc.ircm.lana.user.web.SigninView.FAIL;
import static ca.qc.ircm.lana.web.WebConstants.INVALID_EMAIL;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.LdapConfiguration;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class SigninViewPresenterTest extends AbstractViewTestCase {
  private SigninViewPresenter presenter;
  @Mock
  private SigninView view;
  @Mock
  private LdapConfiguration ldapConfiguration;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(SigninView.class, locale);
  private MessageResource generalResources = new MessageResource(WebConstants.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new SigninViewPresenter(ldapConfiguration);
    view.header = new H2();
    view.email = new TextField();
    view.password = new PasswordField();
    view.signin = new Button();
    view.doSignin = mock(Button.class);
    view.error = new Div();
  }

  @Test
  public void error_NotVisible() {
    presenter.init(view);
    assertFalse(view.error.isVisible());
  }

  @Test
  public void sign_EmptyEmail() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.password.setValue("test");
    clickButton(view.signin);

    verifyZeroInteractions(view.doSignin);
    // Necessary because required status is not updated immediately.
    BinderValidationStatus<User> statuses = presenter.validate();
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(statuses, view.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(generalResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void sign_InvalidEmail() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.email.setValue("test");
    view.password.setValue("test");
    clickButton(view.signin);

    verifyZeroInteractions(view.doSignin);
    assertEquals(generalResources.message(INVALID_EMAIL), view.email.getErrorMessage());
  }

  @Test
  public void sign_InvalidEmailWithLdap() {
    when(ldapConfiguration.isEnabled()).thenReturn(true);
    presenter.init(view);
    presenter.localeChange(locale);
    view.email.setValue("test");
    view.password.setValue("test_password");
    clickButton(view.signin);

    verify(view.doSignin).click();
  }

  @Test
  public void sign_EmptyPassword() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.email.setValue("test");
    clickButton(view.signin);

    verifyZeroInteractions(view.doSignin);
    // Necessary because required status is not updated immediately.
    BinderValidationStatus<User> statuses = presenter.validate();
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(statuses, view.password);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(generalResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void sign() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.email.setValue("test@ircm.qc.ca");
    view.password.setValue("test_password");
    clickButton(view.signin);

    verify(view.doSignin).click();
  }

  @Test
  public void showError_Disabled() {
    presenter.init(view);
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put(DISABLED, Collections.emptyList());

    presenter.showError(parameters, locale);

    assertTrue(view.error.isVisible());
    assertEquals(resources.message(DISABLED), view.error.getText());
  }

  @Test
  public void showError_ExcessiveAttempts() {
    presenter.init(view);
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put(EXCESSIVE_ATTEMPTS, Collections.emptyList());

    presenter.showError(parameters, locale);

    assertTrue(view.error.isVisible());
    assertEquals(resources.message(EXCESSIVE_ATTEMPTS), view.error.getText());
  }

  @Test
  public void showError_Fail() {
    presenter.init(view);
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put(FAIL, Collections.emptyList());

    presenter.showError(parameters, locale);

    assertTrue(view.error.isVisible());
    assertEquals(resources.message(FAIL), view.error.getText());
  }
}
