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

package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.UserDialog.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UsersView.ID;
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

@TestBenchTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UserDialogItTest extends AbstractTestBenchTestCase {
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
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
    UsersViewElement view = $(UsersViewElement.class).id(ID);

    view.users().doubleClick(1);

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
    UsersViewElement view = $(UsersViewElement.class).id(ID);

    view.users().doubleClick(2);

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
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    view.users().doubleClick(2);
    UserDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(UserDialog.class);
    assertEquals(resources.message(SAVED, email), notification.getText());
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
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    view.users().doubleClick(2);
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
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    view.users().doubleClick(2);
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
