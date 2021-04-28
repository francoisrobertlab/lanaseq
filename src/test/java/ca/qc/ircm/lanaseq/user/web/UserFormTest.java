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

import static ca.qc.ircm.lanaseq.user.UserProperties.ADMIN;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;
import static ca.qc.ircm.lanaseq.user.web.UserForm.ID;
import static ca.qc.ircm.lanaseq.user.web.UserForm.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Tests for {@link UserForm}.
 */
@ServiceTestAnnotations
public class UserFormTest extends AbstractViewTestCase {
  private UserForm form;
  @Mock
  private UserFormPresenter presenter;
  @Mock
  private User user;
  private Locale locale = Locale.ENGLISH;
  private AppResources userResources = new AppResources(User.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    form = new UserForm(presenter);
    form.init();
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(form);
  }

  @Test
  public void styles() {
    assertEquals(ID, form.getId().orElse(""));
    assertEquals(id(EMAIL), form.email.getId().orElse(""));
    assertEquals(id(NAME), form.name.getId().orElse(""));
    assertEquals(id(ADMIN), form.admin.getId().orElse(""));
    assertEquals(id(MANAGER), form.manager.getId().orElse(""));
  }

  @Test
  public void labels() {
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(userResources.message(EMAIL), form.email.getLabel());
    assertEquals(userResources.message(NAME), form.name.getLabel());
    assertEquals(userResources.message(ADMIN), form.admin.getLabel());
    assertEquals(userResources.message(MANAGER), form.manager.getLabel());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    form.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources userResources = new AppResources(User.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    form.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(userResources.message(EMAIL), form.email.getLabel());
    assertEquals(userResources.message(NAME), form.name.getLabel());
    assertEquals(userResources.message(ADMIN), form.admin.getLabel());
    assertEquals(userResources.message(MANAGER), form.manager.getLabel());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void isValid_False() {
    assertFalse(form.isValid());
    verify(presenter).isValid();
  }

  @Test
  public void isValid_True() {
    when(presenter.isValid()).thenReturn(true);
    assertTrue(form.isValid());
    verify(presenter).isValid();
  }

  @Test
  public void getPassword() {
    String password = "unit_test";
    when(presenter.getPassword()).thenReturn(password);
    assertEquals(password, form.getPassword());
    verify(presenter).getPassword();
  }

  @Test
  public void getUser() {
    when(presenter.getUser()).thenReturn(user);
    assertEquals(user, form.getUser());
    verify(presenter).getUser();
  }

  @Test
  public void setUser() {
    User user = new User();
    when(presenter.getUser()).thenReturn(user);

    form.localeChange(mock(LocaleChangeEvent.class));
    form.setUser(user);

    verify(presenter).setUser(user);
  }

  @Test
  public void setUser_Null() {
    form.localeChange(mock(LocaleChangeEvent.class));
    form.setUser(null);

    verify(presenter).setUser(null);
  }
}
