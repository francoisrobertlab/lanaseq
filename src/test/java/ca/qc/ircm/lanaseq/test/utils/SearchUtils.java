package ca.qc.ircm.lanaseq.test.utils;

import ca.qc.ircm.lanaseq.DataNullableId;
import java.util.Collection;
import java.util.Optional;

/**
 * Search utilities.
 */
public class SearchUtils {
  public static <D extends DataNullableId> Optional<D> find(Collection<D> datas, Long id) {
    return datas.stream().filter(data -> id.equals(data.getId())).findFirst();
  }

  public static <V> boolean containsInstanceOf(Collection<V> values, Class<? extends V> clazz) {
    return values.stream().filter(extension -> clazz.isInstance(extension)).findAny().isPresent();
  }

  @SuppressWarnings("unchecked")
  public static <V, R extends V> Optional<R> findInstanceOf(Collection<V> values, Class<R> clazz) {
    return values.stream().filter(extension -> clazz.isInstance(extension))
        .map(extension -> (R) extension).findAny();
  }
}
