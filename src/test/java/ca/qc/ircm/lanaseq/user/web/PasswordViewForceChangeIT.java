package ca.qc.ircm.lanaseq.user.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.testbench.BrowserTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link PasswordView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("christian.poitras@ircm.qc.ca")
public class PasswordViewForceChangeIT extends AbstractBrowserTestCase {

  @BrowserTest
  @WithAnonymousUser
  public void sign_ForceChangePassword() {
    openView(SigninView.VIEW_NAME);
    SigninViewElement signinView = $(SigninViewElement.class).waitForFirst();
    signinView.getUsernameField().setValue("christian.poitras@ircm.qc.ca");
    signinView.getPasswordField().setValue("pass1");
    signinView.getSubmitButton().click();
    PasswordViewElement view = $(PasswordViewElement.class).waitForFirst();
    assertTrue(optional(view::header).isPresent());
    assertTrue(optional(() -> view.passwords().password()).isPresent());
    assertTrue(optional(() -> view.passwords().passwordConfirm()).isPresent());
    assertTrue(optional(view::save).isPresent());
  }
}
