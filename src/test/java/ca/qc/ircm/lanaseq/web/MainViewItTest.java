package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.MainView.VIEW_NAME;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link MainView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class MainViewItTest extends AbstractBrowserTestCase {

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
  public void userRedirected() {
    open();

    $(DatasetsViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void managerRedirected() {
    open();

    $(DatasetsViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void adminRedirected() {
    open();

    $(DatasetsViewElement.class).waitForFirst();
  }
}
