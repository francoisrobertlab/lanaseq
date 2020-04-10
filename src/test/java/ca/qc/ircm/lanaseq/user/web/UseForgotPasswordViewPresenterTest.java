/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.INVALID;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SEPARATOR;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.user.web.PasswordsForm;
import ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.UseForgotPasswordViewPresenter;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UseForgotPasswordViewPresenterTest extends AbstractViewTestCase {
  private UseForgotPasswordViewPresenter presenter;
  @Mock
  private UseForgotPasswordView view;
  @Mock
  private ForgotPasswordService service;
  @Mock
  private ForgotPassword forgotPassword;
  private Locale locale = ENGLISH;
  private AppResources resources = new AppResources(UseForgotPasswordView.class, locale);
  private long id = 34925;
  private String confirmNumber = "feafet23ts";
  private String parameter = id + SEPARATOR + confirmNumber;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new UseForgotPasswordViewPresenter(service);
    view.header = new H2();
    view.message = new Div();
    view.form = mock(PasswordsForm.class);
    view.buttonsLayout = new HorizontalLayout();
    view.save = new Button();
    when(service.get(any(Long.class), any())).thenReturn(forgotPassword);
  }

  @Test
  public void init() {
    presenter.init(view);
  }

  @Test
  public void save_Invalid() {
    presenter.init(view);
    presenter.setParameter(parameter, locale);

    presenter.save(locale);

    verify(view.form).isValid();
    verify(service, never()).updatePassword(any(), any());
  }

  @Test
  public void save() {
    String password = "test_password";
    when(view.form.isValid()).thenReturn(true);
    when(view.form.getPassword()).thenReturn(password);
    presenter.init(view);
    presenter.setParameter(parameter, locale);

    presenter.save(locale);

    verify(view.form).isValid();
    verify(service).updatePassword(eq(forgotPassword), eq(password));
    verify(ui).navigate(SigninView.class);
    verify(view).showNotification(resources.message(SAVED));
  }

  @Test
  public void setParameter() {
    presenter.init(view);
    presenter.setParameter(parameter, locale);
    verify(service, atLeastOnce()).get(id, confirmNumber);
    assertTrue(view.save.isEnabled());
    verify(view.form, never()).setEnabled(false);
  }

  @Test
  public void setParameter_IdNotNumber() {
    presenter.init(view);
    presenter.setParameter("A434GS", locale);
    verify(service, never()).get(id, confirmNumber);
    verify(view).showNotification(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    verify(view.form).setEnabled(false);
  }

  @Test
  public void setParameter_MissingConfirm() {
    presenter.init(view);
    presenter.setParameter(String.valueOf(id), locale);
    verify(service, never()).get(id, confirmNumber);
    verify(view).showNotification(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    verify(view.form).setEnabled(false);
  }

  @Test
  public void setParameter_NullForgotPassword() {
    when(service.get(any(Long.class), any())).thenReturn(null);
    presenter.init(view);
    presenter.setParameter(parameter, locale);
    verify(service).get(id, confirmNumber);
    verify(view).showNotification(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    verify(view.form).setEnabled(false);
  }

  @Test
  public void setParameter_Null() {
    presenter.init(view);
    presenter.setParameter(null, locale);
    verify(service, never()).get(id, confirmNumber);
    verify(view).showNotification(resources.message(INVALID));
    assertFalse(view.save.isEnabled());
    verify(view.form).setEnabled(false);
  }
}
