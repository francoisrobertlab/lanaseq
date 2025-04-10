package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.ProfileView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.ProfileView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchBrowser;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link ProfileView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProfileViewItTest extends AbstractTestBenchBrowser {

  private static final String MESSAGE_PREFIX = messagePrefix(ProfileView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  private final String email = "test@ircm.qc.ca";
  private final String name = "Test User";
  private final String password = "test_password";

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  @WithAnonymousUser
  public void security_Anonymous() {
    open();

    $(SigninViewElement.class).waitForFirst();
  }

  @BrowserTest
  public void title() {
    open();

    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null,
        currentLocale());
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[]{applicationName},
            currentLocale()), getDriver().getTitle());
  }

  @BrowserTest
  public void fieldsExistence_User() {
    open();
    ProfileViewElement view = $(ProfileViewElement.class).waitForFirst();
    assertTrue(optional(view::form).isPresent());
    assertTrue(optional(() -> view.form().email()).isPresent());
    assertTrue(optional(() -> view.form().name()).isPresent());
    assertFalse(optional(() -> view.form().admin()).isPresent());
    assertFalse(optional(() -> view.form().manager()).isPresent());
    assertTrue(optional(() -> view.form().passwords()).isPresent());
    assertTrue(optional(() -> view.form().passwords().password()).isPresent());
    assertTrue(optional(() -> view.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(view::save).isPresent());
  }

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void fieldsExistence_Manager() {
    open();
    ProfileViewElement view = $(ProfileViewElement.class).waitForFirst();
    assertTrue(optional(view::form).isPresent());
    assertTrue(optional(() -> view.form().email()).isPresent());
    assertTrue(optional(() -> view.form().name()).isPresent());
    assertFalse(optional(() -> view.form().admin()).isPresent());
    assertTrue(optional(() -> view.form().manager()).isPresent());
    assertTrue(optional(() -> view.form().passwords()).isPresent());
    assertTrue(optional(() -> view.form().passwords().password()).isPresent());
    assertTrue(optional(() -> view.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(view::save).isPresent());
  }

  @BrowserTest
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void fieldsExistence_Admin() {
    open();
    ProfileViewElement view = $(ProfileViewElement.class).waitForFirst();
    assertTrue(optional(view::form).isPresent());
    assertTrue(optional(() -> view.form().email()).isPresent());
    assertTrue(optional(() -> view.form().name()).isPresent());
    assertTrue(optional(() -> view.form().admin()).isPresent());
    assertTrue(optional(() -> view.form().manager()).isPresent());
    assertTrue(optional(() -> view.form().passwords()).isPresent());
    assertTrue(optional(() -> view.form().passwords().password()).isPresent());
    assertTrue(optional(() -> view.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(view::save).isPresent());
  }

  @BrowserTest
  public void save() {
    open();
    ProfileViewElement view = $(ProfileViewElement.class).waitForFirst();
    view.form().email().setValue(email);
    view.form().name().setValue(name);
    view.form().passwords().password().setValue(password);
    view.form().passwords().passwordConfirm().setValue(password);

    TestTransaction.flagForCommit();
    view.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, null, currentLocale()),
        notification.getText());
    User user = repository.findById(3L).orElseThrow();
    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(name, user.getName());
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
    Assertions.assertEquals(LocalDateTime.of(2018, 12, 7, 15, 40, 12), user.getLastSignAttempt());
    assertNull(user.getLocale());
    $(ProfileViewElement.class).waitForFirst();
  }
}
