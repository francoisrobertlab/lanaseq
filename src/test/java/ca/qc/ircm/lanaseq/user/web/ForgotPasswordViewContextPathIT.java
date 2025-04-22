package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.mail.MailConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordRepository;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration tests for {@link ForgotPasswordView}.
 */
@TestBenchTestAnnotations
@ActiveProfiles({"integration-test", "context-path"})
@WithAnonymousUser
public class ForgotPasswordViewContextPathIT extends AbstractBrowserTestCase {

  @RegisterExtension
  static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);
  private static final String MESSAGE_PREFIX = messagePrefix(ForgotPasswordView.class);
  private static final String SERVICE_PREFIX = messagePrefix(ForgotPasswordService.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final Logger logger = LoggerFactory.getLogger(
      ForgotPasswordViewContextPathIT.class);
  @Autowired
  private ForgotPasswordRepository repository;
  @Autowired
  private MailConfiguration mailConfiguration;
  @Autowired
  private MessageSource messageSource;
  private final String email = "olivia.brown@ircm.qc.ca";

  @DynamicPropertySource
  public static void springMailProperties(DynamicPropertyRegistry registry) {
    logger.info("Setting spring.mail.port to {}", ServerSetupTest.SMTP.getPort());
    registry.add("spring.mail.port", () -> String.valueOf(ServerSetupTest.SMTP.getPort()));
    registry.add("mail.enabled", () -> "true");
  }

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void save() throws MessagingException {
    open();
    ForgotPasswordViewElement view = $(ForgotPasswordViewElement.class).waitForFirst();
    view.email().setValue(email);
    view.save().click();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{email}, currentLocale()),
        notification.getText());
    List<ForgotPassword> forgotPasswords = repository.findByUserEmail(email);
    ForgotPassword forgotPassword = forgotPasswords.get(forgotPasswords.size() - 1);
    Assertions.assertEquals(4, forgotPasswords.size());
    $(SigninViewElement.class).waitForFirst();

    MimeMessage[] messages = greenMail.getReceivedMessages();
    Assertions.assertEquals(1, messages.length);
    MimeMessage message = messages[0];
    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + "application.name", null,
        currentLocale());
    String subject = messageSource.getMessage(SERVICE_PREFIX + "subject",
        new Object[]{applicationName}, currentLocale());
    Assertions.assertEquals(subject, message.getSubject());
    assertNotNull(message.getFrom());
    Assertions.assertEquals(1, message.getFrom().length);
    Assertions.assertEquals(new InternetAddress(mailConfiguration.from()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    Assertions.assertEquals(1, message.getRecipients(RecipientType.TO).length);
    Assertions.assertEquals(new InternetAddress(email), message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    String body = GreenMailUtil.getBody(message);
    String url = viewUrl(UseForgotPasswordView.VIEW_NAME) + "/" + forgotPassword.getId() + "/"
        + forgotPassword.getConfirmNumber();
    assertTrue(body.contains(url), url + " not found in email " + body);
  }
}
