package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsViewElement;
import ca.qc.ircm.lanaseq.sample.web.SamplesViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ProfileViewElement;
import ca.qc.ircm.lanaseq.user.web.UsersViewElement;
import com.vaadin.flow.component.messages.testbench.MessageElement;
import com.vaadin.testbench.BrowserTest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ViewLayout}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ViewLayoutIT extends AbstractBrowserTestCase {

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  @WithAnonymousUser
  public void security_Anonymous() {
    open();

    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void fieldsExistence_User() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(view::applicationName).isPresent());
    assertTrue(optional(view::header).isPresent());
    assertTrue(optional(view::laboratory).isPresent());
    assertTrue(optional(view::drawerToggle).isPresent());
    assertTrue(optional(view::datasets).isPresent());
    assertTrue(optional(view::samples).isPresent());
    assertTrue(optional(view::protocols).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertFalse(optional(view::users).isPresent());
    assertFalse(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
    assertTrue(optional(view::notifications).isPresent());
    assertTrue(optional(view::notificationsCount).isPresent());
    view.notifications().click();
    assertTrue(optional(view::notificationsPopover).isPresent());
    assertTrue(optional(view::notificationsHeader).isPresent());
    assertTrue(optional(view::notificationsMarkAsRead).isPresent());
    assertTrue(optional(view::notificationsList).isPresent());
  }

  @BrowserTest
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void fieldsExistence_Manager() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(view::applicationName).isPresent());
    assertTrue(optional(view::header).isPresent());
    assertTrue(optional(view::laboratory).isPresent());
    assertTrue(optional(view::drawerToggle).isPresent());
    assertTrue(optional(view::datasets).isPresent());
    assertTrue(optional(view::samples).isPresent());
    assertTrue(optional(view::protocols).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertTrue(optional(view::users).isPresent());
    assertFalse(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
    assertTrue(optional(view::notifications).isPresent());
    assertTrue(optional(view::notificationsCount).isPresent());
    view.notifications().click();
    assertTrue(optional(view::notificationsPopover).isPresent());
    assertTrue(optional(view::notificationsHeader).isPresent());
    assertTrue(optional(view::notificationsMarkAsRead).isPresent());
    assertTrue(optional(view::notificationsList).isPresent());
  }

  @BrowserTest
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void fieldsExistence_Admin() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(view::applicationName).isPresent());
    assertTrue(optional(view::header).isPresent());
    assertTrue(optional(view::laboratory).isPresent());
    assertTrue(optional(view::drawerToggle).isPresent());
    assertTrue(optional(view::datasets).isPresent());
    assertTrue(optional(view::samples).isPresent());
    assertTrue(optional(view::protocols).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertTrue(optional(view::users).isPresent());
    assertFalse(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
    assertTrue(optional(view::notifications).isPresent());
    assertTrue(optional(view::notificationsCount).isPresent());
    view.notifications().click();
    assertTrue(optional(view::notificationsPopover).isPresent());
    assertTrue(optional(view::notificationsHeader).isPresent());
    assertTrue(optional(view::notificationsMarkAsRead).isPresent());
    assertTrue(optional(view::notificationsList).isPresent());
  }

  @BrowserTest
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void fieldsExistence_Runas() {
    open();
    $(ViewLayoutElement.class).waitForFirst().users().click();
    UsersViewElement usersView = $(UsersViewElement.class).waitForFirst();
    usersView.users().select(1);
    usersView.switchUser().click();
    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(view::applicationName).isPresent());
    assertTrue(optional(view::header).isPresent());
    assertTrue(optional(view::laboratory).isPresent());
    assertTrue(optional(view::drawerToggle).isPresent());
    assertTrue(optional(view::datasets).isPresent());
    assertTrue(optional(view::samples).isPresent());
    assertTrue(optional(view::protocols).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertTrue(optional(view::users).isPresent());
    assertTrue(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
    assertTrue(optional(view::notifications).isPresent());
    assertTrue(optional(view::notificationsCount).isPresent());
    view.notifications().click();
    assertTrue(optional(view::notificationsPopover).isPresent());
    assertTrue(optional(view::notificationsHeader).isPresent());
    assertTrue(optional(view::notificationsMarkAsRead).isPresent());
    assertTrue(optional(view::notificationsList).isPresent());
  }

  @BrowserTest
  public void datasets() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.datasets().click();
    $(DatasetsViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void samples() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.samples().click();
    $(SamplesViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void protocols() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.protocols().click();
    $(ProtocolsViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void profile() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.profile().click();
    $(ProfileViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void users() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.users().click();
    $(UsersViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void exitSwitchUser() {
    open();
    $(ViewLayoutElement.class).waitForFirst().users().click();
    UsersViewElement usersView = $(UsersViewElement.class).waitForFirst();
    usersView.users().select(2);
    usersView.switchUser().click();
    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.profile().click();
    view.exitSwitchUser().click();
    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement viewReload = $(ViewLayoutElement.class).waitForFirst();
    assertFalse(optional(viewReload::exitSwitchUser).isPresent());
    assertTrue(optional(viewReload::users).isPresent());
  }

  @BrowserTest
  public void signout() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.signout().click();
    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void notifications() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    Assertions.assertEquals("2", view.notificationsCount().getText());
    view.notifications().click();
    List<MessageElement> messages = view.notificationsList().getMessageElements();
    Assertions.assertEquals(2, messages.size());
    MessageElement message = messages.get(0);
    Assertions.assertEquals("Second unread message", message.getText());
    Assertions.assertEquals("LANAseq", message.getUserName());
    Assertions.assertEquals("Jan 15, 2026, 11:22 a.m.", message.getTime());
    assertTrue(message.hasClassName("success"));
    message = messages.get(1);
    Assertions.assertEquals("First unread message", message.getText());
    Assertions.assertEquals("LANAseq", message.getUserName());
    Assertions.assertEquals("Jan 15, 2026, 11:20 a.m.", message.getTime());
    assertTrue(message.hasClassName("error"));
    view.notificationsMarkAsRead().click();
    Assertions.assertEquals("0", view.notificationsCount().getText());
    Assertions.assertEquals(0, view.notificationsList().getMessageElements().size());
  }
}
