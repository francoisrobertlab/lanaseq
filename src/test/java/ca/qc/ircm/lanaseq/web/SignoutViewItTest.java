package ca.qc.ircm.lanaseq.web;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SignoutView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutViewItTest extends AbstractTestBenchTestCase {

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    openView(SignoutView.VIEW_NAME);

    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  public void security_User() {
    openView(SignoutView.VIEW_NAME);

    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  public void signout() {
    openView(DatasetsView.VIEW_NAME);
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    openView(SignoutView.VIEW_NAME);
    $(SigninViewElement.class).waitForFirst();
  }
}
