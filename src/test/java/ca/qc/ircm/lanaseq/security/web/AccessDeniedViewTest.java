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

package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.HEADER;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.HOME;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.MESSAGE;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.web.UsersView;
import com.vaadin.flow.component.UI;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link AccessDeniedView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AccessDeniedViewTest extends SpringUIUnitTest {
  private AccessDeniedView view;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AccessDeniedView.class, locale);
  private AppResources generalResources = new AppResources(Constants.class, locale);

  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    assertThrows(IllegalArgumentException.class, () -> navigate(UsersView.class));
    view = $(AccessDeniedView.class).first();
  }

  @Test
  public void styles() {
    assertTrue(view.getContent().getId().orElse("").equals(VIEW_NAME));
    assertTrue(view.header.hasClassName(HEADER));
    assertTrue(view.message.hasClassName(MESSAGE));
    assertTrue(view.home.hasClassName(HOME));
  }

  @Test
  public void labels() {
    assertEquals(resources.message(HEADER), view.header.getText());
    assertEquals(resources.message(MESSAGE), view.message.getText());
    assertEquals(resources.message(HOME), view.home.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(AccessDeniedView.class, locale);
    UI.getCurrent().setLocale(locale);
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
