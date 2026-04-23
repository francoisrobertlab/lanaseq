package ca.qc.ircm.lanaseq.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewPage;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SignoutView} using Selenium.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutIT extends AbstractSeleniumTestCase {

  private void open() {
    openView(DatasetsView.VIEW_NAME);
  }

  @Test
  public void signout() {
    openView(DatasetsView.VIEW_NAME);
    waitUntil(ViewLayoutPage.find());
    openView(SignoutView.VIEW_NAME);
    waitUntil(SigninViewPage.find());
  }

  @Test
  @WithAnonymousUser
  public void signout_clear_rememberme() {
    openView(SigninView.VIEW_NAME);
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.username().sendKeys("jonh.smith@ircm.qc.ca");
    view.password().sendKeys("pass1");
    view.signin().click();
    waitUntil(DatasetsViewPage.find());
    assertNotNull(driver.manage().getCookieNamed("remember-me"));
    openView(SignoutView.VIEW_NAME);
    waitUntil(SigninViewPage.find());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void signout_sidenav() {
    open();
    ViewLayoutPage layout = waitUntil(ViewLayoutPage.find());
    layout.signout().click();
    waitUntil(SigninViewPage.find());
  }

  @Test
  @WithAnonymousUser
  public void signout_sidenav_clear_rememberme() {
    openView(SigninView.VIEW_NAME);
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.username().sendKeys("jonh.smith@ircm.qc.ca");
    view.password().sendKeys("pass1");
    view.signin().click();
    waitUntil(DatasetsViewPage.find());
    assertNotNull(driver.manage().getCookieNamed("remember-me"));
    ViewLayoutPage layout = waitUntil(ViewLayoutPage.find());
    layout.signout().click();
    waitUntil(SigninViewPage.find());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }
}
