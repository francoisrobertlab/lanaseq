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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialogElement;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesViewItTest extends AbstractTestBenchTestCase {
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
  public void title() throws Throwable {
    open();
    assertEquals(resources(SamplesView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.samples()).isPresent());
    assertTrue(optional(() -> view.add()).isPresent());
  }

  @Test
  public void view() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.doubleClickSample(0);
    assertTrue(optional(() -> $(SampleDialogElement.class).id(SampleDialog.ID)).isPresent());
  }

  @Test
  public void view_Protocol() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.doubleClickProtocol(0);
    assertTrue(optional(() -> $(ProtocolDialogElement.class).id(ProtocolDialog.ID)).isPresent());
  }

  @Test
  public void add() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.add().click();
    assertTrue(optional(() -> $(SampleDialogElement.class).id(SampleDialog.ID)).isPresent());
  }
}
