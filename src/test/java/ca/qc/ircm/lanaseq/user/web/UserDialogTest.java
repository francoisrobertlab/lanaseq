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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.HEADER;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.ID;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link UserDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UserDialogTest extends SpringUIUnitTest {
  private UserDialog dialog;
  @MockBean
  private UserService userService;
  @Mock
  private ComponentEventListener<SavedEvent<UserDialog>> savedListener;
  @Autowired
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(UserDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    UsersView view = navigate(UsersView.class);
    User user = userRepository.findById(2L).get();
    doubleClickItem(view.users, user);
    dialog = $(UserDialog.class).first();
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    assertEquals(id(CANCEL), dialog.cancel.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(resources.message(HEADER, 0), dialog.getHeaderTitle());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(UserDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    UI.getCurrent().setLocale(locale);
    assertEquals(resources.message(HEADER, 0), dialog.getHeaderTitle());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
  }

  @Test
  public void savedListener() {
    dialog.addSavedListener(savedListener);
    dialog.fireSavedEvent();
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void savedListener_Remove() {
    dialog.addSavedListener(savedListener).remove();
    dialog.fireSavedEvent();
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void getUser() {
    User user = mock(User.class);
    dialog.form = mock(UserForm.class);
    when(dialog.form.getUser()).thenReturn(user);
    assertEquals(user, dialog.getUser());
    verify(dialog.form).getUser();
  }

  @Test
  public void setUser_NewUser() {
    User user = new User();
    dialog.form = mock(UserForm.class);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(dialog.form).setUser(user);
    assertEquals(resources.message(HEADER, 0), dialog.getHeaderTitle());
  }

  @Test
  public void setUser_User() {
    User user = userRepository.findById(2L).get();
    dialog.form = mock(UserForm.class);
    when(dialog.form.getUser()).thenReturn(user);

    dialog.setUser(user);

    verify(dialog.form).setUser(user);
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.getHeaderTitle());
  }

  @Test
  public void setUser_Null() {
    dialog.form = mock(UserForm.class);

    dialog.setUser(null);

    verify(dialog.form).setUser(null);
    assertEquals(resources.message(HEADER, 0), dialog.getHeaderTitle());
  }

  @Test
  public void save_ValidationFalse() {
    dialog.form = mock(UserForm.class);
    dialog.addSavedListener(savedListener);

    dialog.save();

    verify(userService, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_ValidationTrue() {
    User user = mock(User.class);
    dialog.form = mock(UserForm.class);
    when(dialog.form.isValid()).thenReturn(true);
    when(dialog.form.getUser()).thenReturn(user);
    String password = "test_password";
    when(dialog.form.getPassword()).thenReturn(password);
    String email = "test@ircm.qc.ca";
    when(user.getEmail()).thenReturn(email);
    dialog.addSavedListener(savedListener);

    dialog.save();

    verify(userService).save(user, password);
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, email), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_ValidationTrueNullPassword() {
    User user = mock(User.class);
    dialog.form = mock(UserForm.class);
    when(dialog.form.isValid()).thenReturn(true);
    when(dialog.form.getUser()).thenReturn(user);
    String email = "test@ircm.qc.ca";
    when(user.getEmail()).thenReturn(email);
    dialog.addSavedListener(savedListener);

    dialog.save();

    verify(userService).save(user, null);
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, email), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void cancel_Close() {
    dialog.addSavedListener(savedListener);

    dialog.cancel();

    verify(userService, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertFalse(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }
}
