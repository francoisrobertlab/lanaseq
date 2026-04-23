package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewPage;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link SigninView} using Selenium and a non-empty context path.
 */
@TestBenchTestAnnotations
@ActiveProfiles({"integration-test", "context-path"})
@WithAnonymousUser
public class SigninContextPathIT extends AbstractSeleniumTestCase {

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void sign() {
    open();
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.username().sendKeys("jonh.smith@ircm.qc.ca");
    view.password().sendKeys("pass1");
    view.signin().click();
    waitUntil(DatasetsViewPage.find());
    Cookie rememberMeCookie = driver.manage().getCookieNamed("remember-me");
    assertNotNull(rememberMeCookie);
    assertEquals(contextPath, rememberMeCookie.getPath());
    assertNotEquals("pass1", rememberMeCookie.getValue());
  }
}
