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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import ca.qc.ircm.lanaseq.user.web.PasswordViewPresenter;
import ca.qc.ircm.lanaseq.user.web.Passwords;
import ca.qc.ircm.lanaseq.user.web.PasswordsForm;
import ca.qc.ircm.lanaseq.web.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.binder.BinderValidationStatus;
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
public class PasswordViewPresenterTest extends AbstractViewTestCase {
  private PasswordViewPresenter presenter;
  @Mock
  private PasswordView view;
  @Mock
  private UserService service;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private BinderValidationStatus<Passwords> passwordsValidationStatus;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;
  @Autowired
  private UserRepository userRepository;
  private String password = "test_password";
  private User currentUser;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new PasswordViewPresenter(service, authorizationService);
    view.header = new H2();
    view.passwords = mock(PasswordsForm.class);
    view.save = new Button();
    currentUser = userRepository.findById(2L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(currentUser);
    when(view.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(true);
    presenter.init(view);
  }

  @Test
  public void init() {
    verify(view.passwords).setRequired(true);
    verify(view.passwords, never()).setRequired(false);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void save_PasswordValidationFailed() {
    when(passwordsValidationStatus.isOk()).thenReturn(false);

    presenter.save();

    verify(service, never()).save(any());
    verify(ui, never()).navigate(any(Class.class));
  }

  @Test
  public void save() {
    when(view.passwords.getPassword()).thenReturn(password);

    presenter.save();

    verify(service).save(password);
    verify(ui).navigate(MainView.class);
  }
}
