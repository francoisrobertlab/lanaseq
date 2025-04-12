package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.web.SigninView.DISABLED;
import static ca.qc.ircm.lanaseq.web.SigninView.FAIL;
import static ca.qc.ircm.lanaseq.web.SigninView.LOCKED;
import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordViewElement;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SigninView}.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class SigninViewItTest extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(SigninView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private transient SecurityConfiguration configuration;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void title() {
    open();

    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null,
        currentLocale());
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[]{applicationName},
            currentLocale()), getDriver().getTitle());
  }

  @BrowserTest
  public void fieldsExistence() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    assertTrue(optional(view::getUsernameField).isPresent());
    assertTrue(optional(view::getPasswordField).isPresent());
    assertTrue(optional(view::getSubmitButton).isPresent());
    assertTrue(optional(view::getForgotPasswordButton).isPresent());
  }

  @BrowserTest
  public void sign_Fail() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
    view.getPasswordField().setValue("notright");
    view.getSubmitButton().click();
    waitUntil(driver -> driver != null && driver.getCurrentUrl() != null && driver.getCurrentUrl()
        .endsWith("?" + FAIL));
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.getErrorMessage());
    assertNotNull(getDriver().getCurrentUrl());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @BrowserTest
  public void sign_Disabled() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("ava.martin@ircm.qc.ca");
    view.getPasswordField().setValue("password");
    view.getSubmitButton().click();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + DISABLED, null, currentLocale()),
        view.getErrorMessage());
    assertNotNull(getDriver().getCurrentUrl());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @BrowserTest
  public void sign_Locked() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    for (int i = 0; i < 6; i++) {
      view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
      view.getPasswordField().setValue("notright");
      view.getSubmitButton().click();
      try {
        Thread.sleep(1000); // Wait for page to load.
      } catch (InterruptedException e) {
        throw new IllegalStateException("Sleep was interrupted", e);
      }
    }
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + LOCKED,
            new Object[]{configuration.lockDuration().getSeconds() / 60}, currentLocale()),
        view.getErrorMessage());
    assertNotNull(getDriver().getCurrentUrl());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @BrowserTest
  public void sign() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("jonh.smith@ircm.qc.ca");
    view.getPasswordField().setValue("pass1");
    view.getSubmitButton().click();
    $(DatasetsViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void forgotPassword() {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getForgotPasswordButton().click();
    Assertions.assertEquals(viewUrl(ForgotPasswordView.VIEW_NAME), getDriver().getCurrentUrl());
    $(ForgotPasswordViewElement.class).waitForFirst();
  }

  @BrowserTest
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void already_User() {
    open();
    $(DatasetsViewElement.class).waitForFirst();
  }
}
