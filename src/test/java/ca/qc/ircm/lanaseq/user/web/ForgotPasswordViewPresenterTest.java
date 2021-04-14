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

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.INVALID_EMAIL;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.SAVED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.user.ForgotPasswordWebContext;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

@ServiceTestAnnotations
public class ForgotPasswordViewPresenterTest extends AbstractViewTestCase {
  private ForgotPasswordViewPresenter presenter;
  @Mock
  private ForgotPasswordView view;
  @Mock
  private ForgotPasswordService service;
  @Mock
  private UserService userService;
  @Captor
  private ArgumentCaptor<ForgotPasswordWebContext> webContextCaptor;
  private Locale locale = ENGLISH;
  private AppResources resources = new AppResources(ForgotPasswordView.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private String email = "test@ircm.qc.ca";
  private ForgotPassword forgotPassword;
  private long id = 34925;
  private String confirmNumber = "feafet23ts";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    presenter = new ForgotPasswordViewPresenter(service, userService);
    view.header = new H2();
    view.message = new Div();
    view.email = new TextField();
    view.buttonsLayout = new HorizontalLayout();
    view.save = new Button();
    forgotPassword = new ForgotPassword();
    forgotPassword.setId(id);
    forgotPassword.setConfirmNumber(confirmNumber);
    when(service.insert(any(), any())).thenReturn(forgotPassword);
    presenter.init(view);
    presenter.localeChange(locale);
  }

  private void setFields() {
    view.email.setValue(email);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void save_EmailEmtpy() {
    view.email.setValue("");

    presenter.save();

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, view.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).insert(any(), any());
    verify(ui, never()).navigate(any(Class.class));
    verify(view, never()).showNotification(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void save_EmailInvalid() {
    view.email.setValue("test");

    presenter.save();

    BinderValidationStatus<User> status = presenter.validateUser();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, view.email);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(INVALID_EMAIL)), error.getMessage());
    verify(service, never()).insert(any(), any());
    verify(ui, never()).navigate(any(Class.class));
    verify(view, never()).showNotification(any());
  }

  @Test
  public void save_EmailNotExists() {
    setFields();

    presenter.save();

    verify(userService).exists(email);
    verify(service, never()).insert(any(), any());
    verify(ui).navigate(SigninView.class);
    verify(view).showNotification(resources.message(SAVED, email));
  }

  @Test
  public void save() {
    String viewUrl = "/usefp";
    when(userService.exists(any())).thenReturn(true);
    when(view.getUrl(any())).thenReturn(viewUrl);
    setFields();

    presenter.save();

    verify(userService).exists(email);
    verify(service).insert(eq(email), webContextCaptor.capture());
    ForgotPasswordWebContext webContext = webContextCaptor.getValue();
    String url = webContext.getChangeForgottenPasswordUrl(forgotPassword, locale);
    assertEquals(viewUrl + "/" + id + UseForgotPasswordView.SEPARATOR + confirmNumber, url);
    verify(ui).navigate(SigninView.class);
    verify(view).showNotification(resources.message(SAVED, email));
  }
}
