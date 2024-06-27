package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.MainView.VIEW_NAME;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link MainView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class MainViewItTest extends AbstractTestBenchTestCase {
  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();

    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  public void userRedirected() throws Throwable {
    open();

    $(DatasetsViewElement.class).waitForFirst();
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void managerRedirected() throws Throwable {
    open();

    $(DatasetsViewElement.class).waitForFirst();
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void adminRedirected() throws Throwable {
    open();

    $(DatasetsViewElement.class).waitForFirst();
  }
}
