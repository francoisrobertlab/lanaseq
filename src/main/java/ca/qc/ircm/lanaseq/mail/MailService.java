package ca.qc.ircm.lanaseq.mail;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Service class for sending emails.
 */
@Component
public class MailService {
  private final Logger logger = LoggerFactory.getLogger(MailService.class);
  @Autowired
  private MailConfiguration mailConfiguration;
  @Autowired
  private JavaMailSender mailSender;
  @Autowired
  private AuthenticatedUser authenticatedUser;

  protected MailService() {
  }

  protected MailService(MailConfiguration mailConfiguration, JavaMailSender mailSender,
      AuthenticatedUser authenticatedUser) {
    this.mailConfiguration = mailConfiguration;
    this.mailSender = mailSender;
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * Creates a plain text email.
   *
   * @return plain text email
   * @throws MessagingException
   *           could not create email
   */
  public MimeMessageHelper textEmail() throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);
    helper.setFrom(mailConfiguration.from());
    helper.setSubject(mailConfiguration.subject());
    helper.setText("");
    return helper;
  }

  /**
   * Creates a multipart email.
   *
   * @return multipart email
   * @throws MessagingException
   *           could not create email
   */
  public MimeMessageHelper htmlEmail() throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(mailConfiguration.from());
    helper.setSubject(mailConfiguration.subject());
    return helper;
  }

  /**
   * Sends an email.
   *
   * @param email
   *          email with text content only
   * @throws MessagingException
   *           could not send email
   */
  public void send(MimeMessageHelper email) throws MessagingException {
    if (!mailConfiguration.enabled()) {
      return;
    }

    try {
      mailSender.send(email.getMimeMessage());
    } catch (MailException e) {
      logger.error("Could not send error email with content {}", message(email), e);
    }
  }

  private String message(MimeMessageHelper email) {
    try {
      if (email.isMultipart()) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamUtils.copy(email.getMimeMultipart().getBodyPart(0).getInputStream(), output);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
      } else {
        return String.valueOf(email.getMimeMessage().getContent());
      }
    } catch (IOException | IllegalStateException | MessagingException e) {
      return "<exception>: " + e.getMessage();
    }
  }

  /**
   * Sends an email to the system administrator containing error.
   *
   * @param error
   *          error to send
   */
  public void sendError(Throwable error) {
    if (!mailConfiguration.enabled()) {
      return;
    }

    StringBuilder message = new StringBuilder();
    message.append("User:");
    message.append(authenticatedUser.getUser().map(User::getEmail).orElse("null"));
    message.append("\n");
    message.append(error.getMessage());
    message.append("\n");
    StringWriter stringWriter = new StringWriter();
    error.printStackTrace(new PrintWriter(stringWriter));
    message.append(stringWriter.toString());
    try {
      MimeMessageHelper email = textEmail();
      email.setTo(mailConfiguration.to());
      email.setText(message.toString());
      send(email);
    } catch (MessagingException e) {
      logger.error("Could not send error email with content {}", message.toString(), e);
    }
  }
}
