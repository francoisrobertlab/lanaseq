package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.Cookie;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link SigninView}.
 */
@TestBenchTestAnnotations
@ActiveProfiles({"integration-test", "context-path"})
@WithAnonymousUser
public class SigninContextPathIT extends AbstractBrowserTestCase {

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void sign() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("jonh.smith@ircm.qc.ca");
    view.getPasswordField().setValue("pass1");
    view.getSubmitButton().click();
    $(DatasetsViewElement.class).waitForFirst();
    Cookie rememberMeCookie = getDriver().manage().getCookieNamed("remember-me");
    assertNotNull(rememberMeCookie);
    Assertions.assertEquals(contextPath, rememberMeCookie.getPath());
    Assertions.assertNotEquals("pass1", rememberMeCookie.getValue());
  }
}
