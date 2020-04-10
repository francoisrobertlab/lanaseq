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

package ca.qc.ircm.lanaseq.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Converts time instance to other time instance.
 */
public class TimeConverter {
  public static Instant toInstant(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant();
  }

  public static Instant toInstant(LocalDate date) {
    return date.atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant();
  }

  public static LocalDateTime toLocalDateTime(Instant instant) {
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
  }

  public static LocalDate toLocalDate(Instant instant) {
    return toLocalDateTime(instant).toLocalDate();
  }
}
