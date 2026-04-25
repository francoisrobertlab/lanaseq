package ca.qc.ircm.lanaseq.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewComponent;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.SeleniumTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SignoutView} using Selenium.
 */
@SeleniumTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutIT extends AbstractSeleniumTestCase {

  private void open() {
    openView(DatasetsView.VIEW_NAME);
  }

  @Test
  public void signout() {
    openView(DatasetsView.VIEW_NAME);
    waitUntil(ViewLayoutComponent.find());
    openView(SignoutView.VIEW_NAME);
    waitUntil(SigninViewComponent.find());
  }

  @Test
  @WithAnonymousUser
  public void signout_clear_rememberme() {
    openView(SigninView.VIEW_NAME);
    SigninViewComponent view = waitUntil(SigninViewComponent.find());
    view.username().sendKeys("jonh.smith@ircm.qc.ca");
    view.password().sendKeys("pass1");
    view.signin().click();
    waitUntil(DatasetsViewComponent.find());
    assertNotNull(driver.manage().getCookieNamed("remember-me"));
    openView(SignoutView.VIEW_NAME);
    waitUntil(SigninViewComponent.find());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void signout_sidenav() {
    open();
    ViewLayoutComponent layout = waitUntil(ViewLayoutComponent.find());
    layout.signout().click();
    waitUntil(SigninViewComponent.find());
  }

  @Test
  @WithAnonymousUser
  public void signout_sidenav_clear_rememberme() {
    openView(SigninView.VIEW_NAME);
    SigninViewComponent view = waitUntil(SigninViewComponent.find());
    view.username().sendKeys("jonh.smith@ircm.qc.ca");
    view.password().sendKeys("pass1");
    view.signin().click();
    waitUntil(DatasetsViewComponent.find());
    assertNotNull(driver.manage().getCookieNamed("remember-me"));
    ViewLayoutComponent layout = waitUntil(ViewLayoutComponent.find());
    layout.signout().click();
    waitUntil(SigninViewComponent.find());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }
}
