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

import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import com.google.common.collect.Range;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Filters samples.
 */
public class SampleFilter implements Predicate<Sample> {
  public String nameContains;
  public String protocolContains;
  public Range<LocalDate> dateRange;
  public String ownerContains;

  @Override
  public boolean test(Sample sample) {
    boolean test = true;
    if (nameContains != null) {
      test &= comparable(replaceNull(sample.getName())).contains(comparable(nameContains));
    }
    if (protocolContains != null) {
      test &= comparable(replaceNull(sample.getProtocol().getName()))
          .contains(comparable(protocolContains));
    }
    if (dateRange != null) {
      test &= dateRange.contains(sample.getDate());
    }
    if (ownerContains != null) {
      test &=
          comparable(replaceNull(sample.getOwner().getEmail())).contains(comparable(ownerContains))
              || comparable(replaceNull(sample.getOwner().getName()))
                  .contains(comparable(ownerContains));
    }
    return test;
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
