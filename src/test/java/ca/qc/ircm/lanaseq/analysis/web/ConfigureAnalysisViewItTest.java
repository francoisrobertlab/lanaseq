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

package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.analysis.web.ConfigureAnalysisView.ID;
import static ca.qc.ircm.lanaseq.analysis.web.ConfigureAnalysisView.SEQTOOLS_LINK;
import static ca.qc.ircm.lanaseq.analysis.web.ConfigureAnalysisView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ConfigureAnalysisViewItTest extends AbstractTestBenchTestCase {
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
  public void title() throws Throwable {
    open();
    assertEquals(resources(ConfigureAnalysisView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    ConfigureAnalysisViewElement view = $(ConfigureAnalysisViewElement.class).id(ID);
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.message()).isPresent());
    assertTrue(optional(() -> view.seqtools()).isPresent());
  }

  @Test
  public void seqtools() throws Throwable {
    open();
    ConfigureAnalysisViewElement view = $(ConfigureAnalysisViewElement.class).id(ID);
    assertEquals(SEQTOOLS_LINK, view.seqtools().getAttribute("href"));
    assertEquals("_blank", view.seqtools().getAttribute("target"));
    view.seqtools().click();
    assertEquals(2, view.getDriver().getWindowHandles().size());
  }
}
