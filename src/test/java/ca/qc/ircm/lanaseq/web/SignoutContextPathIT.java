package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;

import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link ViewLayout}.
 */
@TestBenchTestAnnotations
@ActiveProfiles({"integration-test", "context-path"})
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutContextPathIT extends AbstractBrowserTestCase {

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void signout() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.signout().click();
    $(SigninViewElement.class).waitForFirst();
  }
}
