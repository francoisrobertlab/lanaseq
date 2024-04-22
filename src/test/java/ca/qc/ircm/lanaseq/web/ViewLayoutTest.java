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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ViewLayout}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ViewLayoutTest extends SpringUIUnitTest {
  private ViewLayout view;
  @MockBean
  private SwitchUserService switchUserService;
  @Mock
  private AfterNavigationListener navigationListener;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ViewLayout.class, locale);
  private User user = new User(1L, "myuser");

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    navigate(DatasetsView.class);
    view = $(ViewLayout.class).first();
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
    assertEquals(styleName(SIGNOUT, TAB), view.signout.getId().orElse(""));
  }

  @Test
  public void labels() {
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
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ViewLayout.class, locale);
    UI.getCurrent().setLocale(locale);
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
    assertTrue(view.datasets.isVisible());
    assertTrue(view.samples.isVisible());
    assertTrue(view.protocols.isVisible());
    assertTrue(view.profile.isVisible());
    assertFalse(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
    assertTrue(view.signout.isVisible());
  }

  @Test
  public void tabs_SelectDatasets() {
    navigate(SamplesView.class);
    view = $(ViewLayout.class).first();
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.datasets.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.datasets, view.tabs.getSelectedTab());
    assertTrue($(DatasetsView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectDatasetsNoChange() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.datasets.getLabel());

    verify(navigationListener, never()).afterNavigation(any());
    assertEquals(view.datasets, view.tabs.getSelectedTab());
    assertTrue($(DatasetsView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectSamples() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.samples.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.samples, view.tabs.getSelectedTab());
    assertTrue($(SamplesView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectSamplesNoChange() {
    navigate(SamplesView.class);
    view = $(ViewLayout.class).first();
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.samples.getLabel());

    verify(navigationListener, never()).afterNavigation(any());
    assertEquals(view.samples, view.tabs.getSelectedTab());
    assertTrue($(SamplesView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProtocol() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.protocols.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.protocols, view.tabs.getSelectedTab());
    assertTrue($(ProtocolsView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProtocolNoChange() {
    navigate(ProtocolsView.class);
    view = $(ViewLayout.class).first();
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.protocols.getLabel());

    verify(navigationListener, never()).afterNavigation(any());
    assertEquals(view.protocols, view.tabs.getSelectedTab());
    assertTrue($(ProtocolsView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProfile() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.profile.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.profile, view.tabs.getSelectedTab());
    assertTrue($(ProfileView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProfileNoChange() {
    navigate(ProfileView.class);
    view = $(ViewLayout.class).first();
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.profile.getLabel());

    verify(navigationListener, never()).afterNavigation(any());
    assertEquals(view.profile, view.tabs.getSelectedTab());
    assertTrue($(ProfileView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void tabs_SelectUsers() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.users.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.users, view.tabs.getSelectedTab());
    assertTrue($(UsersView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void tabs_SelectUsersNoChange() {
    navigate(UsersView.class);
    view = $(ViewLayout.class).first();
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.users.getLabel());

    verify(navigationListener, never()).afterNavigation(any());
    assertEquals(view.users, view.tabs.getSelectedTab());
    assertTrue($(UsersView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectExitSwitchUser() {
    navigate(SamplesView.class);
    view = $(ViewLayout.class).first();

    view.tabs.setSelectedTab(view.exitSwitchUser);

    verify(switchUserService).exitSwitchUser();
    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> i.getInvocation().getExpression().contains("window.open($0, $1)")
            && i.getInvocation().getParameters().size() > 0
            && i.getInvocation().getParameters().get(0).equals("/")));
  }

  @Test
  @Disabled("Fails because of invalidated session")
  public void tabs_SelectSignout() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.tabs).select(view.signout.getLabel());

    verify(navigationListener, never()).afterNavigation(any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void tabs_UserVisibility() {
    assertFalse(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void tabs_ManagerVisibility() {
    assertTrue(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void tabs_AdminVisibility() {
    assertTrue(view.users.isVisible());
    assertFalse(view.exitSwitchUser.isVisible());
  }

  @Test
  @WithMockUser(username = "jonh.smith@ircm.qc.ca", roles = { "USER", "PREVIOUS_ADMINISTRATOR" })
  public void tabs_SwitchedUserVisibility() {
    assertFalse(view.users.isVisible());
    assertTrue(view.exitSwitchUser.isVisible());
  }
}
