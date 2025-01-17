package ca.qc.ircm.lanaseq.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimePart;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link MailService}.
 */
@ContextConfiguration
@NonTransactionalTestAnnotations
public class MailServiceTest {
  @RegisterExtension
  static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);
  private static final Logger logger = LoggerFactory.getLogger(MailServiceTest.class);
  @Autowired
  private MailService mailService;
  @Autowired
  private JavaMailSender mailSender;
  @Autowired
  private MailConfiguration mailConfiguration;
  @MockitoBean
  private AuthenticatedUser authenticatedUser;

  @DynamicPropertySource
  public static void springMailProperties(DynamicPropertyRegistry registry) {
    logger.info("Setting spring.mail.port to {}", ServerSetupTest.SMTP.getPort());
    registry.add("spring.mail.port", () -> String.valueOf(ServerSetupTest.SMTP.getPort()));
  }

  private Optional<String> plainContent(MimePart part) throws MessagingException, IOException {
    if (part.isMimeType("text/plain") && part.getContent() instanceof String) {
      return Optional.of(part.getContent().toString());
    } else if (part.getContent() instanceof Multipart) {
      Multipart mp = (Multipart) part.getContent();
      @SuppressWarnings("unchecked")
      Optional<String> content =
          (Optional<String>) IntStream.range(0, mp.getCount()).mapToObj(i -> {
            try {
              return plainContent((MimeBodyPart) mp.getBodyPart(i));
            } catch (MessagingException | IOException e) {
              return Optional.empty();
            }
          }).filter(Optional::isPresent).findAny().orElse(Optional.empty());
      return content;
    } else {
      return Optional.empty();
    }
  }

  private Optional<String> htmlContent(MimePart part) throws MessagingException, IOException {
    if (part.isMimeType("text/html")
        || part.getDataHandler().getContentType().startsWith("text/html")) {
      return Optional.of(part.getContent().toString());
    } else if (part.getContent() instanceof Multipart) {
      Multipart mp = (Multipart) part.getContent();
      @SuppressWarnings("unchecked")
      Optional<String> content =
          (Optional<String>) IntStream.range(0, mp.getCount()).mapToObj(i -> {
            try {
              return htmlContent((MimeBodyPart) mp.getBodyPart(i));
            } catch (MessagingException | IOException e) {
              return Optional.empty();
            }
          }).filter(Optional::isPresent).findAny().orElse(Optional.empty());
      return content;
    } else {
      return Optional.empty();
    }
  }

  @Test
  @Timeout(150)
  public void textEmail() throws Throwable {
    String content = "text message";
    MimeMessageHelper email = mailService.textEmail();
    email.setText(content);

    MimeMessage message = email.getMimeMessage();
    assertEquals("lanaseq", message.getSubject());
    assertEquals(1, message.getFrom().length);
    assertTrue(message.getFrom()[0] instanceof InternetAddress);
    assertEquals("lanaseq@ircm.qc.ca", ((InternetAddress) message.getFrom()[0]).getAddress());
    assertEquals(content, plainContent(message).orElse(""));
  }

  @Test
  @Timeout(150)
  public void htmlEmail() throws Throwable {
    String textContent = "text message";
    String htmlContent = "<html><body>html message</body></html>";
    MimeMessageHelper email = mailService.htmlEmail();
    email.setText(textContent, htmlContent);

    MimeMessage message = email.getMimeMessage();
    assertEquals("lanaseq", message.getSubject());
    assertEquals(1, message.getFrom().length);
    assertTrue(message.getFrom()[0] instanceof InternetAddress);
    assertEquals("lanaseq@ircm.qc.ca", ((InternetAddress) message.getFrom()[0]).getAddress());
    assertTrue(message.getContent() instanceof Multipart);
    assertEquals(htmlContent, htmlContent(message).orElse(""));
    assertEquals(textContent, plainContent(message).orElse(""));
  }

  @Test
  @Timeout(150)
  public void sendEmail_Text() throws Throwable {
    String receiver = "liam.li@ircm.qc.ca";
    String subject = "test subject";
    String content = "text message";
    MimeMessageHelper email = new MimeMessageHelper(mailSender.createMimeMessage());
    email.setFrom("lanaseq@ircm.qc.ca");
    email.addTo(receiver);
    email.setSubject(subject);
    email.setText(content);

    mailService.send(email);

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    MimeMessage message = messages[0];
    assertEquals(subject, message.getSubject());
    assertNotNull(message.getFrom());
    assertEquals(1, message.getFrom().length);
    assertEquals(new InternetAddress(mailConfiguration.from()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(receiver), message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    String body = GreenMailUtil.getBody(message);
    assertEquals(content, body);
  }

  @Test
  @Timeout(150)
  public void sendEmail_HtmlAndText() throws Throwable {
    String receiver = "liam.li@ircm.qc.ca";
    String subject = "test subject";
    String textContent = "text message";
    String htmlContent = "<html><body>html message</body></html>";
    MimeMessageHelper email = new MimeMessageHelper(mailSender.createMimeMessage(), true);
    email.setFrom("lanaseq@ircm.qc.ca");
    email.addTo(receiver);
    email.setSubject(subject);
    email.setText(textContent, htmlContent);

    mailService.send(email);

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    MimeMessage message = messages[0];
    assertEquals(subject, message.getSubject());
    assertNotNull(message.getFrom());
    assertEquals(1, message.getFrom().length);
    assertEquals(new InternetAddress(mailConfiguration.from()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(receiver), message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    assertEquals(htmlContent, htmlContent(message).orElse(""));
    assertEquals(textContent, plainContent(message).orElse(""));
  }

  @Test
  @Timeout(10)
  public void sendEmail_TwoHtmlAndText() throws Throwable {
    String receiver = "liam.li@ircm.qc.ca";
    String subject = "test subject";
    String textContent = "text message";
    String htmlContent = "<html><body>html message</body></html>";
    MimeMessageHelper email = mailService.htmlEmail();
    email.addTo(receiver);
    email.setSubject(subject);
    email.setText(textContent, htmlContent);
    mailService.send(email);
    email = mailService.htmlEmail();
    email.addTo(receiver);
    email.setSubject(subject);
    email.setText(textContent, htmlContent);

    mailService.send(email);

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(2, messages.length);
    for (MimeMessage message : messages) {
      assertEquals(subject, message.getSubject());
      assertNotNull(message.getFrom());
      assertEquals(1, message.getFrom().length);
      assertEquals(new InternetAddress(mailConfiguration.from()), message.getFrom()[0]);
      assertNotNull(message.getRecipients(RecipientType.TO));
      assertEquals(1, message.getRecipients(RecipientType.TO).length);
      assertEquals(new InternetAddress(receiver), message.getRecipients(RecipientType.TO)[0]);
      assertTrue(message.getRecipients(RecipientType.CC) == null
          || message.getRecipients(RecipientType.CC).length == 0);
      assertTrue(message.getRecipients(RecipientType.BCC) == null
          || message.getRecipients(RecipientType.BCC).length == 0);
      assertEquals(htmlContent, htmlContent(message).orElse(""));
      assertEquals(textContent, plainContent(message).orElse(""));
    }
  }

  @Test
  @Timeout(150)
  public void sendEmail_ErrorText() throws Throwable {
    String receiver = "liam.li@ircm.qc.ca";
    String subject = "test subject";
    String content = "text message";
    MimeMessageHelper email = new MimeMessageHelper(mailSender.createMimeMessage());
    email.setFrom("lanaseq@ircm.qc.ca");
    email.addTo(receiver);
    email.setSubject(subject);
    email.setText(content);
    greenMail.stop();

    mailService.send(email);
  }

  @Test
  @Timeout(150)
  public void sendEmail_ErrorHtmlAndText() throws Throwable {
    String receiver = "liam.li@ircm.qc.ca";
    String subject = "test subject";
    String textContent = "text message";
    String htmlContent = "<html><body>html message</body></html>";
    MimeMessageHelper email = new MimeMessageHelper(mailSender.createMimeMessage(), true);
    email.setFrom("lanaseq@ircm.qc.ca");
    email.addTo(receiver);
    email.setSubject(subject);
    email.setText(textContent, htmlContent);
    greenMail.stop();

    mailService.send(email);
  }

  @Test
  @Timeout(150)
  public void sendError() throws Throwable {
    Exception error = new IllegalStateException("test");
    StringWriter writer = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(writer)) {
      error.printStackTrace(printWriter);
    }
    User user = new User(1L, "christian.poitras@ircm.qc.ca");
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));

    mailService.sendError(error);

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    MimeMessage message = messages[0];
    assertEquals(mailConfiguration.subject(), message.getSubject());
    assertNotNull(message.getFrom());
    assertEquals(1, message.getFrom().length);
    assertEquals(new InternetAddress(mailConfiguration.from()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(mailConfiguration.to()),
        message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    String body = GreenMailUtil.getBody(message).replaceAll("\r?\n", "");
    String expectedBody =
        "User:" + user.getEmail() + (error.getMessage() + "\n" + writer).replaceAll("\r?\n", "");
    assertEquals(expectedBody, body);
  }

  @Test
  @Timeout(150)
  public void sendError_NoCurrentUser() throws Throwable {
    Exception error = new IllegalStateException("test");
    StringWriter writer = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(writer)) {
      error.printStackTrace(printWriter);
    }

    mailService.sendError(error);

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    MimeMessage message = messages[0];
    assertEquals(mailConfiguration.subject(), message.getSubject());
    assertNotNull(message.getFrom());
    assertEquals(1, message.getFrom().length);
    assertEquals(new InternetAddress(mailConfiguration.from()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(mailConfiguration.to()),
        message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    String body = GreenMailUtil.getBody(message).replaceAll("\r?\n", "");
    String expectedBody =
        "User:null" + (error.getMessage() + "\n" + writer).replaceAll("\r?\n", "");
    assertEquals(expectedBody, body);
  }
}
