package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DRAWER_TOGGLE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER_FORM;
import static ca.qc.ircm.lanaseq.web.ViewLayout.HEADER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.ID;
import static ca.qc.ircm.lanaseq.web.ViewLayout.LABORATORY;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NAV;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROFILE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROTOCOLS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SAMPLES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIDE_NAV;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIGNOUT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Tests for {@link ViewLayout}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ViewLayoutTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ViewLayout.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private ViewLayout view;
  @MockitoSpyBean
  private SwitchUserService switchUserService;
  @Autowired
  private UserRepository userRepository;
  @Mock
  private AfterNavigationListener navigationListener;
  @Captor
  private ArgumentCaptor<AfterNavigationEvent> afterNavigationEventCaptor;
  private Locale locale = Locale.ENGLISH;
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
    assertEquals(styleName(APPLICATION_NAME), view.applicationName.getId().orElse(""));
    assertEquals(styleName(ID, HEADER), view.header.getId().orElse(""));
    assertEquals("100%", view.header.getWidth());
    assertEquals(styleName(ID, LABORATORY), view.laboratory.getId().orElse(""));
    assertEquals("15em", view.laboratory.getMinWidth());
    assertEquals("right", view.laboratory.getStyle().get("text-align"));
    assertEquals(DRAWER_TOGGLE, view.drawerToggle.getId().orElse(""));
    assertEquals(SIDE_NAV, view.sideNav.getId().orElse(""));
    assertEquals(styleName(DATASETS, NAV), view.datasets.getId().orElse(""));
    assertEquals(styleName(SAMPLES, NAV), view.samples.getId().orElse(""));
    assertEquals(styleName(PROTOCOLS, NAV), view.protocols.getId().orElse(""));
    assertEquals(styleName(PROFILE, NAV), view.profile.getId().orElse(""));
    assertEquals(styleName(USERS, NAV), view.users.getId().orElse(""));
    assertEquals(styleName(EXIT_SWITCH_USER, NAV), view.exitSwitchUser.getId().orElse(""));
    assertEquals(styleName(SIGNOUT, NAV), view.signout.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME),
        view.applicationName.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + LABORATORY), view.laboratory.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS), view.datasets.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES), view.samples.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROTOCOLS), view.protocols.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROFILE), view.profile.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + USERS), view.users.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + EXIT_SWITCH_USER),
        view.exitSwitchUser.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNOUT), view.signout.getLabel());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME),
        view.applicationName.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + LABORATORY), view.laboratory.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS), view.datasets.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES), view.samples.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROTOCOLS), view.protocols.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROFILE), view.profile.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + USERS), view.users.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + EXIT_SWITCH_USER),
        view.exitSwitchUser.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNOUT), view.signout.getLabel());
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

    test(view.sideNav).clickItem(view.datasets.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.datasets, view.selectedSideNavItem().orElseThrow());
    assertEquals(view.datasets.getLabel(), view.header.getText());
    assertTrue($(DatasetsView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectSamples() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.sideNav).clickItem(view.samples.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.samples, view.selectedSideNavItem().orElseThrow());
    assertEquals(view.samples.getLabel(), view.header.getText());
    assertTrue($(SamplesView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProtocol() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.sideNav).clickItem(view.protocols.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.protocols, view.selectedSideNavItem().orElseThrow());
    assertEquals(view.protocols.getLabel(), view.header.getText());
    assertTrue($(ProtocolsView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  public void tabs_SelectProfile() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.sideNav).clickItem(view.profile.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.profile, view.selectedSideNavItem().orElseThrow());
    assertEquals(view.profile.getLabel(), view.header.getText());
    assertTrue($(ProfileView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void tabs_SelectUsers() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.sideNav).clickItem(view.users.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.users, view.selectedSideNavItem().orElseThrow());
    assertEquals(view.users.getLabel(), view.header.getText());
    assertTrue($(UsersView.class).exists());
    assertNoExecuteJs();
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void tabs_SelectExitSwitchUser() {
    switchUserService.switchUser(userRepository.findById(3L).orElseThrow(),
        VaadinServletRequest.getCurrent());
    navigate(SamplesView.class);
    view = $(ViewLayout.class).first();

    test(view.sideNav).clickItem(view.exitSwitchUser.getLabel());

    verify(switchUserService).exitSwitchUser(VaadinServletRequest.getCurrent());
    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> i.getInvocation().getExpression().contains("window.open($0, $1)")
            && !i.getInvocation().getParameters().isEmpty()
            && i.getInvocation().getParameters().get(0).equals("/")));
  }

  @Test
  public void tabs_SelectSignout() {
    // Invalidated session.
    assertThrows(InvocationTargetException.class,
        () -> test(view.sideNav).clickItem(view.signout.getLabel()));
    assertThrows(IllegalStateException.class,
        () -> VaadinServletRequest.getCurrent().getWrappedSession(false).getAttributeNames());

    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
        .anyMatch(i -> i.getInvocation().getExpression().contains("window.open($0, $1)")
            && !i.getInvocation().getParameters().isEmpty()
            && i.getInvocation().getParameters().get(0).equals("/")));
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
  @WithMockUser(username = "jonh.smith@ircm.qc.ca", roles = {"USER", "PREVIOUS_ADMINISTRATOR"})
  public void tabs_SwitchedUserVisibility() {
    assertFalse(view.users.isVisible());
    assertTrue(view.exitSwitchUser.isVisible());
  }
}
