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
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.Headless;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("lana@ircm.qc.ca")
public class UsersViewItTest extends AbstractTestBenchTestCase {
  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @Headless(false)
  public void title() throws Throwable {
    open();
    Thread.sleep(5000);

    assertEquals(resources(UsersView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }
}
