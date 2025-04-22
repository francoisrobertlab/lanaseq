package ca.qc.ircm.lanaseq.web;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SignoutView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutViewIT extends AbstractBrowserTestCase {

  @BrowserTest
  @WithAnonymousUser
  public void security_Anonymous() {
    openView(SignoutView.VIEW_NAME);

    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void security_User() {
    openView(SignoutView.VIEW_NAME);

    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void signout() {
    openView(DatasetsView.VIEW_NAME);
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    openView(SignoutView.VIEW_NAME);
    $(SigninViewElement.class).waitForFirst();
  }
}
