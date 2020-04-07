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

import static ca.qc.ircm.lana.Constants.BORDER;
import static ca.qc.ircm.lana.Constants.CANCEL;
import static ca.qc.ircm.lana.Constants.PRIMARY;
import static ca.qc.ircm.lana.Constants.SAVE;
import static ca.qc.ircm.lana.Constants.THEME;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.text.Strings.styleName;
import static ca.qc.ircm.lana.user.UserProperties.ADMIN;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.MANAGER;
import static ca.qc.ircm.lana.user.UserProperties.NAME;
import static ca.qc.ircm.lana.user.web.UserDialog.CLASS_NAME;
import static ca.qc.ircm.lana.user.web.UserDialog.CREATE_NEW_LABORATORY;
import static ca.qc.ircm.lana.user.web.UserDialog.HEADER;
import static ca.qc.ircm.lana.user.web.UserDialog.LABORATORY_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.Constants;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.web.SavedEvent;
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
  @Mock
  private UserDialogPresenter presenter;
  @Mock
  private User user;
  @Mock
  private ComponentEventListener<SavedEvent<UserDialog>> savedListener;
  @Autowired
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(UserDialog.class, locale);
  private AppResources userResources = new AppResources(User.class, locale);
  private AppResources laboratoryResources = new AppResources(Laboratory.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new UserDialog(presenter);
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
    assertTrue(dialog.email.getClassNames().contains(EMAIL));
    assertTrue(dialog.name.getClassNames().contains(NAME));
    assertTrue(dialog.admin.getClassNames().contains(ADMIN));
    assertTrue(dialog.manager.getClassNames().contains(MANAGER));
    assertTrue(dialog.createNewLaboratory.getClassNames().contains(CREATE_NEW_LABORATORY));
    assertTrue(dialog.laboratory.getClassNames().contains(LABORATORY));
    assertTrue(dialog.newLaboratoryLayout.getClassNames().contains(BORDER));
    assertTrue(
        dialog.newLaboratoryName.getClassNames().contains(styleName(LABORATORY, LABORATORY_NAME)));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertTrue(dialog.save.getElement().getAttribute(THEME).contains(PRIMARY));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(EMAIL), dialog.email.getLabel());
    assertEquals(userResources.message(NAME), dialog.name.getLabel());
    assertEquals(userResources.message(ADMIN), dialog.admin.getLabel());
    assertEquals(userResources.message(MANAGER), dialog.manager.getLabel());
    assertEquals(resources.message(CREATE_NEW_LABORATORY), dialog.createNewLaboratory.getLabel());
    assertEquals(userResources.message(LABORATORY), dialog.laboratory.getLabel());
    assertEquals(userResources.message(LABORATORY), dialog.newLaboratoryHeader.getText());
    assertEquals(laboratoryResources.message(LABORATORY_NAME), dialog.newLaboratoryName.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(UserDialog.class, locale);
    final AppResources userResources = new AppResources(User.class, locale);
    final AppResources laboratoryResources = new AppResources(Laboratory.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(EMAIL), dialog.email.getLabel());
    assertEquals(userResources.message(NAME), dialog.name.getLabel());
    assertEquals(userResources.message(ADMIN), dialog.admin.getLabel());
    assertEquals(userResources.message(MANAGER), dialog.manager.getLabel());
    assertEquals(resources.message(CREATE_NEW_LABORATORY), dialog.createNewLaboratory.getLabel());
    assertEquals(userResources.message(LABORATORY), dialog.laboratory.getLabel());
    assertEquals(userResources.message(LABORATORY), dialog.newLaboratoryHeader.getText());
    assertEquals(laboratoryResources.message(LABORATORY_NAME), dialog.newLaboratoryName.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    verify(presenter).localeChange(locale);
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
    when(presenter.getUser()).thenReturn(user);
    assertEquals(user, dialog.getUser());
    verify(presenter).getUser();
  }

  @Test
  public void setUser_NewUser() {
    User user = new User();
    when(presenter.getUser()).thenReturn(user);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(presenter).setUser(user);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setUser_User() {
    User user = userRepository.findById(2L).get();
    when(presenter.getUser()).thenReturn(user);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(user);

    verify(presenter).setUser(user);
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_UserBeforeLocaleChange() {
    User user = userRepository.findById(2L).get();
    when(presenter.getUser()).thenReturn(user);

    dialog.setUser(user);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setUser(user);
    assertEquals(resources.message(HEADER, 1, user.getName()), dialog.header.getText());
  }

  @Test
  public void setUser_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setUser(null);

    verify(presenter).setUser(null);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void save() {
    clickButton(dialog.save);

    verify(presenter).save();
  }

  @Test
  public void cancel() {
    clickButton(dialog.cancel);

    verify(presenter).cancel();
  }
}
