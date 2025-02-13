package ca.qc.ircm.lanaseq.text;

import java.text.Collator;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utilities for strings.
 */
public class Strings {

  /**
   * Concatenates properties separating them by dots.
   *
   * @param names property names
   * @return properties separated by dots
   */
  public static String property(Object... names) {
    return Arrays.stream(names).map(String::valueOf).collect(Collectors.joining("."));
  }

  /**
   * Concatenates names to create a valid CSS class name.
   *
   * @param names class names
   * @return valid CSS class name based on names
   */
  public static String styleName(Object... names) {
    return Arrays.stream(names).map(String::valueOf).map(name -> name.replaceAll("\\.", "-"))
        .collect(Collectors.joining("-"));
  }

  /**
   * Normalize value by, among other things, removing accents from characters.
   *
   * @param value value
   * @return value normalized string representation of value
   */
  public static String normalize(String value) {
    value = value.replaceAll("œ", "oe");
    value = value.replaceAll("æ", "ae");
    return Normalizer.normalize(value, Form.NFKD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  /**
   * Removes accents from characters and change string to lower case.
   *
   * @param value value
   * @return value without accents and lower case
   */
  public static String comparable(String value) {
    return normalize(value).toLowerCase();
  }

  /**
   * Returns {@link Collator} that treats modified characters (for example, accents) as non-modified
   * characters.
   *
   * @return {@link Collator} that treats modified characters as non-modified characters
   */
  public static Collator normalizedCollator() {
    Collator collator = Collator.getInstance(Locale.US);
    collator.setStrength(Collator.PRIMARY);
    return collator;
  }
}
