package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.user.UserService;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Integration tests for {@link UserDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UserDialogIT extends SpringBrowserlessTest {

  private static final String MESSAGE_PREFIX = messagePrefix(UserDialog.class);
  @MockitoSpyBean
  private UserService service;
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private EntityManager entityManager;
  private final String email = "it_test@ircm.qc.ca";
  private final String name = "test_name";
  private final String password = "test_password";

  private void setFields(UserDialog dialog) {
    dialog.form.email.setValue(email);
    dialog.form.name.setValue(name);
    dialog.form.passwords.password.setValue(password);
    dialog.form.passwords.passwordConfirm.setValue(password);
  }

  private void detachOnServiceGet() {
    when(service.get(anyLong())).then(a -> {
      @SuppressWarnings("unchecked") Optional<User> optionalUser = (Optional<User>) a.callRealMethod();
      optionalUser.ifPresent(d -> entityManager.detach(d));
      return optionalUser;
    });
  }

  @Test
  public void save() {
    detachOnServiceGet();
    UsersView view = navigate(UsersView.class);
    test(view.users).select(2);
    test(view.edit).click();
    UserDialog dialog = $(UserDialog.class).first();
    setFields(dialog);

    test(dialog.save).click();

    assertFalse($(UserDialog.class).exists());
    Notification notification = $(Notification.class).first();
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{email},
        UI.getCurrent().getLocale()), test(notification).getText());
    User user = repository.findById(3L).orElseThrow();
    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
  }

  @Test
  public void save_Fail() {
    detachOnServiceGet();
    UsersView view = navigate(UsersView.class);
    test(view.users).select(2);
    test(view.edit).click();
    UserDialog dialog = $(UserDialog.class).first();
    setFields(dialog);
    dialog.form.email.setValue("test");

    test(dialog.save).click();

    assertTrue($(UserDialog.class).exists());
    assertFalse($(Notification.class).exists());
    User user = repository.findById(3L).orElseThrow();
    Assertions.assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    Assertions.assertEquals("Jonh Smith", user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches("pass1", user.getHashedPassword()));
  }

  @Test
  public void cancel() {
    detachOnServiceGet();
    UsersView view = navigate(UsersView.class);
    test(view.users).select(2);
    test(view.edit).click();
    UserDialog dialog = $(UserDialog.class).first();
    setFields(dialog);

    test(dialog.cancel).click();

    assertFalse($(UserDialog.class).exists());
    assertFalse($(Notification.class).exists());
    User user = repository.findById(3L).orElseThrow();
    Assertions.assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    Assertions.assertEquals("Jonh Smith", user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches("pass1", user.getHashedPassword()));
  }
}
