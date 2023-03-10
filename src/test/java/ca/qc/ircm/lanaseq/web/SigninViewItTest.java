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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.web.SigninView.DISABLED;
import static ca.qc.ircm.lanaseq.web.SigninView.FAIL;
import static ca.qc.ircm.lanaseq.web.SigninView.LOCKED;
import static ca.qc.ircm.lanaseq.web.SigninView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SigninView}.
 */
@TestBenchTestAnnotations
public class SigninViewItTest extends AbstractTestBenchTestCase {
  @Autowired
  private transient SecurityConfiguration configuration;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void title() throws Throwable {
    open();

    assertEquals(resources(SigninView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.getUsernameField()).isPresent());
    assertTrue(optional(() -> view.getPasswordField()).isPresent());
    assertTrue(optional(() -> view.getSubmitButton()).isPresent());
    assertTrue(optional(() -> view.getForgotPasswordButton()).isPresent());
  }

  @Test
  public void sign_Fail() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
    view.getPasswordField().setValue("notright");
    view.getSubmitButton().click();
    assertEquals(new AppResources(SigninView.class, currentLocale()).message(FAIL),
        view.getErrorMessage());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @Test
  public void sign_Disabled() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("ava.martin@ircm.qc.ca");
    view.getPasswordField().setValue("password");
    view.getSubmitButton().click();
    assertEquals(new AppResources(SigninView.class, currentLocale()).message(DISABLED),
        view.getErrorMessage());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @Test
  public void sign_Locked() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    for (int i = 0; i < 6; i++) {
      view.getUsernameField().setValue("olivia.brown@ircm.qc.ca");
      view.getPasswordField().setValue("notright");
      view.getSubmitButton().click();
    }
    assertEquals(new AppResources(SigninView.class, currentLocale()).message(LOCKED,
        configuration.getLockDuration().getSeconds() / 60), view.getErrorMessage());
    assertTrue(getDriver().getCurrentUrl().startsWith(viewUrl(VIEW_NAME) + "?"));
  }

  @Test
  public void sign() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getUsernameField().setValue("jonh.smith@ircm.qc.ca");
    view.getPasswordField().setValue("pass1");
    view.getSubmitButton().click();
    assertEquals(viewUrl(DatasetsView.VIEW_NAME), getDriver().getCurrentUrl());
  }

  @Test
  public void forgotPassword() throws Throwable {
    open();
    SigninViewElement view = $(SigninViewElement.class).waitForFirst();
    view.getForgotPasswordButton().click();
    assertEquals(viewUrl(ForgotPasswordView.VIEW_NAME), getDriver().getCurrentUrl());
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void already_User() throws Throwable {
    open();
    assertEquals(viewUrl(DatasetsView.VIEW_NAME), getDriver().getCurrentUrl());
  }
}
