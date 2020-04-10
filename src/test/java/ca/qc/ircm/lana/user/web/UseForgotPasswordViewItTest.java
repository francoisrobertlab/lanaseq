/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lana.Constants.TITLE;
import static ca.qc.ircm.lana.user.web.UseForgotPasswordView.ID;
import static ca.qc.ircm.lana.user.web.UseForgotPasswordView.SAVED;
import static ca.qc.ircm.lana.user.web.UseForgotPasswordView.SEPARATOR;
import static ca.qc.ircm.lana.user.web.UseForgotPasswordView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.Constants;
import ca.qc.ircm.lana.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lana.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lana.user.ForgotPassword;
import ca.qc.ircm.lana.user.ForgotPasswordRepository;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.web.SigninView;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
public class UseForgotPasswordViewItTest extends AbstractTestBenchTestCase {
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
  private String password = "test_password";
  private long id = 9;
  private String confirm = "174407008";

  private void open() {
    openView(VIEW_NAME, id + SEPARATOR + confirm);
  }

  @Test
  public void title() throws Throwable {
    open();

    assertEquals(resources(UseForgotPasswordView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    UseForgotPasswordViewElement view = $(UseForgotPasswordViewElement.class).id(ID);
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.message()).isPresent());
    assertTrue(optional(() -> view.passwordsForm()).isPresent());
    assertTrue(optional(() -> view.save()).isPresent());
  }

  @Test
  public void save() throws Throwable {
    open();
    UseForgotPasswordViewElement view = $(UseForgotPasswordViewElement.class).id(ID);

    view.passwordsForm().password().setValue(password);
    view.passwordsForm().passwordConfirm().setValue(password);
    view.save().click();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(UseForgotPasswordView.class);
    assertEquals(resources.message(SAVED), notification.getText());
    ForgotPassword forgotPassword = repository.findById(id).orElse(null);
    entityManager.refresh(forgotPassword);
    assertTrue(forgotPassword.isUsed());
    User user = userRepository.findById(10L).orElse(null);
    entityManager.refresh(user);
    assertTrue(passwordEncoder.matches(password, user.getHashedPassword()));
    assertEquals(viewUrl(SigninView.VIEW_NAME), getDriver().getCurrentUrl());
  }
}
