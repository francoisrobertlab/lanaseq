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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.experiment.web.ExperimentsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.web.ViewLayout.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.experiment.web.ExperimentsView;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import ca.qc.ircm.lanaseq.user.web.UsersViewElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
public class ViewLayoutItTest extends AbstractTestBenchTestCase {
  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();

    assertEquals(viewUrl(SigninView.VIEW_NAME), getDriver().getCurrentUrl());
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void fieldsExistence_User() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    assertTrue(optional(() -> view.experiments()).isPresent());
    assertFalse(optional(() -> view.users()).isPresent());
    assertFalse(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void fieldsExistence_Manager() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    assertTrue(optional(() -> view.experiments()).isPresent());
    assertTrue(optional(() -> view.users()).isPresent());
    assertFalse(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void fieldsExistence_Admin() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    assertTrue(optional(() -> view.experiments()).isPresent());
    assertTrue(optional(() -> view.users()).isPresent());
    assertFalse(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  public void fieldsExistence_Runas() throws Throwable {
    openView(UsersView.VIEW_NAME);
    SigninViewElement signinView = $(SigninViewElement.class).id(SigninView.ID);
    signinView.getUsernameField().setValue("lana@ircm.qc.ca");
    signinView.getPasswordField().setValue("pass2");
    signinView.getSubmitButton().click();
    UsersViewElement usersView = $(UsersViewElement.class).id(UsersView.ID);
    usersView.clickUser(1);
    usersView.clickSwitchUser();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    assertTrue(optional(() -> view.experiments()).isPresent());
    assertTrue(optional(() -> view.users()).isPresent());
    assertTrue(optional(() -> view.exitSwitchUser()).isPresent());
    assertTrue(optional(() -> view.signout()).isPresent());
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void experiments() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    view.experiments().click();
    assertEquals(viewUrl(ExperimentsView.VIEW_NAME), getDriver().getCurrentUrl());
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void users() throws Throwable {
    open();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    view.users().click();
    assertEquals(viewUrl(UsersView.VIEW_NAME), getDriver().getCurrentUrl());
  }

  @Test
  public void exitSwitchUser() throws Throwable {
    openView(UsersView.VIEW_NAME);
    SigninViewElement signinView = $(SigninViewElement.class).id(SigninView.ID);
    signinView.getUsernameField().setValue("lana@ircm.qc.ca");
    signinView.getPasswordField().setValue("pass2");
    signinView.getSubmitButton().click();
    UsersViewElement usersView = $(UsersViewElement.class).id(UsersView.ID);
    usersView.clickUser(1);
    usersView.clickSwitchUser();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    view.exitSwitchUser().click();
    assertEquals(viewUrl(UsersView.VIEW_NAME), getDriver().getCurrentUrl());
    assertFalse(optional(() -> view.exitSwitchUser()).isPresent());
  }

  @Test
  public void signout() throws Throwable {
    open();
    SigninViewElement signinView = $(SigninViewElement.class).id(SigninView.ID);
    signinView.getUsernameField().setValue("jonh.smith@ircm.qc.ca");
    signinView.getPasswordField().setValue("pass1");
    signinView.getSubmitButton().click();
    ViewLayoutElement view = $(ViewLayoutElement.class).id(ID);
    view.signout().click();
    assertEquals(viewUrl(SigninView.VIEW_NAME), getDriver().getCurrentUrl());
  }
}
