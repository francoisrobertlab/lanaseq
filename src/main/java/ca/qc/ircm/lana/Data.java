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

package ca.qc.ircm.lana;

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
   * @return data having this id within all data
   */
  public static <D extends Data> Optional<D> find(Collection<D> datas, long id) {
    return datas.stream().filter(data -> data.getId() != null && id == data.getId()).findFirst();
  }
}
