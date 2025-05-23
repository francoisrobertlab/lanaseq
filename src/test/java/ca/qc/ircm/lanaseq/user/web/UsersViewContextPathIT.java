package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.ViewLayoutElement;
import com.vaadin.testbench.BrowserTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link UsersView}.
 */
@TestBenchTestAnnotations
@ActiveProfiles({"integration-test", "context-path"})
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewContextPathIT extends AbstractBrowserTestCase {

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void switchUser() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().select(2);

    view.switchUser().click();

    $(DatasetsViewElement.class).waitForFirst();
    ViewLayoutElement viewReload = $(ViewLayoutElement.class).waitForFirst();
    assertTrue(optional(viewReload::exitSwitchUser).isPresent());
    assertFalse(optional(viewReload::users).isPresent());
  }
}
