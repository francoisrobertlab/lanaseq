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

package ca.qc.ircm.lana.time;

import static ca.qc.ircm.lana.time.TimeConverter.toInstant;
import static ca.qc.ircm.lana.time.TimeConverter.toLocalDate;
import static ca.qc.ircm.lana.time.TimeConverter.toLocalDateTime;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.Test;

public class TimeConverterTest {
  @Test
  public void toInstant_LocalDateTime() {
    LocalDateTime dateTime1 = LocalDateTime.now();
    LocalDateTime dateTime2 = LocalDateTime.now().minusMinutes(10);

    assertEquals(dateTime1.atZone(ZoneId.systemDefault()).toInstant(), toInstant(dateTime1));
    assertEquals(dateTime2.atZone(ZoneId.systemDefault()).toInstant(), toInstant(dateTime2));
  }

  @Test
  public void toInstant_LocalDate() {
    LocalDate date1 = LocalDate.now();
    LocalDate date2 = LocalDate.now().minusDays(2);

    assertEquals(date1.atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant(), toInstant(date1));
    assertEquals(date2.atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant(), toInstant(date2));
  }

  @Test
  public void toLocalDateTime_Instant() {
    Instant instant1 = Instant.now();
    Instant instant2 = Instant.now().minus(10, ChronoUnit.MINUTES);

    assertEquals(LocalDateTime.ofInstant(instant1, ZoneId.systemDefault()),
        toLocalDateTime(instant1));
    assertEquals(LocalDateTime.ofInstant(instant2, ZoneId.systemDefault()),
        toLocalDateTime(instant2));
  }

  @Test
  public void toLocalDate_Instant() {
    Instant instant1 = Instant.now();
    Instant instant2 = Instant.now().minus(10, ChronoUnit.MINUTES);

    assertEquals(LocalDateTime.ofInstant(instant1, ZoneId.systemDefault()).toLocalDate(),
        toLocalDate(instant1));
    assertEquals(LocalDateTime.ofInstant(instant2, ZoneId.systemDefault()).toLocalDate(),
        toLocalDate(instant2));
  }
}
