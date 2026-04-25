package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.PasswordView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.PasswordView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.MainView;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link PasswordView}.
 */
@ServiceTestAnnotations
@WithUserDetails("christian.poitras@ircm.qc.ca")
public class PasswordViewIT extends SpringBrowserlessTest {

  private static final String MESSAGE_PREFIX = messagePrefix(PasswordView.class);
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  private final String password = "test_password";

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void forceChangePassword_mainView() {
    PasswordView view = navigate(MainView.VIEW_NAME, PasswordView.class);
    assertTrue(test(view.header).isUsable());
    assertTrue(test(view.passwords.password).isUsable());
    assertTrue(test(view.passwords.passwordConfirm).isUsable());
    assertTrue(test(view.save).isUsable());
  }

  @Test
  public void forceChangePassword_datasetsView() {
    PasswordView view = navigate(DatasetsView.VIEW_NAME, PasswordView.class);
    assertTrue(test(view.header).isUsable());
    assertTrue(test(view.passwords.password).isUsable());
    assertTrue(test(view.passwords.passwordConfirm).isUsable());
    assertTrue(test(view.save).isUsable());
  }

  @Test
  public void save() {
    PasswordView view = navigate(PasswordView.class);
    test(view.passwords.password).setValue(password);
    test(view.passwords.passwordConfirm).setValue(password);

    test(view.save).click();

    Notification notification = $(Notification.class).first();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, null, UI.getCurrent().getLocale()),
        test(notification).getText());
    assertTrue($(DatasetsView.class).exists());
    User user = repository.findById(6L).orElseThrow();
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
  }
}
