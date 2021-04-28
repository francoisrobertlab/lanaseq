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

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utilities for strings.
 */
public class Strings {
  /**
   * Concatenates properties separating them by dots.
   *
   * @param names
   *          property names
   * @return properties separated by dots
   */
  public static String property(Object... names) {
    return Arrays.asList(names).stream().filter(name -> name != null)
        .map(name -> String.valueOf(name)).collect(Collectors.joining("."));
  }

  /**
   * Concatenates names to create a valid CSS class name.
   *
   * @param names
   *          class names
   * @return valid CSS class name based on names
   */
  public static String styleName(Object... names) {
    return Arrays.asList(names).stream().filter(name -> name != null)
        .map(name -> String.valueOf(name)).map(name -> name.replaceAll("\\.", "-"))
        .collect(Collectors.joining("-"));
  }

  /**
   * Removes accents from characters.
   *
   * @param value
   *          value
   * @return value without accents
   */
  public static String normalize(String value) {
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  /**
   * Removes accents from characters and change string to lower case.
   *
   * @param value
   *          value
   * @return value without accents and lower case
   */
  public static String comparable(String value) {
    return normalize(value).toLowerCase();
  }
}
