package ca.qc.ircm.lanaseq.user.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewPage;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.ViewLayoutPage;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for impersonating users and exiting impersonation using Selenium.
 */
@TestBenchTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class SwitchUserIT extends AbstractSeleniumTestCase {

  @Test
  public void switchUser() {
    openView(UsersView.VIEW_NAME);
    UsersViewPage view = waitUntil(UsersViewPage.find());
    view.users().select(2);

    view.switchUser().click();

    waitUntil(DatasetsViewPage.find());
    ViewLayoutPage layout = waitUntil(ViewLayoutPage.find());
    assertTrue(optional(layout::exitSwitchUser).isPresent());
    assertFalse(optional(layout::users).isPresent());
  }

  @Test
  public void exitSwitchUser() {
    openView(DatasetsView.VIEW_NAME);
    waitUntil(ViewLayoutPage.find()).users().click();
    UsersViewPage view = waitUntil(UsersViewPage.find());
    view.users().select(2);
    view.switchUser().click();
    waitUntil(DatasetsViewPage.find());
    ViewLayoutPage layout = waitUntil(ViewLayoutPage.find());
    layout.profile().click();
    openView(ExitSwitchUserView.VIEW_NAME);
    waitUntil(DatasetsViewPage.find());
    layout = waitUntil(ViewLayoutPage.find());
    assertFalse(optional(layout::exitSwitchUser).isPresent());
    assertTrue(optional(layout::users).isPresent());
  }
}
