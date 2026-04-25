package ca.qc.ircm.lanaseq.web;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.browserless.SpringBrowserlessTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SignoutView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutViewIT extends SpringBrowserlessTest {

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(SignoutView.VIEW_NAME, SigninView.class);
  }
}
