package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.web.SigninView.DISABLED;
import static ca.qc.ircm.lanaseq.web.SigninView.FAIL;
import static ca.qc.ircm.lanaseq.web.SigninView.LOCKED;
import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewPage;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordViewPage;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SigninView} using Selenium.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class SigninIT extends AbstractSeleniumTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(SigninView.class);
  @Autowired
  private transient SecurityConfiguration configuration;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void sign_Fail_invalid_username() {
    open();
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.username().sendKeys("not.exists@ircm.qc.ca");
    view.password().sendKeys("notright");
    view.signin().click();
    waitUntil(d -> Optional.ofNullable(d.getCurrentUrl()).orElse("")
        .startsWith(viewUrl(VIEW_NAME) + "?"));
    view = waitUntil(SigninViewPage.find());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.errorMessageDescription().getText());
    assertNotNull(driver.getCurrentUrl());
    assertEquals(viewUrl(VIEW_NAME) + "?" + FAIL, driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void sign_Fail_invalid_password() {
    open();
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.username().sendKeys("olivia.brown@ircm.qc.ca");
    view.password().sendKeys("notright");
    view.signin().click();
    waitUntil(d -> Optional.ofNullable(d.getCurrentUrl()).orElse("")
        .startsWith(viewUrl(VIEW_NAME) + "?"));
    view = waitUntil(SigninViewPage.find());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.errorMessageDescription().getText());
    assertEquals(viewUrl(VIEW_NAME) + "?" + FAIL, driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void sign_Disabled() {
    open();
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.username().sendKeys("ava.martin@ircm.qc.ca");
    view.password().sendKeys("password");
    view.signin().click();
    waitUntil(d -> Optional.ofNullable(d.getCurrentUrl()).orElse("")
        .startsWith(viewUrl(VIEW_NAME) + "?"));
    view = waitUntil(SigninViewPage.find());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + DISABLED, null, currentLocale()),
        view.errorMessageDescription().getText());
    assertEquals(viewUrl(VIEW_NAME) + "?" + DISABLED, driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void sign_Locked() {
    open();
    SigninViewPage view;
    for (int i = 0; i < 6; i++) {
      view = waitUntil(SigninViewPage.find());
      view.username().sendKeys("olivia.brown@ircm.qc.ca");
      view.password().sendKeys("notright");
      view.signin().click();
      try {
        Thread.sleep(1000); // Wait for page to load.
      } catch (InterruptedException e) {
        throw new IllegalStateException("Sleep was interrupted", e);
      }
    }
    view = waitUntil(SigninViewPage.find());
    System.out.println(view.errorMessageDescription().getText());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + LOCKED,
            new Object[]{configuration.lockDuration().getSeconds() / 60}, currentLocale()),
        view.errorMessageDescription().getText());
    assertEquals(viewUrl(VIEW_NAME) + "?" + LOCKED, driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void sign() {
    open();
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.username().sendKeys("jonh.smith@ircm.qc.ca");
    view.password().sendKeys("pass1");
    view.signin().click();
    waitUntil(DatasetsViewPage.find());
    assertEquals(viewUrl(DatasetsView.VIEW_NAME), driver.getCurrentUrl());
    Cookie rememberMeCookie = driver.manage().getCookieNamed("remember-me");
    assertNotNull(rememberMeCookie);
    Assertions.assertEquals("/", rememberMeCookie.getPath());
    Assertions.assertNotEquals("pass1", rememberMeCookie.getValue());
  }

  @Test
  public void forgotPassword() {
    open();
    SigninViewPage view = waitUntil(SigninViewPage.find());
    view.forgotPassword().click();
    waitUntil(ForgotPasswordViewPage.find());
    assertEquals(viewUrl(ForgotPasswordView.VIEW_NAME), driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void already_User() {
    open();
    waitUntil(DatasetsViewPage.find());
    assertEquals(viewUrl(DatasetsView.VIEW_NAME), driver.getCurrentUrl());
  }
}
