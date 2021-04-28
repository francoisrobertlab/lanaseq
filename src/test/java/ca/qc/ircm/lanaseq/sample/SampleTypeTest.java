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

package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.sample.SampleType.IMMUNO_PRECIPITATION;
import static ca.qc.ircm.lanaseq.sample.SampleType.INPUT;
import static ca.qc.ircm.lanaseq.sample.SampleType.NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SampleType}.
 */
public class SampleTypeTest {
  @Test
  public void getLabel_English() {
    Locale locale = Locale.ENGLISH;
    assertEquals("Not applicable", NULL.getLabel(locale));
    assertEquals("IP", IMMUNO_PRECIPITATION.getLabel(locale));
    assertEquals("Input", INPUT.getLabel(locale));
  }

  @Test
  public void getLabel_French() {
    Locale locale = Locale.FRENCH;
    assertEquals("Non applicable", NULL.getLabel(locale));
    assertEquals("IP", IMMUNO_PRECIPITATION.getLabel(locale));
    assertEquals("Input", INPUT.getLabel(locale));
  }
}
