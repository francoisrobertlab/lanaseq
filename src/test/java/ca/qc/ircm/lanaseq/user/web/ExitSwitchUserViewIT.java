package ca.qc.ircm.lanaseq.user.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.security.web.AccessDeniedViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import ca.qc.ircm.lanaseq.web.ViewLayoutElement;
import com.vaadin.testbench.BrowserTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ExitSwitchUserView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class ExitSwitchUserViewIT extends AbstractBrowserTestCase {

  @BrowserTest
  @WithAnonymousUser
  public void security_Anonymous() {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void security_User() {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(AccessDeniedViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void security_Manager() {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(AccessDeniedViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void security_Admin() {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(AccessDeniedViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void exitSwitchUser() {
    openView(DatasetsView.VIEW_NAME);
    $(ViewLayoutElement.class).waitForFirst().users().click();
    UsersViewElement usersView = $(UsersViewElement.class).waitForFirst();
    usersView.users().select(2);
    usersView.switchUser().click();
    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.profile().click();
    openView(ExitSwitchUserView.VIEW_NAME);
    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement viewReload = $(ViewLayoutElement.class).waitForFirst();
    assertFalse(optional(viewReload::exitSwitchUser).isPresent());
    assertTrue(optional(viewReload::users).isPresent());
  }
}
