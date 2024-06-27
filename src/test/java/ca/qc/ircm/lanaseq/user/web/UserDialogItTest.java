package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import org.junit.jupiter.api.Test;
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
public class UserDialogItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(UserDialog.class);
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  private String email = "it_test@ircm.qc.ca";
  private String name = "test_name";
  private String password = "test_password";

  private void open() {
    openView(VIEW_NAME);
  }

  private void setFields(UserDialogElement dialog) {
    dialog.form().email().setValue(email);
    dialog.form().name().setValue(name);
    dialog.form().passwords().password().setValue(password);
    dialog.form().passwords().passwordConfirm().setValue(password);
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void fieldsExistence_Manager() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();

    view.users().edit(1).click();

    UserDialogElement dialog = view.dialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.form()).isPresent());
    assertTrue(optional(() -> dialog.form().email()).isPresent());
    assertTrue(optional(() -> dialog.form().name()).isPresent());
    assertFalse(optional(() -> dialog.form().admin()).isPresent());
    assertTrue(optional(() -> dialog.form().manager()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().password()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
  }

  @Test
  public void fieldsExistence_Admin() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();

    view.users().edit(2).click();

    UserDialogElement dialog = view.dialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.form()).isPresent());
    assertTrue(optional(() -> dialog.form().email()).isPresent());
    assertTrue(optional(() -> dialog.form().name()).isPresent());
    assertTrue(optional(() -> dialog.form().admin()).isPresent());
    assertTrue(optional(() -> dialog.form().manager()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().password()).isPresent());
    assertTrue(optional(() -> dialog.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
  }

  @Test
  public void save() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().edit(2).click();
    UserDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[] { email }, currentLocale()),
        notification.getText());
    User user = repository.findById(3L).get();
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
  }

  @Test
  public void save_Fail() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().edit(2).click();
    UserDialogElement dialog = view.dialog();
    setFields(dialog);
    dialog.form().email().setValue("test");

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    assertTrue(optional(() -> view.dialog()).isPresent());
    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    User user = repository.findById(3L).get();
    assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    assertEquals("Jonh Smith", user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches("pass1", user.getHashedPassword()));
  }

  @Test
  public void cancel() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).waitForFirst();
    view.users().edit(2).click();
    UserDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    User user = repository.findById(3L).get();
    assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    assertEquals("Jonh Smith", user.getName());
    assertFalse(user.isAdmin());
    assertFalse(user.isManager());
    assertTrue(passwordEncoder.matches("pass1", user.getHashedPassword()));
  }
}
