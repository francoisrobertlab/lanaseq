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

import static ca.qc.ircm.lanaseq.web.MainView.VIEW_NAME;
import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class MainViewItTest extends AbstractTestBenchTestCase {
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
  public void userRedirected() throws Throwable {
    open();

    assertEquals(viewUrl(DatasetsView.VIEW_NAME), getDriver().getCurrentUrl());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void managerRedirected() throws Throwable {
    open();

    assertEquals(viewUrl(DatasetsView.VIEW_NAME), getDriver().getCurrentUrl());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void adminRedirected() throws Throwable {
    open();

    assertEquals(viewUrl(UsersView.VIEW_NAME), getDriver().getCurrentUrl());
  }
}
