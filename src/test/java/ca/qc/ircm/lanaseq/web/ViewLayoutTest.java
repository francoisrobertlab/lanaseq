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

import static ca.qc.ircm.lanaseq.security.web.WebSecurityConfiguration.SWITCH_USER_EXIT_URL;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER_FORM;
import static ca.qc.ircm.lanaseq.web.ViewLayout.ID;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROFILE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROTOCOLS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SAMPLES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIGNOUT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.TAB;
import static ca.qc.ircm.lanaseq.web.ViewLayout.TABS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.Location;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

/**
 * Tests for {@link ViewLayout}.
 */
@ServiceTestAnnotations
@WithMockUser
public class ViewLayoutTest extends AbstractKaribuTestCase {
  private ViewLayout view;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private AfterNavigationListener navigationListener;
  @Mock
  private AfterNavigationEvent afterNavigationEvent;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ViewLayout.class, locale);
  private User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    ui.addAfterNavigationListener(navigationListener);
    view = new ViewLayout(authenticatedUser);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    view.init();
  }

  private void assertNoExecuteJs() {
    assertFalse(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> i.getInvocation().getExpression().contains(EXIT_SWITCH_USER_FORM)));
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(TABS, view.tabs.getId().orElse(""));
    assertEquals(styleName(DATASETS, TAB), view.datasets.getId().orElse(""));
    assertEquals(styleName(SAMPLES, TAB), view.samples.getId().orElse(""));
    assertEquals(styleName(PROTOCOLS, TAB), view.protocols.getId().orElse(""));
    assertEquals(styleName(PROFILE, TAB), view.profile.getId().orElse(""));
    assertEquals(styleName(USERS, TAB), view.users.getId().orElse(""));
    assertEquals(styleName(EXIT_SWITCH_USER, TAB), view.exitSwitchUser.getId().orElse(""));
    assertEquals(styleName(EXIT_SWITCH_USER_FORM, TAB), view.exitSwitchUserForm.getId().orElse(""));
    assertEquals(SWITCH_USER_EXIT_URL, view.exitSwitchUserForm.getElement().getAttribute("action"));
    assertEquals("post", view.exitSwitchUserForm.getElement().getAttribute("method"));
    assertEquals("none", view.exitSwitchUserForm.getElement().getStyle().get("display"));
    assertEquals(styleName(SIGNOUT, TAB), view.signout.getId().orElse(""));
  }

  @Test
  public void labels() {
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(DATASETS), view.datasets.getLabel());
    assertEquals(resources.message(SAMPLES), view.samples.getLabel());
    assertEquals(resources.message(PROTOCOLS), view.protocols.getLabel());
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
    ui.setLocale(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(DATASETS), view.datasets.getLabel());
    assertEquals(resources.message(SAMPLES), view.samples.getLabel());
    assertEquals(resources.message(PROTOCOLS), view.protocols.getLabel());
    assertEquals(resources.message(PROFILE), view.profile.getLabel());
    assertEquals(resources.message(USERS), view.users.getLabel());
    assertEquals(resources.message(EXIT_SWITCH_USER), view.exitSwitchUser.getLabel());
    assertEquals(resources.message(SIGNOUT), view.signout.getLabel());
  }

  @Test
  public void tabs() {
    view.init();
    assertTrue(view.datasets.isVisible());
    assertTrue(view.samples.isVisible());
    assertTrue(view.protocols.isVisible());
    assertTrue(view.profile.isVisible());
    assertFalse(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
    assertFalse(view.exitSwitchUserForm.isVisible());
    assertTrue(view.signout.isVisible());
  }

  @Test
  public void tabs_AllowUsersView() {
    when(authenticatedUser.isAuthorized(UsersView.class)).thenReturn(true);
    view.init();
    assertTrue(view.datasets.isVisible());
    assertTrue(view.samples.isVisible());
    assertTrue(view.protocols.isVisible());
    assertTrue(view.profile.isVisible());
    assertTrue(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
    assertFalse(view.exitSwitchUserForm.isVisible());
    assertTrue(view.signout.isVisible());
  }

  @Test
  public void tabs_SwitchedUser() {
    when(authenticatedUser.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR)).thenReturn(true);
    view.init();
    assertTrue(view.datasets.isVisible());
    assertTrue(view.samples.isVisible());
    assertTrue(view.protocols.isVisible());
    assertTrue(view.profile.isVisible());
    assertFalse(view.users.isVisible());
    assertTrue(view.exitSwitchUser.isVisible());
    assertTrue(view.exitSwitchUserForm.isVisible());
    assertTrue(view.signout.isVisible());
  }

  @Test
  public void tabs_SelectDatasets() {
    Location location = new Location(UsersView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.datasets);

    verify(navigationListener).afterNavigation(any());
    assertCurrentView(DatasetsView.class);
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectDatasetsNoChange() {
    Location location = new Location(DatasetsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.datasets);

    verify(navigationListener, never()).afterNavigation(any());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectSamples() {
    Location location = new Location(UsersView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.samples);

    verify(navigationListener).afterNavigation(any());
    assertCurrentView(SamplesView.class);
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectSamplesNoChange() {
    Location location = new Location(SamplesView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.samples);

    verify(navigationListener, never()).afterNavigation(any());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProtocol() {
    Location location = new Location(DatasetsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.protocols);

    verify(navigationListener).afterNavigation(any());
    assertCurrentView(ProtocolsView.class);
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProtocolNoChange() {
    Location location = new Location(ProtocolsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.protocols);

    verify(navigationListener, never()).afterNavigation(any());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProfile() {
    Location location = new Location(DatasetsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.profile);

    verify(navigationListener).afterNavigation(any());
    assertCurrentView(ProfileView.class);
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProfileNoChange() {
    Location location = new Location(ProfileView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.profile);

    verify(navigationListener, never()).afterNavigation(any());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectUsers() {
    Location location = new Location(DatasetsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.users);

    verify(navigationListener).afterNavigation(any());
    assertCurrentView(UsersView.class);
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectUsersNoChange() {
    Location location = new Location(UsersView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.users);

    verify(navigationListener, never()).afterNavigation(any());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectExitSwitchUser() {
    Location location = new Location(DatasetsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.exitSwitchUser);

    verify(navigationListener, never()).afterNavigation(any());
    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> i.getInvocation().getExpression().equals("document.getElementById(\""
            + styleName(EXIT_SWITCH_USER_FORM, TAB) + "\").submit()")));
  }

  @Test
  public void tabs_SelectSignout() {
    Location location = new Location(DatasetsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);
    view.afterNavigation(afterNavigationEvent);

    view.tabs.setSelectedTab(view.signout);

    verify(navigationListener, never()).afterNavigation(any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void afterNavigation_Datasets() {
    Location location = new Location(DatasetsView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);

    view.afterNavigation(afterNavigationEvent);

    assertEquals(view.datasets, view.tabs.getSelectedTab());
  }

  @Test
  public void afterNavigation_Samples() {
    Location location = new Location(SamplesView.VIEW_NAME);
    when(afterNavigationEvent.getLocation()).thenReturn(location);

    view.afterNavigation(afterNavigationEvent);

    assertEquals(view.samples, view.tabs.getSelectedTab());
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

  /**
   * Fake view for tests.
   */
  public static class ViewTest {
  }
}
