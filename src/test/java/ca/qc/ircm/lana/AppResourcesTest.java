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

package ca.qc.ircm.lana;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import org.junit.Test;

public class AppResourcesTest {
  private Locale locale = Locale.ENGLISH;

  @Test
  public void message_Name() {
    AppResources resources = new AppResources(Constants.class.getName(), locale);
    assertEquals("Lana", resources.message("application.name"));
  }

  @Test
  public void message_NameEnglish() {
    AppResources resources = new AppResources(Constants.class.getName(), Locale.ENGLISH);
    assertEquals("Save", resources.message("save"));
  }

  @Test
  public void message_NameFrench() {
    AppResources resources = new AppResources(Constants.class.getName(), Locale.FRENCH);
    assertEquals("Sauvegarder", resources.message("save"));
  }

  @Test
  public void message_Class() {
    AppResources resources = new AppResources(Constants.class, locale);
    assertEquals("Lana", resources.message("application.name"));
  }

  @Test
  public void message_ClassEnglish() {
    AppResources resources = new AppResources(Constants.class, Locale.ENGLISH);
    assertEquals("Save", resources.message("save"));
  }

  @Test
  public void message_ClassFrench() {
    AppResources resources = new AppResources(Constants.class, Locale.FRENCH);
    assertEquals("Sauvegarder", resources.message("save"));
  }
}
