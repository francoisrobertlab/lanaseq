package ca.qc.ircm.lanaseq.user.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.security.web.AccessDeniedViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import ca.qc.ircm.lanaseq.web.ViewLayoutElement;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ExitSwitchUserView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class ExitSwitchUserViewItTest extends AbstractTestBenchTestCase {
  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void security_User() throws Throwable {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(AccessDeniedViewElement.class).waitForFirst();
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void security_Manager() throws Throwable {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(AccessDeniedViewElement.class).waitForFirst();
  }

  @Test
  public void security_Admin() throws Throwable {
    openView(ExitSwitchUserView.VIEW_NAME);

    $(AccessDeniedViewElement.class).waitForFirst();
  }

  @Test
  public void exitSwitchUser() throws Throwable {
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
    assertFalse(optional(() -> viewReload.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> viewReload.users()).isPresent());
  }
}
