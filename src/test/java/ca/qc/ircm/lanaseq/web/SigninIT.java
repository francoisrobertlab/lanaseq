package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.web.SigninView.DISABLED;
import static ca.qc.ircm.lanaseq.web.SigninView.FAIL;
import static ca.qc.ircm.lanaseq.web.SigninView.LOCKED;
import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordViewElement;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SigninView}.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class SigninIT extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(SigninView.class);
  @Autowired
  private transient SecurityConfiguration configuration;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void sign_Fail_invalid_username() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("not.exists@ircm.qc.ca");
    view.getPasswordField().setValue("notright");
    view.getSubmitButton().click();
    view = $(SigninViewElement.class).waitForFirst();
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.getErrorMessage());
    assertNotNull(getDriver().getCurrentUrl());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
    assertNull(getDriver().manage().getCookieNamed("remember-me"));
  }

  @BrowserTest
  public void sign_Fail_invalid_password() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
    view.getPasswordField().setValue("notright");
    view.getSubmitButton().click();
    view = $(SigninViewElement.class).waitForFirst();
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.getErrorMessage());
    assertNotNull(getDriver().getCurrentUrl());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
    assertNull(getDriver().manage().getCookieNamed("remember-me"));
  }

  @BrowserTest
  public void sign_Disabled() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("ava.martin@ircm.qc.ca");
    view.getPasswordField().setValue("password");
    view.getSubmitButton().click();
    view = $(SigninViewElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + DISABLED, null, currentLocale()),
        view.getErrorMessage());
    assertNotNull(getDriver().getCurrentUrl());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
    assertNull(getDriver().manage().getCookieNamed("remember-me"));
  }

  @BrowserTest
  public void sign_Locked() {
    open();
    SigninViewElement view;
    for (int i = 0; i < 6; i++) {
      view = $(SigninViewElement.class).waitForFirst();
      view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
      view.getPasswordField().setValue("notright");
      view.getSubmitButton().click();
      try {
        Thread.sleep(1000); // Wait for page to load.
      } catch (InterruptedException e) {
        throw new IllegalStateException("Sleep was interrupted", e);
      }
    }
    view = $(SigninViewElement.class).waitForFirst();
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + LOCKED,
            new Object[]{configuration.lockDuration().getSeconds() / 60}, currentLocale()),
        view.getErrorMessage());
    assertNotNull(getDriver().getCurrentUrl());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
    assertNull(getDriver().manage().getCookieNamed("remember-me"));
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
    Assertions.assertEquals("/", rememberMeCookie.getPath());
    Assertions.assertNotEquals("pass1", rememberMeCookie.getValue());
  }

  @BrowserTest
  public void forgotPassword() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getForgotPasswordButton().click();
    Assertions.assertEquals(viewUrl(ForgotPasswordView.VIEW_NAME), getDriver().getCurrentUrl());
    $(ForgotPasswordViewElement.class).waitForFirst();
    assertNull(getDriver().manage().getCookieNamed("remember-me"));
  }

  @BrowserTest
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void already_User() {
    open();
    $(DatasetsViewElement.class).waitForFirst();
  }
}
