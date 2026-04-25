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
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewComponent;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.SeleniumTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordViewComponent;
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
@SeleniumTestAnnotations
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
    SigninViewComponent view = waitUntil(SigninViewComponent.find());
    view.username().sendKeys("not.exists@ircm.qc.ca");
    view.password().sendKeys("notright");
    view.signin().click();
    waitUntil(d -> Optional.ofNullable(d.getCurrentUrl()).orElse("")
        .startsWith(viewUrl(VIEW_NAME) + "?"));
    view = waitUntil(SigninViewComponent.find());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.errorMessageDescription().getText());
    assertNotNull(driver.getCurrentUrl());
    assertEquals(viewUrl(VIEW_NAME) + "?" + FAIL, driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void sign_Fail_invalid_password() {
    open();
    SigninViewComponent view = waitUntil(SigninViewComponent.find());
    view.username().sendKeys("olivia.brown@ircm.qc.ca");
    view.password().sendKeys("notright");
    view.signin().click();
    waitUntil(d -> Optional.ofNullable(d.getCurrentUrl()).orElse("")
        .startsWith(viewUrl(VIEW_NAME) + "?"));
    view = waitUntil(SigninViewComponent.find());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.errorMessageDescription().getText());
    assertEquals(viewUrl(VIEW_NAME) + "?" + FAIL, driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void sign_Disabled() {
    open();
    SigninViewComponent view = waitUntil(SigninViewComponent.find());
    view.username().sendKeys("ava.martin@ircm.qc.ca");
    view.password().sendKeys("password");
    view.signin().click();
    waitUntil(d -> Optional.ofNullable(d.getCurrentUrl()).orElse("")
        .startsWith(viewUrl(VIEW_NAME) + "?"));
    view = waitUntil(SigninViewComponent.find());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + DISABLED, null, currentLocale()),
        view.errorMessageDescription().getText());
    assertEquals(viewUrl(VIEW_NAME) + "?" + DISABLED, driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  public void sign_Locked() {
    open();
    SigninViewComponent view;
    for (int i = 0; i < 6; i++) {
      view = waitUntil(SigninViewComponent.find());
      view.username().sendKeys("olivia.brown@ircm.qc.ca");
      view.password().sendKeys("notright");
      view.signin().click();
      try {
        Thread.sleep(1000); // Wait for page to load.
      } catch (InterruptedException e) {
        throw new IllegalStateException("Sleep was interrupted", e);
      }
    }
    view = waitUntil(SigninViewComponent.find());
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
    SigninViewComponent view = waitUntil(SigninViewComponent.find());
    view.username().sendKeys("jonh.smith@ircm.qc.ca");
    view.password().sendKeys("pass1");
    view.signin().click();
    waitUntil(DatasetsViewComponent.find());
    assertEquals(viewUrl(DatasetsView.VIEW_NAME), driver.getCurrentUrl());
    Cookie rememberMeCookie = driver.manage().getCookieNamed("remember-me");
    assertNotNull(rememberMeCookie);
    Assertions.assertEquals("/", rememberMeCookie.getPath());
    Assertions.assertNotEquals("pass1", rememberMeCookie.getValue());
  }

  @Test
  public void forgotPassword() {
    open();
    SigninViewComponent view = waitUntil(SigninViewComponent.find());
    view.forgotPassword().click();
    waitUntil(ForgotPasswordViewComponent.find());
    assertEquals(viewUrl(ForgotPasswordView.VIEW_NAME), driver.getCurrentUrl());
    assertNull(driver.manage().getCookieNamed("remember-me"));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void already_User() {
    open();
    waitUntil(DatasetsViewComponent.find());
    assertEquals(viewUrl(DatasetsView.VIEW_NAME), driver.getCurrentUrl());
  }
}
