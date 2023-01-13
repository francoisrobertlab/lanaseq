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

import static ca.qc.ircm.lanaseq.user.web.PasswordView.SAVED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link PasswordViewPresenter}.
 */
@ServiceTestAnnotations
@WithMockUser
public class PasswordViewPresenterTest extends AbstractKaribuTestCase {
  private PasswordViewPresenter presenter;
  @Mock
  private PasswordView view;
  @Mock
  private UserService service;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private BinderValidationStatus<Passwords> passwordsValidationStatus;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;
  @Autowired
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(PasswordView.class, locale);
  private String password = "test_password";
  private User currentUser;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    presenter = new PasswordViewPresenter(service, authenticatedUser);
    view.header = new H2();
    view.passwords = mock(PasswordsForm.class);
    view.save = new Button();
    currentUser = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(currentUser));
    when(view.passwords.validate()).thenReturn(passwordsValidationStatus);
    when(passwordsValidationStatus.isOk()).thenReturn(true);
    presenter.init(view);
    presenter.localeChange(locale);
    ui.navigate(PasswordView.class);
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
    assertCurrentView(PasswordView.class);
  }

  @Test
  public void save() {
    when(view.passwords.getPassword()).thenReturn(password);

    presenter.save();

    verify(service).save(password);
    assertCurrentView(DatasetsView.class);
    verify(view).showNotification(resources.message(SAVED));
  }
}
