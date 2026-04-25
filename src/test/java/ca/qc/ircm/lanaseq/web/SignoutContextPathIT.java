package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;

import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.SeleniumTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link SignoutView} using Selenium and a non-empty context path.
 */
@SeleniumTestAnnotations
@ActiveProfiles({"integration-test", "context-path"})
@WithUserDetails("jonh.smith@ircm.qc.ca")
class SignoutContextPathIT extends AbstractSeleniumTestCase {

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void signout() {
    open();
    ViewLayoutComponent layout = waitUntil(ViewLayoutComponent.find());
    layout.signout().click();
    waitUntil(SigninViewComponent.find());
  }
}
