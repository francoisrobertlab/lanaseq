package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.files.web.PublicFilesViewElement;
import ca.qc.ircm.lanaseq.jobs.web.JobsViewElement;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolsViewElement;
import ca.qc.ircm.lanaseq.sample.web.SamplesViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ProfileViewElement;
import ca.qc.ircm.lanaseq.user.web.UsersViewElement;
import com.vaadin.testbench.BrowserTest;
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
    assertTrue(optional(view::publicFiles).isPresent());
    assertTrue(optional(view::jobs).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertFalse(optional(view::users).isPresent());
    assertFalse(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
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
    assertTrue(optional(view::publicFiles).isPresent());
    assertTrue(optional(view::jobs).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertTrue(optional(view::users).isPresent());
    assertFalse(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
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
    assertTrue(optional(view::publicFiles).isPresent());
    assertTrue(optional(view::jobs).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertTrue(optional(view::users).isPresent());
    assertFalse(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
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
    assertTrue(optional(view::publicFiles).isPresent());
    assertTrue(optional(view::jobs).isPresent());
    assertTrue(optional(view::profile).isPresent());
    assertTrue(optional(view::users).isPresent());
    assertTrue(optional(view::exitSwitchUser).isPresent());
    assertTrue(optional(view::signout).isPresent());
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
  public void publicFiles() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.publicFiles().click();
    $(PublicFilesViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void jobs() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.jobs().click();
    $(JobsViewElement.class).waitForFirst();
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
}
