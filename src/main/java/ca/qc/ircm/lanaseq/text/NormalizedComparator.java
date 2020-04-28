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

package ca.qc.ircm.lanaseq.text;

import static ca.qc.ircm.lanaseq.text.Strings.normalize;

import com.vaadin.flow.function.SerializableFunction;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * Comparator that normalizes string before comparison.
 */
public class NormalizedComparator<T> implements Comparator<T>, Serializable {
  private static final long serialVersionUID = -1607340161804603169L;
  private final SerializableFunction<T, String> converter;

  public NormalizedComparator(SerializableFunction<T, String> converter) {
    this.converter = converter;
  }

  public static <T> NormalizedComparator<T> of(SerializableFunction<T, String> converter) {
    return new NormalizedComparator<T>(converter);
  }

  @Override
  public int compare(T o1, T o2) {
    String s1 = convert(o1);
    String s2 = convert(o2);
    return normalize(s1).compareToIgnoreCase(normalize(s2));
  }

  private String convert(T ot) {
    try {
      return Objects.toString(converter.apply(ot), "");
    } catch (Throwable e) {
      return "";
    }
  }

  public SerializableFunction<T, String> getConverter() {
    return converter;
  }
}
