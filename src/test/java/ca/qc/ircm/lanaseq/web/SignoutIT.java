package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ViewLayout}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SignoutIT extends AbstractBrowserTestCase {

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void signout() {
    openView(DatasetsView.VIEW_NAME);
    $(ViewLayoutElement.class).waitForFirst();
    openView(SignoutView.VIEW_NAME);
    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithAnonymousUser
  public void signout_clear_rememberme() {
    openView(SigninView.VIEW_NAME);
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("jonh.smith@ircm.qc.ca");
    view.getPasswordField().setValue("pass1");
    view.getSubmitButton().click();
    $(DatasetsViewElement.class).waitForFirst();
    assertNotNull(getDriver().manage().getCookieNamed("remember-me"));
    openView(SignoutView.VIEW_NAME);
    $(SigninViewElement.class).waitForFirst();
    assertNull(getDriver().manage().getCookieNamed("remember-me"));
  }

  @BrowserTest
  public void signout_sidenav() {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).waitForFirst();
    view.signout().click();
    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithAnonymousUser
  public void signout_sidenav_clear_rememberme() {
    openView(SigninView.VIEW_NAME);
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("jonh.smith@ircm.qc.ca");
    view.getPasswordField().setValue("pass1");
    view.getSubmitButton().click();
    $(DatasetsViewElement.class).waitForFirst();
    assertNotNull(getDriver().manage().getCookieNamed("remember-me"));
    ViewLayoutElement layout = $(ViewLayoutElement.class).waitForFirst();
    layout.signout().click();
    $(SigninViewElement.class).waitForFirst();
    assertNull(getDriver().manage().getCookieNamed("remember-me"));
  }
}
