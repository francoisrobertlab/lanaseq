package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.web.SigninView.DISABLED;
import static ca.qc.ircm.lanaseq.web.SigninView.FAIL;
import static ca.qc.ircm.lanaseq.web.SigninView.LOCKED;
import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordViewElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SigninView}.
 */
@TestBenchTestAnnotations
public class SigninViewItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(SigninView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private transient SecurityConfiguration configuration;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void title() throws Throwable {
    open();

    String applicationName =
        messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null, currentLocale());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[] { applicationName },
        currentLocale()), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.getUsernameField()).isPresent());
    assertTrue(optional(() -> view.getPasswordField()).isPresent());
    assertTrue(optional(() -> view.getSubmitButton()).isPresent());
    assertTrue(optional(() -> view.getForgotPasswordButton()).isPresent());
  }

  @Test
  public void sign_Fail() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
    view.getPasswordField().setValue("notright");
    view.getSubmitButton().click();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FAIL, null, currentLocale()),
        view.getErrorMessage());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @Test
  public void sign_Disabled() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("ava.martin@ircm.qc.ca");
    view.getPasswordField().setValue("password");
    view.getSubmitButton().click();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + DISABLED, null, currentLocale()),
        view.getErrorMessage());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @Test
  public void sign_Locked() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    for (int i = 0; i < 6; i++) {
      view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
      view.getPasswordField().setValue("notright");
      view.getSubmitButton().click();
    }
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + LOCKED,
            new Object[] { configuration.lockDuration().getSeconds() / 60 }, currentLocale()),
        view.getErrorMessage());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @Test
  public void sign() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("jonh.smith@ircm.qc.ca");
    view.getPasswordField().setValue("pass1");
    view.getSubmitButton().click();
    $(DatasetsViewElement.class).waitForFirst();
  }

  @Test
  public void forgotPassword() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getForgotPasswordButton().click();
    assertEquals(viewUrl(ForgotPasswordView.VIEW_NAME), getDriver().getCurrentUrl());
    $(ForgotPasswordViewElement.class).waitForFirst();
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void already_User() throws Throwable {
    open();
    $(DatasetsViewElement.class).waitForFirst();
  }
}
