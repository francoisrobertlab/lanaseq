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
import static ca.qc.ircm.lanaseq.user.web.UsersView.ID;
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.security.web.AccessDeniedView;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import java.util.Locale;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link UsersView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class UsersViewItTest extends AbstractTestBenchTestCase {
  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();

    Locale locale = currentLocale();
    assertEquals(
        new AppResources(SigninView.class, locale).message(TITLE,
            new AppResources(Constants.class, locale).message(APPLICATION_NAME)),
        getDriver().getTitle());
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void security_User() throws Throwable {
    open();

    Locale locale = currentLocale();
    assertEquals(
        new AppResources(AccessDeniedView.class, locale).message(TITLE,
            new AppResources(Constants.class, locale).message(APPLICATION_NAME)),
        getDriver().getTitle());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void security_Manager() throws Throwable {
    open();

    assertEquals(resources(UsersView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void security_Admin() throws Throwable {
    open();

    assertEquals(resources(UsersView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void title() throws Throwable {
    open();

    assertEquals(resources(UsersView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.users()).isPresent());
    assertFalse(optional(() -> view.switchFailed()).isPresent());
    assertTrue(optional(() -> view.add()).isPresent());
    assertTrue(optional(() -> view.switchUser()).isPresent());
  }

  @Test
  public void edit() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);

    view.users().edit(0).click();

    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void add() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);

    view.add().click();

    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void switchUser() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    view.users().select(1);

    view.switchUser().click();

    Locale locale = currentLocale();
    assertEquals(
        new AppResources(DatasetsView.class, locale).message(TITLE,
            new AppResources(Constants.class, locale).message(APPLICATION_NAME)),
        getDriver().getTitle());
  }

  @Test
  @Disabled("Admins are allowed to switch to another admin right now")
  public void switchUser_Fail() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    view.users().select(0);

    view.switchUser().click();

    assertTrue(optional(() -> view.switchFailed()).isPresent());
  }
}
