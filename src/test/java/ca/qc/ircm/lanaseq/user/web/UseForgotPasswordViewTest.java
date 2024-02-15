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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.ID;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.INVALID;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.MESSAGE;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.github.mvysny.kaributesting.v10.NotificationsKt;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;

/**
 * Tests for {@link UseForgotPasswordView}.
 */
@ServiceTestAnnotations
@WithAnonymousUser
public class UseForgotPasswordViewTest extends AbstractKaribuTestCase {
  private UseForgotPasswordView view;
  @MockBean
  private ForgotPasswordService service;
  @Mock
  private ForgotPassword forgotPassword;
  private Locale locale = ENGLISH;
  private AppResources resources = new AppResources(UseForgotPasswordView.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(any(), any())).thenReturn(Optional.of(forgotPassword));
    ui.setLocale(locale);
    String parameter = "2/b";
    view = ui.navigate(UseForgotPasswordView.class, parameter).get();
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(MESSAGE, view.message.getId().orElse(""));
    assertEquals(SAVE, view.save.getId().orElse(""));
    assertTrue(view.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
  }

  @Test
  public void labels() {
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(resources.message(MESSAGE), view.message.getText());
    assertEquals(webResources.message(SAVE), view.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), view.save.getIcon());
  }

  @Test
  public void localeChange() {
    Locale locale = FRENCH;
    final AppResources resources = new AppResources(UseForgotPasswordView.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(resources.message(MESSAGE), view.message.getText());
    assertEquals(webResources.message(SAVE), view.save.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void save_Invalid() {
    view.form = mock(PasswordsForm.class);

    view.save();

    verify(view.form).isValid();
    verify(service, never()).updatePassword(any(), any());
  }

  @Test
  public void save() {
    String password = "test_password";
    view.form = mock(PasswordsForm.class);
    when(view.form.isValid()).thenReturn(true);
    when(view.form.getPassword()).thenReturn(password);

    view.save();

    verify(view.form).isValid();
    verify(service).updatePassword(eq(forgotPassword), eq(password));
    assertCurrentView(SigninView.class);
    NotificationsKt.expectNotifications(resources.message(SAVED));
  }

  @Test
  public void setParameter() {
    String parameter = "34925/feafet23ts";
    view = ui.navigate(UseForgotPasswordView.class, parameter).get();
    verify(service, atLeastOnce()).get(34925L, "feafet23ts");
    assertTrue(view.save.isEnabled());
    assertTrue(view.form.isEnabled());
  }

  @Test
  public void setParameter_IdNotNumber() {
    String parameter = "A434GS";
    view = ui.navigate(UseForgotPasswordView.class, parameter).get();
    verify(service, times(2)).get(any(), any());
    NotificationsKt.expectNotifications(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }

  @Test
  public void setParameter_MissingConfirm() {
    view.form = mock(PasswordsForm.class);
    String parameter = "34925";
    view = ui.navigate(UseForgotPasswordView.class, parameter).get();
    verify(service, times(2)).get(any(), any());
    NotificationsKt.expectNotifications(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }

  @Test
  public void setParameter_NullForgotPassword() {
    when(service.get(any(Long.class), any())).thenReturn(Optional.empty());
    String parameter = "34925/feafet23ts";
    view = ui.navigate(UseForgotPasswordView.class, parameter).get();
    verify(service, atLeastOnce()).get(34925L, "feafet23ts");
    NotificationsKt.expectNotifications(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }

  @Test
  public void setParameter_Null() {
    view = ui.navigate(UseForgotPasswordView.class).get();
    verify(service, times(2)).get(any(), any());
    NotificationsKt.expectNotifications(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    assertFalse(view.form.isEnabled());
  }
}
