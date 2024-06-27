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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.ProfileView.SAVED;
import static ca.qc.ircm.lanaseq.user.web.ProfileView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
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
public class ProfileViewItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(ProfileView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
  private String email = "test@ircm.qc.ca";
  private String name = "Test User";
  private String password = "test_password";

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();

    $(SigninViewElement.class).waitForFirst();
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
  public void fieldsExistence_User() throws Throwable {
    open();
    ProfileViewElement view = $(ProfileViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.form()).isPresent());
    assertTrue(optional(() -> view.form().email()).isPresent());
    assertTrue(optional(() -> view.form().name()).isPresent());
    assertFalse(optional(() -> view.form().admin()).isPresent());
    assertFalse(optional(() -> view.form().manager()).isPresent());
    assertTrue(optional(() -> view.form().passwords()).isPresent());
    assertTrue(optional(() -> view.form().passwords().password()).isPresent());
    assertTrue(optional(() -> view.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void fieldsExistence_Manager() throws Throwable {
    open();
    ProfileViewElement view = $(ProfileViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.form()).isPresent());
    assertTrue(optional(() -> view.form().email()).isPresent());
    assertTrue(optional(() -> view.form().name()).isPresent());
    assertFalse(optional(() -> view.form().admin()).isPresent());
    assertTrue(optional(() -> view.form().manager()).isPresent());
    assertTrue(optional(() -> view.form().passwords()).isPresent());
    assertTrue(optional(() -> view.form().passwords().password()).isPresent());
    assertTrue(optional(() -> view.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void fieldsExistence_Admin() throws Throwable {
    open();
    ProfileViewElement view = $(ProfileViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.form()).isPresent());
    assertTrue(optional(() -> view.form().email()).isPresent());
    assertTrue(optional(() -> view.form().name()).isPresent());
    assertTrue(optional(() -> view.form().admin()).isPresent());
    assertTrue(optional(() -> view.form().manager()).isPresent());
    assertTrue(optional(() -> view.form().passwords()).isPresent());
    assertTrue(optional(() -> view.form().passwords().password()).isPresent());
    assertTrue(optional(() -> view.form().passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  public void save() throws Throwable {
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
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, null, currentLocale()),
        notification.getText());
    User user = repository.findById(3L).orElse(null);
    assertNotNull(user);
    assertEquals(email, user.getEmail());
    assertEquals(name, user.getName());
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
    assertEquals(LocalDateTime.of(2018, 12, 7, 15, 40, 12), user.getLastSignAttempt());
    assertEquals(null, user.getLocale());
    $(ProfileViewElement.class).waitForFirst();
  }
}
