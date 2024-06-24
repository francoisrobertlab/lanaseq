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
import static ca.qc.ircm.lanaseq.SpringConfiguration.messagePrefix;
import static ca.qc.ircm.lanaseq.user.web.PasswordView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsViewElement;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import ca.qc.ircm.lanaseq.web.MainView;
import ca.qc.ircm.lanaseq.web.SigninView;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link PasswordView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("christian.poitras@ircm.qc.ca")
public class PasswordViewItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(PasswordView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MessageSource messageSource;
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
  public void fieldsExistence() throws Throwable {
    open();
    PasswordViewElement view = $(PasswordViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.passwords().password()).isPresent());
    assertTrue(optional(() -> view.passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  @WithAnonymousUser
  public void sign_ForceChangePassword() throws Throwable {
    openView(SigninView.VIEW_NAME);
    SigninViewElement signinView = $(SigninViewElement.class).waitForFirst();
    signinView.getUsernameField().setValue("christian.poitras@ircm.qc.ca");
    signinView.getPasswordField().setValue("pass1");
    signinView.getSubmitButton().click();
    PasswordViewElement view = $(PasswordViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.passwords().password()).isPresent());
    assertTrue(optional(() -> view.passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  public void mainView() throws Throwable {
    openView(MainView.VIEW_NAME);
    PasswordViewElement view = $(PasswordViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.passwords().password()).isPresent());
    assertTrue(optional(() -> view.passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  public void datasetsView() throws Throwable {
    openView(DatasetsView.VIEW_NAME);
    PasswordViewElement view = $(PasswordViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.passwords().password()).isPresent());
    assertTrue(optional(() -> view.passwords().passwordConfirm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  public void save() throws Throwable {
    open();
    PasswordViewElement view = $(PasswordViewElement.class).waitForFirst();
    view.passwords().password().setValue(password);
    view.passwords().passwordConfirm().setValue(password);

    TestTransaction.flagForCommit();
    view.save().click();
    TestTransaction.end();

    $(DatasetsViewElement.class).waitForFirst();
    User user = repository.findById(6L).orElse(null);
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
  }
}
