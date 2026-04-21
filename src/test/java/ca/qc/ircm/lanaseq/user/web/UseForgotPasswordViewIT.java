package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordRepository;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Integration tests for {@link UseForgotPasswordView}.
 */
@ServiceTestAnnotations
public class UseForgotPasswordViewIT extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(UseForgotPasswordView.class);
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(UseForgotPasswordViewIT.class);
  @Autowired
  private ForgotPasswordRepository repository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  private final String password = "test_password";
  private final long id = 9;
  private final String confirm = "174407008";

  @Test
  public void save() {
    UseForgotPasswordView view = navigate(UseForgotPasswordView.class, id + SEPARATOR + confirm);

    test(view.form.password).setValue(password);
    test(view.form.passwordConfirm).setValue(password);
    test(view.save).click();

    Notification notification = $(Notification.class).first();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, null, UI.getCurrent().getLocale()),
        test(notification).getText());
    ForgotPassword forgotPassword = repository.findById(id).orElseThrow();
    assertTrue(forgotPassword.isUsed());
    User user = userRepository.findById(9L).orElseThrow();
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
    assertTrue($(SigninView.class).exists());
  }
}
