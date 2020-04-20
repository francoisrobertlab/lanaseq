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
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.THEME;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.CLASS_NAME;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserDialogTest extends AbstractViewTestCase {
  private UserDialog dialog;
  private UserForm form;
  @Mock
  private UserDialogPresenter presenter;
  @Mock
  private UserFormPresenter userFormPresenter;
  @Mock
  private User user;
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
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    form = new UserForm(userFormPresenter);
    dialog = new UserDialog(presenter, form);
    dialog.init();
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(CLASS_NAME, dialog.getId().orElse(""));
    assertTrue(dialog.header.getClassNames().contains(HEADER));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertTrue(dialog.save.getElement().getAttribute(THEME).contains(PRIMARY));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
  }

  @Test
  public void localeChange() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(UserDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
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
    when(userFormPresenter.getUser()).thenReturn(user);
    assertEquals(user, dialog.getUser());
    verify(userFormPresenter).getUser();
  }

  @Test
  public void setUser_NewUser() {
    User user = new User();
    when(userFormPresenter.getUser()).thenReturn(user);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(userFormPresenter).setUser(user);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setUser_User() {
    User user = userRepository.findById(2L).get();
    when(userFormPresenter.getUser()).thenReturn(user);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(userFormPresenter).setUser(user);
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_UserBeforeLocaleChange() {
    User user = userRepository.findById(2L).get();
    when(userFormPresenter.getUser()).thenReturn(user);

    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(userFormPresenter).setUser(user);
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(null);

    verify(userFormPresenter).setUser(null);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void save() {
    clickButton(dialog.save);

    verify(presenter).save(locale);
  }

  @Test
  public void cancel() {
    clickButton(dialog.cancel);

    verify(presenter).cancel();
  }
}
