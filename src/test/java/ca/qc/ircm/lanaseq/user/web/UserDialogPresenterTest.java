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

import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox.CustomValueSetEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Locale;
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
public class UserDialogPresenterTest extends AbstractViewTestCase {
  private UserDialogPresenter presenter;
  @Mock
  private UserDialog dialog;
  @Mock
  private UserService userService;
  @Mock
  private AuthorizationService authorizationService;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;
  @Captor
  @SuppressWarnings("checkstyle:linelength")
  private ArgumentCaptor<ComponentEventListener<CustomValueSetEvent<ComboBox<Laboratory>>>> laboratoryComponentEventListenerCaptor;
  @Autowired
  private UserRepository userRepository;
  @Mock
  private User user;
  private String email = "test@ircm.qc.ca";
  private String password = "test_password";
  private User currentUser;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(UserDialog.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new UserDialogPresenter(userService);
    dialog.header = new H2();
    dialog.form = mock(UserForm.class);
    dialog.buttonsLayout = new HorizontalLayout();
    dialog.save = new Button();
    dialog.cancel = new Button();
    currentUser = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
  }

  @Test
  public void save_ValidationFalse() {
    presenter.init(dialog);

    presenter.save(locale);

    verify(userService, never()).save(any(), any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_ValidationTrue() {
    when(dialog.form.isValid()).thenReturn(true);
    when(dialog.form.getUser()).thenReturn(user);
    when(dialog.form.getPassword()).thenReturn(password);
    when(user.getEmail()).thenReturn(email);
    presenter.init(dialog);

    presenter.save(locale);

    verify(userService).save(user, password);
    verify(dialog).showNotification(resources.message(SAVED, email));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_ValidationTrueNullPassword() {
    when(dialog.form.isValid()).thenReturn(true);
    when(dialog.form.getUser()).thenReturn(user);
    when(user.getEmail()).thenReturn(email);
    presenter.init(dialog);

    presenter.save(locale);

    verify(userService).save(user, null);
    verify(dialog).showNotification(resources.message(SAVED, email));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.init(dialog);

    presenter.cancel();

    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
  }
}
