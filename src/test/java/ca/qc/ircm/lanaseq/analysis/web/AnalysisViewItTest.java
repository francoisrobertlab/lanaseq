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
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ID;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ROBTOOLS_LINK;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.VIEW_NAME;
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
public class AnalysisViewItTest extends AbstractTestBenchTestCase {
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
    assertEquals(resources(AnalysisView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.datasets()).isPresent());
    assertTrue(optional(() -> view.dialog()).isPresent());
  }

  @Test
  public void view() throws Throwable {
    open();
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    view.datasets().getCell(0, 0).doubleClick();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void robtools() throws Throwable {
    open();
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    assertEquals(ROBTOOLS_LINK, view.robtools().getAttribute("href"));
    assertEquals("_blank", view.robtools().getAttribute("target"));
    view.robtools().click();
    assertEquals(2, view.getDriver().getWindowHandles().size());
  }
}
