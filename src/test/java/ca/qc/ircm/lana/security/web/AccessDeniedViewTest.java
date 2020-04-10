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

package ca.qc.ircm.lana.security.web;

import static ca.qc.ircm.lana.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lana.Constants.TITLE;
import static ca.qc.ircm.lana.security.web.AccessDeniedView.HEADER;
import static ca.qc.ircm.lana.security.web.AccessDeniedView.HOME;
import static ca.qc.ircm.lana.security.web.AccessDeniedView.MESSAGE;
import static ca.qc.ircm.lana.security.web.AccessDeniedView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.Constants;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class AccessDeniedViewTest extends AbstractViewTestCase {
  private AccessDeniedView view;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AccessDeniedView.class, locale);
  private AppResources generalResources = new AppResources(Constants.class, locale);

  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new AccessDeniedView();
  }

  @Test
  public void styles() {
    assertTrue(view.getContent().getId().orElse("").equals(VIEW_NAME));
    assertTrue(view.header.getClassNames().contains(HEADER));
    assertTrue(view.message.getClassNames().contains(MESSAGE));
    assertTrue(view.home.getClassNames().contains(HOME));
  }

  @Test
  public void labels() {
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(resources.message(MESSAGE), view.message.getText());
    assertEquals(resources.message(HOME), view.home.getText());
  }

  @Test
  public void localeChange() {
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(AccessDeniedView.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(resources.message(MESSAGE), view.message.getText());
    assertEquals(resources.message(HOME), view.home.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, generalResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }
}
