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
