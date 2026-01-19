package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DRAWER_TOGGLE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER_FORM;
import static ca.qc.ircm.lanaseq.web.ViewLayout.HEADER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.ID;
import static ca.qc.ircm.lanaseq.web.ViewLayout.LABORATORY;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NAV;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_COUNT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_HEADER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_LIST;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_MARK_AS_READ;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_POPOVER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROFILE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROTOCOLS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PUBLIC_FILES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SAMPLES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIDE_NAV;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIGNOUT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.files.web.PublicFilesView;
import ca.qc.ircm.lanaseq.message.Message;
import ca.qc.ircm.lanaseq.message.MessageRepository;
import ca.qc.ircm.lanaseq.message.MessageService;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsView;
import ca.qc.ircm.lanaseq.sample.web.SamplesView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.web.ProfileView;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
  @MockitoSpyBean
  private MessageService messageService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  @Mock
  private AfterNavigationListener navigationListener;
  @Captor
  private ArgumentCaptor<Message> messageCaptor;
  private final Locale locale = Locale.ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(messageService.allUnread()).then(i -> messageRepository.findByOwnerAndUnreadOrderByIdDesc(
        authenticatedUser.getUser().orElseThrow(), true));
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
    assertEquals(styleName(PUBLIC_FILES, NAV), view.publicFiles.getId().orElse(""));
    assertEquals(styleName(PROFILE, NAV), view.profile.getId().orElse(""));
    assertEquals(styleName(USERS, NAV), view.users.getId().orElse(""));
    assertEquals(styleName(EXIT_SWITCH_USER, NAV), view.exitSwitchUser.getId().orElse(""));
    assertEquals(styleName(SIGNOUT, NAV), view.signout.getId().orElse(""));
    assertEquals(styleName(ID, NOTIFICATIONS), view.notifications.getId().orElse(""));
    validateIcon(VaadinIcon.BELL.create(), view.notifications.getIcon());
    assertEquals(view.notificationsCount, view.notifications.getSuffixComponent());
    assertEquals(styleName(ID, NOTIFICATIONS_COUNT), view.notificationsCount.getId().orElse(""));
    assertTrue(view.notificationsCount.getElement().getThemeList().contains("badge"));
    assertTrue(view.notificationsCount.getElement().getThemeList().contains("pill"));
    assertEquals(styleName(ID, NOTIFICATIONS_POPOVER),
        view.notificationsPopover.getId().orElse(""));
    assertEquals(view.notifications, view.notificationsPopover.getTarget());
    assertEquals(styleName(ID, NOTIFICATIONS),
        view.notificationsPopover.getAriaLabelledBy().orElse(""));
    assertTrue(view.notificationsPopover.isModal());
    assertTrue(view.notificationsPopover.isBackdropVisible());
    assertEquals(styleName(ID, NOTIFICATIONS_HEADER), view.notificationsHeader.getId().orElse(""));
    assertEquals(styleName(ID, NOTIFICATIONS_MARK_AS_READ),
        view.notificationsMarkAsRead.getId().orElse(""));
    assertEquals(styleName(ID, NOTIFICATIONS_LIST), view.notificationsList.getId().orElse(""));
  }

  @Test
  public void labels() {
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME),
        view.applicationName.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + LABORATORY), view.laboratory.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DATASETS), view.datasets.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES), view.samples.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROTOCOLS), view.protocols.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PUBLIC_FILES), view.publicFiles.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROFILE), view.profile.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + USERS), view.users.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + EXIT_SWITCH_USER),
        view.exitSwitchUser.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNOUT), view.signout.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + NOTIFICATIONS), view.notifications.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + NOTIFICATIONS_HEADER),
        view.notificationsHeader.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + NOTIFICATIONS_MARK_AS_READ),
        view.notificationsMarkAsRead.getText());
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
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PUBLIC_FILES), view.publicFiles.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + PROFILE), view.profile.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + USERS), view.users.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + EXIT_SWITCH_USER),
        view.exitSwitchUser.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SIGNOUT), view.signout.getLabel());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + NOTIFICATIONS), view.notifications.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + NOTIFICATIONS_HEADER),
        view.notificationsHeader.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + NOTIFICATIONS_MARK_AS_READ),
        view.notificationsMarkAsRead.getText());
  }

  @Test
  public void tabs() {
    assertTrue(view.datasets.isVisible());
    assertTrue(view.samples.isVisible());
    assertTrue(view.protocols.isVisible());
    assertTrue(view.publicFiles.isVisible());
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
  public void tabs_SelectPublicFiles() {
    UI.getCurrent().addAfterNavigationListener(navigationListener);

    test(view.sideNav).clickItem(view.publicFiles.getLabel());

    verify(navigationListener).afterNavigation(any());
    assertEquals(view.publicFiles, view.selectedSideNavItem().orElseThrow());
    assertEquals(view.publicFiles.getLabel(), view.header.getText());
    assertTrue($(PublicFilesView.class).exists());
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
    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream().anyMatch(
        i -> i.getInvocation().getExpression().contains("window.open($0, $1)") && !i.getInvocation()
            .getParameters().isEmpty() && i.getInvocation().getParameters().get(0)
            .equals("/" + DatasetsView.VIEW_NAME)));
  }

  @Test
  public void tabs_SelectSignout() {
    // Invalidated session.
    test(view.sideNav).clickItem(view.signout.getLabel());
    assertThrows(IllegalStateException.class,
        () -> VaadinServletRequest.getCurrent().getWrappedSession(false).getAttributeNames());

    assertTrue(UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream().anyMatch(
        i -> i.getInvocation().getExpression().contains("window.open($0, $1)") && !i.getInvocation()
            .getParameters().isEmpty() && i.getInvocation().getParameters().get(0)
            .equals("/" + SigninView.VIEW_NAME)));
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

  @Test
  public void notifications() {
    assertEquals("2", view.notificationsCount.getText());
    assertTrue(view.notificationsCount.getElement().getThemeList().contains("error"));

    test(view.notifications).click();

    assertTrue(view.notificationsPopover.isVisible());
    assertEquals(2, view.notificationsList.getItems().size());
    MessageListItem item = view.notificationsList.getItems().get(0);
    Message message = messageRepository.findById(3L).orElseThrow();
    assertEquals("Second unread message", item.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME), item.getUserName());
    assertEquals(message.getDate().atZone(ZoneId.systemDefault()).toInstant(), item.getTime());
    assertTrue(item.hasClassName("success"));
    item = view.notificationsList.getItems().get(1);
    message = messageRepository.findById(2L).orElseThrow();
    assertEquals("First unread message", item.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME), item.getUserName());
    assertEquals(message.getDate().atZone(ZoneId.systemDefault()).toInstant(), item.getTime());
    assertTrue(item.hasClassName("error"));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void notifications_Empty() {
    assertEquals("0", view.notificationsCount.getText());
    assertTrue(view.notificationsCount.getElement().getThemeList().contains("contrast"));

    test(view.notifications).click();

    assertTrue(view.notificationsPopover.isVisible());
    assertEquals(0, view.notificationsList.getItems().size());
  }

  @Test
  public void notificationsMarkAsRead() {
    test(view.notifications).click();
    test(view.notificationsMarkAsRead).click();

    verify(messageService, times(2)).save(messageCaptor.capture());
    assertTrue(find(messageCaptor.getAllValues(), 2L).isPresent());
    Message message = find(messageCaptor.getAllValues(), 2L).get();
    assertEquals("First unread message", message.getMessage());
    assertEquals(LocalDateTime.of(2026, 1, 15, 11, 20, 0), message.getDate());
    assertFalse(message.isUnread());
    assertEquals("error", message.getColor());
    assertEquals(3L, message.getOwner().getId());
    assertTrue(find(messageCaptor.getAllValues(), 3L).isPresent());
    message = find(messageCaptor.getAllValues(), 3L).get();
    assertEquals("Second unread message", message.getMessage());
    assertEquals(LocalDateTime.of(2026, 1, 15, 11, 22, 0), message.getDate());
    assertFalse(message.isUnread());
    assertEquals("success", message.getColor());
    assertEquals(3L, message.getOwner().getId());

    verify(messageService, atLeast(2)).allUnread();
    assertEquals("0", view.notificationsCount.getText());
    assertEquals(0, view.notificationsList.getItems().size());
  }
}
