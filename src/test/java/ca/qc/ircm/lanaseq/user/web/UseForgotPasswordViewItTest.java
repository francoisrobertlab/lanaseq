package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SEPARATOR;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordRepository;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Integration tests for {@link UseForgotPasswordView}.
 */
@TestBenchTestAnnotations
public class UseForgotPasswordViewItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(UseForgotPasswordView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(UseForgotPasswordViewItTest.class);
  @Autowired
  private ForgotPasswordRepository repository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private EntityManager entityManager;
  @Autowired
  private MessageSource messageSource;
  private String password = "test_password";
  private long id = 9;
  private String confirm = "174407008";

  private void open() {
    openView(VIEW_NAME, id + SEPARATOR + confirm);
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
    UseForgotPasswordViewElement view = $(UseForgotPasswordViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.message()).isPresent());
    assertTrue(optional(() -> view.passwordsForm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  public void save() throws Throwable {
    open();
    UseForgotPasswordViewElement view = $(UseForgotPasswordViewElement.class).waitForFirst();

    view.passwordsForm().password().setValue(password);
    view.passwordsForm().passwordConfirm().setValue(password);
    view.save().click();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, null, currentLocale()),
        notification.getText());
    ForgotPassword forgotPassword = repository.findById(id).orElseThrow();
    entityManager.refresh(forgotPassword);
    assertTrue(forgotPassword.isUsed());
    User user = userRepository.findById(9L).orElseThrow();
    entityManager.refresh(user);
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
    $(SigninViewElement.class).waitForFirst();
  }
}
