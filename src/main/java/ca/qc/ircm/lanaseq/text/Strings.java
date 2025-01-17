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
    return Arrays.asList(names).stream().map(String::valueOf)
        .collect(Collectors.joining("."));
  }

  /**
   * Concatenates names to create a valid CSS class name.
   *
   * @param names
   *          class names
   * @return valid CSS class name based on names
   */
  public static String styleName(Object... names) {
    return Arrays.asList(names).stream().map(String::valueOf)
        .map(name -> name.replaceAll("\\.", "-")).collect(Collectors.joining("-"));
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
