/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.lana.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.AppConfiguration;
import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.Constants;
import ca.qc.ircm.lana.mail.MailService;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.util.StringUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ForgotPasswordServiceTest {
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(ForgotPasswordServiceTest.class);
  @Autowired
  private ForgotPasswordService service;
  @Autowired
  private ForgotPasswordRepository repository;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private AppConfiguration appConfiguration;
  @MockBean
  private MailService mailService;
  @MockBean
  private PasswordEncoder passwordEncoder;
  @Mock
  private MimeMessageHelper email;
  @Captor
  private ArgumentCaptor<String> stringCaptor;
  private String hashedPassword;
  private User user;
  private String confirmNumber;
  private String forgotPasswordUrl = "/validate/user";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    user = userRepository.findById(9L).orElse(null);
    when(appConfiguration.getUrl(any(String.class))).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return forgotPasswordUrl;
      }
    });
    hashedPassword = "da78f3a74658706/4ae8470fc73a83f369fed012";
    when(passwordEncoder.encode(any(String.class))).thenReturn(hashedPassword);
    when(mailService.htmlEmail()).thenReturn(email);
    confirmNumber = "70987756";
  }

  private ForgotPasswordWebContext forgotPasswordWebContext() {
    return (forgotPassword, locale) -> forgotPasswordUrl;
  }

  @Test
  public void get() throws Exception {
    ForgotPassword forgotPassword = service.get(9L, "174407008");

    forgotPassword = service.get(forgotPassword.getId(), forgotPassword.getConfirmNumber());

    assertEquals((Long) 9L, forgotPassword.getId());
    assertEquals("174407008", forgotPassword.getConfirmNumber());
    assertTrue(
        LocalDateTime.now().plus(2, ChronoUnit.MINUTES).isAfter(forgotPassword.getRequestMoment()));
    assertTrue(LocalDateTime.now().minus(2, ChronoUnit.MINUTES)
        .isBefore(forgotPassword.getRequestMoment()));
  }

  @Test
  public void get_Expired() throws Exception {
    ForgotPassword forgotPassword = service.get(7L, "803369922");

    assertNull(forgotPassword);
  }

  @Test
  public void get_Invalid() throws Exception {
    ForgotPassword forgotPassword = service.get(20L, "435FA");

    assertNull(forgotPassword);
  }

  @Test
  public void get_NullId() throws Exception {
    ForgotPassword forgotPassword = service.get(null, confirmNumber);

    assertNull(forgotPassword);
  }

  @Test
  public void get_NullConfirmNumber() throws Exception {
    ForgotPassword forgotPassword = service.get(7L, null);

    assertNull(forgotPassword);
  }

  @Test
  public void get_Used() throws Exception {
    ForgotPassword forgotPassword = service.get(10L, "460559412");

    assertNull(forgotPassword);
  }

  @Test
  public void insert_Robot() throws Exception {
    user = userRepository.findById(1L).orElse(null);

    try {
      service.insert(user.getEmail(), forgotPasswordWebContext());
      fail("Expected AccessDeniedException");
    } catch (AccessDeniedException e) {
      // Ignore.
    }
  }

  @Test
  public void insert() throws Exception {
    ForgotPassword forgotPassword = service.insert(user.getEmail(), forgotPasswordWebContext());

    repository.flush();
    assertNotNull(forgotPassword.getId());
    verify(mailService).htmlEmail();
    verify(mailService).send(email);
    forgotPassword = repository.findById(forgotPassword.getId()).orElse(null);
    assertNotNull(forgotPassword.getConfirmNumber());
    assertTrue(
        LocalDateTime.now().plus(2, ChronoUnit.MINUTES).isAfter(forgotPassword.getRequestMoment()));
    assertTrue(LocalDateTime.now().minus(2, ChronoUnit.MINUTES)
        .isBefore(forgotPassword.getRequestMoment()));
  }

  @Test
  public void insert_EmailEn() throws Exception {
    insert_Email(Locale.CANADA);
  }

  @Test
  public void insert_EmailFr() throws Exception {
    insert_Email(Locale.CANADA_FRENCH);
  }

  private void insert_Email(final Locale locale) throws Exception {
    user.setLocale(locale);
    userRepository.save(user);

    service.insert(user.getEmail(), forgotPasswordWebContext());

    repository.flush();
    verify(mailService).htmlEmail();
    verify(mailService).send(email);
    verify(email).addTo(user.getEmail());
    AppResources resources = new AppResources(ForgotPasswordService.class, locale);
    AppResources constants = new AppResources(Constants.class, locale);
    ResourceBundle mailResources = ResourceBundle.getBundle("user.forgotpassword", locale);
    verify(email).setSubject(resources.message("subject", constants.message("application.name")));
    verify(email).setText(stringCaptor.capture(), stringCaptor.capture());
    String textContent = stringCaptor.getAllValues().get(0);
    String htmlContent = stringCaptor.getAllValues().get(1);
    assertTrue(textContent.contains(MessageFormat.format(mailResources.getString("header"), "")));
    assertTrue(htmlContent.contains(
        StringUtils.escapeXml(MessageFormat.format(mailResources.getString("header"), ""))));
    assertTrue(textContent.contains(MessageFormat.format(mailResources.getString("message"), "")));
    assertTrue(htmlContent.contains(
        StringUtils.escapeXml(MessageFormat.format(mailResources.getString("message"), ""))));
    assertTrue(textContent.contains(MessageFormat.format(mailResources.getString("footer"), "")));
    assertTrue(htmlContent.contains(
        StringUtils.escapeXml(MessageFormat.format(mailResources.getString("footer"), ""))));
    String url = appConfiguration.getUrl(forgotPasswordUrl);
    assertTrue(textContent.contains(url));
    assertTrue(htmlContent.contains(url));
    assertFalse(textContent.contains("???"));
    assertFalse(htmlContent.contains("???"));
  }

  @Test
  public void insert_NotExists() throws Exception {
    ForgotPassword forgotPassword = service.insert("test@ircm.qc.ca", forgotPasswordWebContext());

    assertNull(forgotPassword);
    verifyNoInteractions(mailService);
  }

  @Test
  public void updatePassword() throws Exception {
    ForgotPassword forgotPassword = repository.findById(9L).orElse(null);

    service.updatePassword(forgotPassword, "abc");

    repository.flush();
    assertNull(service.get(forgotPassword.getId(), forgotPassword.getConfirmNumber()));
    verify(passwordEncoder).encode("abc");
    User user = userRepository.findById(9L).orElse(null);
    assertEquals(hashedPassword, user.getHashedPassword());
  }

  @Test
  public void updatePassword_Expired() throws Exception {
    ForgotPassword forgotPassword = repository.findById(7L).orElse(null);

    try {
      service.updatePassword(forgotPassword, "abc");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Ignore.
    }
  }
}
