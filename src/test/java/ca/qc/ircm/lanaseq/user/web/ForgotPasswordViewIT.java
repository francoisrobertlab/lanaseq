package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.SAVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.mail.MailConfiguration;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordRepository;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration tests for {@link ForgotPasswordView}.
 */
@ServiceTestAnnotations
@WithAnonymousUser
public class ForgotPasswordViewIT extends SpringBrowserlessTest {

  @RegisterExtension
  static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);
  private static final String MESSAGE_PREFIX = messagePrefix(ForgotPasswordView.class);
  private static final String SERVICE_PREFIX = messagePrefix(ForgotPasswordService.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordViewIT.class);
  @Autowired
  private ForgotPasswordRepository repository;
  @Autowired
  private MailConfiguration mailConfiguration;
  @Autowired
  private AppConfiguration appConfiguration;
  @Autowired
  private MessageSource messageSource;
  private final String email = "olivia.brown@ircm.qc.ca";

  @DynamicPropertySource
  public static void springMailProperties(DynamicPropertyRegistry registry) {
    logger.info("Setting spring.mail.port to {}", ServerSetupTest.SMTP.getPort());
    registry.add("spring.mail.port", () -> String.valueOf(ServerSetupTest.SMTP.getPort()));
    registry.add("mail.enabled", () -> "true");
  }

  @Test
  public void save() throws MessagingException {
    ForgotPasswordView view = navigate(ForgotPasswordView.class);
    test(view.email).setValue(email);
    test(view.save).click();

    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{email},
        UI.getCurrent().getLocale()), test(notification).getText());
    List<ForgotPassword> forgotPasswords = repository.findByUserEmail(email);
    ForgotPassword forgotPassword = forgotPasswords.get(forgotPasswords.size() - 1);
    assertEquals(4, forgotPasswords.size());
    assertTrue($(SigninView.class).exists());

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    MimeMessage message = messages[0];
    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + "application.name", null,
        UI.getCurrent().getLocale());
    String subject = messageSource.getMessage(SERVICE_PREFIX + "subject",
        new Object[]{applicationName}, UI.getCurrent().getLocale());
    assertEquals(subject, message.getSubject());
    assertNotNull(message.getFrom());
    assertEquals(1, message.getFrom().length);
    assertEquals(new InternetAddress(mailConfiguration.from()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(email), message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    String body = GreenMailUtil.getBody(message);
    String url = appConfiguration.getUrl(
        UseForgotPasswordView.VIEW_NAME + "/" + forgotPassword.getId() + "/"
            + forgotPassword.getConfirmNumber());
    assertTrue(body.contains(url), url + " not found in email " + body);
  }
}
