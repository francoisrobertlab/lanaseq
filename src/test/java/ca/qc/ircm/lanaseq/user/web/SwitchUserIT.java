package ca.qc.ircm.lanaseq.user.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewComponent;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.SeleniumTestAnnotations;
import ca.qc.ircm.lanaseq.web.ViewLayoutComponent;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for impersonating users and exiting impersonation using Selenium.
 */
@SeleniumTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class SwitchUserIT extends AbstractSeleniumTestCase {

  @Test
  public void switchUser() {
    openView(UsersView.VIEW_NAME);
    UsersViewComponent view = waitUntil(UsersViewComponent.find());
    view.users().select(2);

    view.switchUser().click();

    waitUntil(DatasetsViewComponent.find());
    ViewLayoutComponent layout = waitUntil(ViewLayoutComponent.find());
    assertTrue(optional(layout::exitSwitchUser).isPresent());
    assertFalse(optional(layout::users).isPresent());
  }

  @Test
  public void exitSwitchUser() {
    openView(DatasetsView.VIEW_NAME);
    waitUntil(ViewLayoutComponent.find()).users().click();
    UsersViewComponent view = waitUntil(UsersViewComponent.find());
    view.users().select(2);
    view.switchUser().click();
    waitUntil(DatasetsViewComponent.find());
    ViewLayoutComponent layout = waitUntil(ViewLayoutComponent.find());
    layout.profile().click();
    openView(ExitSwitchUserView.VIEW_NAME);
    waitUntil(DatasetsViewComponent.find());
    layout = waitUntil(ViewLayoutComponent.find());
    assertFalse(optional(layout::exitSwitchUser).isPresent());
    assertTrue(optional(layout::users).isPresent());
  }
}
