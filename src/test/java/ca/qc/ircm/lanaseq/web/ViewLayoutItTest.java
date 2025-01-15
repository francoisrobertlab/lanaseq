package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsViewElement;
import ca.qc.ircm.lanaseq.sample.web.SamplesViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ProfileViewElement;
import ca.qc.ircm.lanaseq.user.web.UsersViewElement;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ViewLayout}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ViewLayoutItTest extends AbstractTestBenchTestCase {
  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();

    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  public void fieldsExistence_User() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(() -> view.applicationName()).isPresent());
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.laboratory()).isPresent());
    assertTrue(optional(() -> view.drawerToggle()).isPresent());
    assertTrue(optional(() -> view.datasets()).isPresent());
    assertTrue(optional(() -> view.samples()).isPresent());
    assertTrue(optional(() -> view.protocols()).isPresent());
    assertTrue(optional(() -> view.profile()).isPresent());
    assertFalse(optional(() -> view.users()).isPresent());
    assertFalse(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void fieldsExistence_Manager() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(() -> view.applicationName()).isPresent());
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.laboratory()).isPresent());
    assertTrue(optional(() -> view.drawerToggle()).isPresent());
    assertTrue(optional(() -> view.datasets()).isPresent());
    assertTrue(optional(() -> view.samples()).isPresent());
    assertTrue(optional(() -> view.protocols()).isPresent());
    assertTrue(optional(() -> view.profile()).isPresent());
    assertTrue(optional(() -> view.users()).isPresent());
    assertFalse(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void fieldsExistence_Admin() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(() -> view.applicationName()).isPresent());
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.laboratory()).isPresent());
    assertTrue(optional(() -> view.drawerToggle()).isPresent());
    assertTrue(optional(() -> view.datasets()).isPresent());
    assertTrue(optional(() -> view.samples()).isPresent());
    assertTrue(optional(() -> view.protocols()).isPresent());
    assertTrue(optional(() -> view.profile()).isPresent());
    assertTrue(optional(() -> view.users()).isPresent());
    assertFalse(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void fieldsExistence_Runas() throws Throwable {
    open();
    $(ViewLayoutElement.class).waitForFirst().users().click();
    UsersViewElement usersView = $(UsersViewElement.class).waitForFirst();
    usersView.users().select(1);
    usersView.switchUser().click();
    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(() -> view.applicationName()).isPresent());
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.laboratory()).isPresent());
    assertTrue(optional(() -> view.drawerToggle()).isPresent());
    assertTrue(optional(() -> view.datasets()).isPresent());
    assertTrue(optional(() -> view.samples()).isPresent());
    assertTrue(optional(() -> view.protocols()).isPresent());
    assertTrue(optional(() -> view.profile()).isPresent());
    assertTrue(optional(() -> view.users()).isPresent());
    assertTrue(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  public void datasets() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.datasets().click();
    $(DatasetsViewElement.class).waitForFirst();
  }

  @Test
  public void samples() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.samples().click();
    $(SamplesViewElement.class).waitForFirst();
  }

  @Test
  public void protocols() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.protocols().click();
    $(ProtocolsViewElement.class).waitForFirst();
  }

  @Test
  public void profile() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.profile().click();
    $(ProfileViewElement.class).waitForFirst();
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void users() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.users().click();
    $(UsersViewElement.class).waitForFirst();
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void exitSwitchUser() throws Throwable {
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
    assertFalse(optional(() -> viewReload.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> viewReload.users()).isPresent());
  }

  @Test
  public void signout() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.signout().click();
    $(SigninViewElement.class).waitForFirst();
  }
}
