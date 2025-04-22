package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link UserDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UserDialogIT extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(UserDialog.class);
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  private final String email = "it_test@ircm.qc.ca";
  private final String name = "test_name";
  private final String password = "test_password";

  private void open() {
    openView(VIEW_NAME);
  }

  private void setFields(UserDialogElement dialog) {
    dialog.form().email().setValue(email);
    dialog.form().name().setValue(name);
    dialog.form().passwords().password().setValue(password);
    dialog.form().passwords().passwordConfirm().setValue(password);
  }

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void fieldsExistence_Manager() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();

    view.users().select(1);
    view.edit().click();

    UserDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::form).isPresent());
    assertTrue(optional(() -> dialog.form().email()).isPresent());
    assertTrue(optional(() -> dialog.form().name()).isPresent());
    assertFalse(optional(() -> dialog.form().admin()).isPresent());
    assertTrue(optional(() -> dialog.form().manager()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().password()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
  }

  @BrowserTest
  public void fieldsExistence_Admin() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();

    view.users().select(2);
    view.edit().click();

    UserDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::form).isPresent());
    assertTrue(optional(() -> dialog.form().email()).isPresent());
    assertTrue(optional(() -> dialog.form().name()).isPresent());
    assertTrue(optional(() -> dialog.form().admin()).isPresent());
    assertTrue(optional(() -> dialog.form().manager()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().password()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
  }

  @BrowserTest
  public void save() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().select(2);
    view.edit().click();
    UserDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{email}, currentLocale()),
        notification.getText());
    User user = repository.findById(3L).orElseThrow();
    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
  }

  @BrowserTest
  public void save_Fail() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().select(2);
    view.edit().click();
    UserDialogElement dialog = view.dialog();
    setFields(dialog);
    dialog.form().email().setValue("test");

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    assertTrue(optional(view::dialog).isPresent());
    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    User user = repository.findById(3L).orElseThrow();
    Assertions.assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    Assertions.assertEquals("Jonh Smith", user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches("pass1", user.getHashedPassword()));
  }

  @BrowserTest
  public void cancel() {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().select(2);
    view.edit().click();
    UserDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    User user = repository.findById(3L).orElseThrow();
    Assertions.assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    Assertions.assertEquals("Jonh Smith", user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches("pass1", user.getHashedPassword()));
  }
}
