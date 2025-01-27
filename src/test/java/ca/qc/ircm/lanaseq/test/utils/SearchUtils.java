package ca.qc.ircm.lanaseq.test.utils;

import ca.qc.ircm.lanaseq.Data;
import java.util.Collection;
import java.util.Optional;

/**
 * Search utilities.
 */
public class SearchUtils {
  public static <D extends Data> Optional<D> find(Collection<D> datas, long id) {
    return datas.stream().filter(data -> id == data.getId()).findFirst();
  }

  public static <V> boolean containsInstanceOf(Collection<V> values, Class<? extends V> clazz) {
    return values.stream().anyMatch(clazz::isInstance);
  }

  @SuppressWarnings("unchecked")
  public static <V, R extends V> Optional<R> findInstanceOf(Collection<V> values, Class<R> clazz) {
    return values.stream().filter(clazz::isInstance).map(extension -> (R) extension).findAny();
  }
}
