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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.HOME;
import static ca.qc.ircm.lanaseq.web.ViewLayout.ID;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROFILE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIGNOUT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.TAB;
import static ca.qc.ircm.lanaseq.web.ViewLayout.TABS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.USERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.experiment.web.ExperimentsView;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.web.WebSecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Location;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class ViewLayoutTest extends AbstractViewTestCase {
  private ViewLayout view;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ViewLayout.class, locale);
  private User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new ViewLayout(authorizationService);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    view.init();
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(TABS, view.tabs.getId().orElse(""));
    assertEquals(styleName(HOME, TAB), view.home.getId().orElse(""));
    assertEquals(styleName(PROFILE, TAB), view.profile.getId().orElse(""));
    assertEquals(styleName(USERS, TAB), view.users.getId().orElse(""));
    assertEquals(styleName(EXIT_SWITCH_USER, TAB), view.exitSwitchUser.getId().orElse(""));
    assertEquals(styleName(SIGNOUT, TAB), view.signout.getId().orElse(""));
  }

  @Test
  public void labels() {
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HOME), view.home.getLabel());
    assertEquals(resources.message(PROFILE), view.profile.getLabel());
    assertEquals(resources.message(USERS), view.users.getLabel());
    assertEquals(resources.message(EXIT_SWITCH_USER), view.exitSwitchUser.getLabel());
    assertEquals(resources.message(SIGNOUT), view.signout.getLabel());
  }

  @Test
  public void localeChange() {
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ViewLayout.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HOME), view.home.getLabel());
    assertEquals(resources.message(PROFILE), view.profile.getLabel());
    assertEquals(resources.message(USERS), view.users.getLabel());
    assertEquals(resources.message(EXIT_SWITCH_USER), view.exitSwitchUser.getLabel());
    assertEquals(resources.message(SIGNOUT), view.signout.getLabel());
  }

  @Test
  public void tabs() {
    view.init();
    assertTrue(view.home.isVisible());
    assertTrue(view.profile.isVisible());
    assertFalse(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
    assertTrue(view.signout.isVisible());
  }

  @Test
  public void tabs_AllowUsersView() {
    when(authorizationService.isAuthorized(UsersView.class)).thenReturn(true);
    view.init();
    assertTrue(view.home.isVisible());
    assertTrue(view.profile.isVisible());
    assertTrue(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
    assertTrue(view.signout.isVisible());
  }

  @Test
  public void tabs_SwitchedUser() {
    when(authorizationService.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR))
        .thenReturn(true);
    view.init();
    assertTrue(view.home.isVisible());
    assertTrue(view.profile.isVisible());
    assertFalse(view.users.isVisible());
    assertTrue(view.exitSwitchUser.isVisible());
    assertTrue(view.signout.isVisible());
  }

  @Test
  public void tabs_SelectHome() {
    Location location = new Location(UsersView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.home);

    verify(ui).navigate(ExperimentsView.VIEW_NAME);
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectHomeNoChange() {
    Location location = new Location(ExperimentsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.home);

    verify(ui, never()).navigate(any(String.class));
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectProtocol() {
    Location location = new Location(ExperimentsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.protocols);

    verify(ui).navigate(ProtocolsView.VIEW_NAME);
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectProtocolNoChange() {
    Location location = new Location(ProtocolsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.protocols);

    verify(ui, never()).navigate(any(String.class));
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectProfile() {
    Location location = new Location(ExperimentsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.profile);

    verify(ui).navigate(ProfileView.VIEW_NAME);
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectProfileNoChange() {
    Location location = new Location(ProfileView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.profile);

    verify(ui, never()).navigate(any(String.class));
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectUsers() {
    Location location = new Location(ExperimentsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.users);

    verify(ui).navigate(UsersView.VIEW_NAME);
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectUsersNoChange() {
    Location location = new Location(UsersView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.users);

    verify(ui, never()).navigate(any(String.class));
    verify(page, never()).executeJs(any());
  }

  @Test
  public void tabs_SelectExitSwitchUser() {
    Location location = new Location(ExperimentsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.exitSwitchUser);

    verify(ui, never()).navigate(any(String.class));
    verify(page)
        .executeJs("location.assign('" + WebSecurityConfiguration.SWITCH_USER_EXIT_URL + "')");
  }

  @Test
  public void tabs_SelectSignout() {
    Location location = new Location(ExperimentsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.signout);

    verify(ui, never()).navigate(any(String.class));
    verify(page).executeJs("location.assign('" + WebSecurityConfiguration.SIGNOUT_URL + "')");
  }

  @Test
  public void afterNavigation_Home() {
    Location location = new Location(ExperimentsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);

    view.afterNavigation(afterNavigationEvent);

    assertEquals(view.home, view.tabs.getSelectedTab());
  }

  @Test
  public void afterNavigation_Protocols() {
    Location location = new Location(ProtocolsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);

    view.afterNavigation(afterNavigationEvent);

    assertEquals(view.protocols, view.tabs.getSelectedTab());
  }

  @Test
  public void afterNavigation_Profile() {
    Location location = new Location(ProfileView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);

    view.afterNavigation(afterNavigationEvent);

    assertEquals(view.profile, view.tabs.getSelectedTab());
  }

  @Test
  public void afterNavigation_Users() {
    Location location = new Location(UsersView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);

    view.afterNavigation(afterNavigationEvent);

    assertEquals(view.users, view.tabs.getSelectedTab());
  }

  public static class ViewTest {
  }
}
