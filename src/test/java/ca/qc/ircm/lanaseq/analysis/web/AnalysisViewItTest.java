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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link AnalysisView}.
 */
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
    assertFalse(optional(() -> view.error()).isPresent());
    assertTrue(optional(() -> view.analyze()).isPresent());
    assertTrue(optional(() -> view.robtools()).isPresent());
  }

  @Test
  public void analyze_One() throws Throwable {
    open();
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    view.datasets().getCell(0, 1).doubleClick();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void analyze_Many() throws Throwable {
    open();
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    view.datasets().select(0);
    view.datasets().select(1);
    view.analyze().click();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void analyze_ManyEmpty() throws Throwable {
    open();
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    view.analyze().click();
    assertTrue(optional(() -> view.error()).isPresent());
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
