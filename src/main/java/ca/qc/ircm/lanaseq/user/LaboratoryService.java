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

package ca.qc.ircm.lanaseq.user;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link Laboratory}.
 */
@Service
@Transactional
public class LaboratoryService {
  @Autowired
  private LaboratoryRepository repository;

  protected LaboratoryService() {
  }

  protected LaboratoryService(LaboratoryRepository repository) {
    this.repository = repository;
  }

  /**
   * Returns laboratory having specified id.
   *
   * @param id
   *          laboratory's id
   * @return laboratory having specified id
   */
  public Laboratory get(Long id) {
    if (id == null) {
      return null;
    }

    return repository.findById(id).orElse(null);
  }

  /**
   * Returns all laboratories the user can access.
   *
   * @return all laboratories the user can access
   */
  public List<Laboratory> all() {
    return repository.findAll();
  }
}
