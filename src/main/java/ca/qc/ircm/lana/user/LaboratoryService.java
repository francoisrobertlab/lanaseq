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

package ca.qc.ircm.lana.user;

import static ca.qc.ircm.lana.security.UserRole.ADMIN;

import ca.qc.ircm.lana.security.AuthorizationService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @Autowired
  private AuthorizationService authorizationService;

  protected LaboratoryService() {
  }

  protected LaboratoryService(LaboratoryRepository repository,
      AuthorizationService authorizationService) {
    this.repository = repository;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns laboratory having specified id.
   *
   * @param id
   *          laboratory's id
   * @return laboratory having specified id
   */
  @PostAuthorize("returnObject == null || hasPermission(returnObject, 'read')")
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
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Laboratory> all() {
    if (authorizationService.hasRole(ADMIN)) {
      return repository.findAll();
    } else {
      return Stream.of(authorizationService.currentUser().getLaboratory())
          .collect(Collectors.toCollection(ArrayList::new));
    }
  }

  /**
   * Saves laboratory into database.
   *
   * @param laboratory
   *          laboratory
   */
  @PreAuthorize("hasPermission(#laboratory, 'write')")
  public void save(Laboratory laboratory) {
    if (laboratory.getId() == null) {
      throw new IllegalArgumentException("cannot create a new laboratory without a user");
    }

    repository.save(laboratory);
  }
}
