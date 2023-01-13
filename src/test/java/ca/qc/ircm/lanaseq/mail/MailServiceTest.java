/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.mail;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.SmtpPortRandomizer;
import ca.qc.ircm.lanaseq.user.User;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests for {@link MailService}.
 */
@ContextConfiguration(initializers = SmtpPortRandomizer.class)
@NonTransactionalTestAnnotations
public class MailServiceTest {
  @Autowired
  private MailService mailService;
  @Autowired
  private MailConfiguration mailConfiguration;
  @Autowired
  private MimeMessage templateMessage;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @Value("${spring.mail.port}")
  private int smtpPort;
  @RegisterExtension
  GreenMailExtension greenMail;

  @PostConstruct
  public void initGreenMail() {
    greenMail = new GreenMailExtension(new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP));
  }

  @Test
  @Timeout(150)
  public void textEmail() throws Throwable {
    MimeMessageHelper email = mailService.textEmail();

    assertNotSame(templateMessage, email.getMimeMessage());
    assertEquals(templateMessage.getSubject(), email.getMimeMessage().getSubject());
    assertArrayEquals(templateMessage.getFrom(), email.getMimeMessage().getFrom());
    assertEquals(templateMessage.getContent(), email.getMimeMessage().getContent());
  }

  @Test
  @Timeout(150)
  public void htmlEmail() throws Throwable {
    MimeMessageHelper email = mailService.htmlEmail();

    assertNotSame(templateMessage, email.getMimeMessage());
    assertEquals(templateMessage.getSubject(), email.getMimeMessage().getSubject());
    assertArrayEquals(templateMessage.getFrom(), email.getMimeMessage().getFrom());
    assertTrue(email.getMimeMessage().getContent() instanceof Multipart);
    final MimeMessageParser mimeMessageParser =
        new MimeMessageParser(email.getMimeMessage()).parse();
    assertNull(mimeMessageParser.getHtmlContent());
    assertNull(mimeMessageParser.getPlainContent());
  }

  @Test
  @Timeout(150)
  public void sendEmail_Text() throws Throwable {
    String receiver = "liam.li@ircm.qc.ca";
    String subject = "test subject";
    String content = "text message";
    MimeMessageHelper email = new MimeMessageHelper(new MimeMessage(templateMessage));
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
    assertEquals(new InternetAddress(mailConfiguration.getFrom()), message.getFrom()[0]);
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
    MimeMessageHelper email = new MimeMessageHelper(new MimeMessage(templateMessage), true);
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
    assertEquals(new InternetAddress(mailConfiguration.getFrom()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(receiver), message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    final MimeMessageParser mimeMessageParser = new MimeMessageParser(message).parse();
    assertEquals(htmlContent, mimeMessageParser.getHtmlContent());
    assertEquals(textContent, mimeMessageParser.getPlainContent());
  }

  @Test
  @Timeout(150)
  public void sendEmail_ErrorText() throws Throwable {
    String receiver = "liam.li@ircm.qc.ca";
    String subject = "test subject";
    String content = "text message";
    MimeMessageHelper email = new MimeMessageHelper(new MimeMessage(templateMessage));
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
    MimeMessageHelper email = new MimeMessageHelper(new MimeMessage(templateMessage), true);
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
    assertEquals(mailConfiguration.getSubject(), message.getSubject());
    assertNotNull(message.getFrom());
    assertEquals(1, message.getFrom().length);
    assertEquals(new InternetAddress(mailConfiguration.getFrom()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(mailConfiguration.getTo()),
        message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    String body = GreenMailUtil.getBody(message).replaceAll("\r?\n", "");
    String expectedBody = "User:" + user.getEmail()
        + (error.getMessage() + "\n" + writer.toString()).replaceAll("\r?\n", "");
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
    assertEquals(mailConfiguration.getSubject(), message.getSubject());
    assertNotNull(message.getFrom());
    assertEquals(1, message.getFrom().length);
    assertEquals(new InternetAddress(mailConfiguration.getFrom()), message.getFrom()[0]);
    assertNotNull(message.getRecipients(RecipientType.TO));
    assertEquals(1, message.getRecipients(RecipientType.TO).length);
    assertEquals(new InternetAddress(mailConfiguration.getTo()),
        message.getRecipients(RecipientType.TO)[0]);
    assertTrue(message.getRecipients(RecipientType.CC) == null
        || message.getRecipients(RecipientType.CC).length == 0);
    assertTrue(message.getRecipients(RecipientType.BCC) == null
        || message.getRecipients(RecipientType.BCC).length == 0);
    String body = GreenMailUtil.getBody(message).replaceAll("\r?\n", "");
    String expectedBody =
        "User:null" + (error.getMessage() + "\n" + writer.toString()).replaceAll("\r?\n", "");
    assertEquals(expectedBody, body);
  }
}
