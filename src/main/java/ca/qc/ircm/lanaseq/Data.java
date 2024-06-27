package ca.qc.ircm.lanaseq;

import java.util.Collection;
import java.util.Optional;

/**
 * Data in the database.
 */
public interface Data {
  /**
   * Returns database's identifier.
   *
   * @return database identifier
   */
  public Long getId();

  /**
   * Finds data having this id within all data.
   *
   * @param datas
   *          all data
   * @param id
   *          id
   * @param <D>
   *          instances of {@link Data}
   * @return data having this id within all data
   */
  public static <D extends Data> Optional<D> find(Collection<D> datas, long id) {
    return datas.stream().filter(data -> data.getId() != null && id == data.getId()).findFirst();
  }
}
