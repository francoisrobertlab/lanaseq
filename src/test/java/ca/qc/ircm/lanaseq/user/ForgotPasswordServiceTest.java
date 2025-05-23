package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.mail.MailService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.util.StringUtils;

/**
 * Tests for {@link ForgotPasswordService}.
 */
@ServiceTestAnnotations
public class ForgotPasswordServiceTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ForgotPasswordService.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(ForgotPasswordServiceTest.class);
  @Autowired
  private ForgotPasswordService service;
  @Autowired
  private ForgotPasswordRepository repository;
  @Autowired
  private UserRepository userRepository;
  @MockitoBean
  private AppConfiguration appConfiguration;
  @MockitoBean
  private MailService mailService;
  @MockitoBean
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  @Mock
  private ForgotPasswordWebContext forgotPasswordWebContext;
  @Mock
  private MimeMessageHelper email;
  @Captor
  private ArgumentCaptor<ForgotPassword> forgotPasswordCaptor;
  @Captor
  private ArgumentCaptor<String> stringCaptor;
  private String hashedPassword;
  private User user;
  private String confirmNumber;
  private final String forgotPasswordUrl = "/validate/user";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Throwable {
    user = userRepository.findById(9L).orElseThrow();
    when(appConfiguration.getUrl(any(String.class))).thenReturn(forgotPasswordUrl);
    hashedPassword = "da78f3a74658706/4ae8470fc73a83f369fed012";
    when(passwordEncoder.encode(any(String.class))).thenReturn(hashedPassword);
    when(mailService.htmlEmail()).thenReturn(email);
    when(forgotPasswordWebContext.getChangeForgottenPasswordUrl(any(), any())).thenReturn(
        forgotPasswordUrl);
    confirmNumber = "70987756";
  }

  @Test
  public void get() {
    ForgotPassword forgotPassword = service.get(9L, "174407008").orElseThrow();

    assertEquals((Long) 9L, forgotPassword.getId());
    assertEquals("174407008", forgotPassword.getConfirmNumber());
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(forgotPassword.getRequestMoment()));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(forgotPassword.getRequestMoment()));
  }

  @Test
  public void get_Expired() {
    assertFalse(service.get(7L, "803369922").isPresent());
  }

  @Test
  public void get_Invalid() {
    assertFalse(service.get(20L, "435FA").isPresent());
  }

  @Test
  public void get_Id0() {
    assertFalse(service.get(0, confirmNumber).isPresent());
  }

  @Test
  public void get_Used() {
    assertFalse(service.get(10L, "460559412").isPresent());
  }

  @Test
  public void insert_Robot() {
    user = userRepository.findById(1L).orElseThrow();

    assertThrows(AccessDeniedException.class,
        () -> service.insert(user.getEmail(), forgotPasswordWebContext));
  }

  @Test
  public void insert() throws Exception {
    service.insert(user.getEmail(), forgotPasswordWebContext);

    repository.flush();
    verify(forgotPasswordWebContext).getChangeForgottenPasswordUrl(forgotPasswordCaptor.capture(),
        any());
    ForgotPassword forgotPassword = forgotPasswordCaptor.getValue();
    assertNotEquals(0, forgotPassword.getId());
    verify(mailService).htmlEmail();
    verify(mailService).send(email);
    forgotPassword = repository.findById(forgotPassword.getId()).orElseThrow();
    assertNotNull(forgotPassword.getConfirmNumber());
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(forgotPassword.getRequestMoment()));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(forgotPassword.getRequestMoment()));
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

    service.insert(user.getEmail(), forgotPasswordWebContext);

    repository.flush();
    verify(mailService).htmlEmail();
    verify(mailService).send(email);
    verify(email).addTo(user.getEmail());
    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + "application.name", null,
        locale);
    ResourceBundle mailResources = ResourceBundle.getBundle("user.forgotpassword", locale);
    verify(email).setSubject(
        messageSource.getMessage(MESSAGE_PREFIX + "subject", new Object[]{applicationName},
            locale));
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
  public void insert_NotExists() {
    service.insert("test@ircm.qc.ca", forgotPasswordWebContext);

    assertEquals(4, repository.findAll().size());
    verifyNoInteractions(forgotPasswordWebContext);
    verifyNoInteractions(mailService);
  }

  @Test
  public void updatePassword() {
    ForgotPassword forgotPassword = repository.findById(9L).orElseThrow();

    service.updatePassword(forgotPassword, "abc");

    repository.flush();
    assertFalse(service.get(forgotPassword.getId(), forgotPassword.getConfirmNumber()).isPresent());
    verify(passwordEncoder).encode("abc");
    User user = userRepository.findById(9L).orElseThrow();
    assertEquals(hashedPassword, user.getHashedPassword());
  }

  @Test
  public void updatePassword_Expired() {
    ForgotPassword forgotPassword = repository.findById(7L).orElseThrow();

    try {
      service.updatePassword(forgotPassword, "abc");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Ignore.
    }
  }
}
