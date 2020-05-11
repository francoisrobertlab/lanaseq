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

package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.LaboratoryRepository;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link Dataset}.
 */
@Service
@Transactional
public class DatasetService {
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AuthorizationService authorizationService;

  protected DatasetService() {
  }

  protected DatasetService(DatasetRepository repository, LaboratoryRepository laboratoryRepository,
      AuthorizationService authorizationService) {
    this.repository = repository;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns dataset having specified id.
   *
   * @param id
   *          dataset's id
   * @return dataset having specified id
   */
  @PostAuthorize("returnObject == null || hasPermission(returnObject, 'read')")
  public Dataset get(Long id) {
    if (id == null) {
      return null;
    }

    return repository.findById(id).orElse(null);
  }

  /**
   * Returns all datasets for current user.
   * <p>
   * If current user is a regular user, returns all datasets owned by him.
   * </p>
   * <p>
   * If current user is a manager, returns all datasets made by users in his lab.
   * </p>
   * <p>
   * If current user is an admin, returns all datasets.
   * </p>
   *
   * @return all datasets for current user
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Dataset> all() {
    if (authorizationService.hasRole(ADMIN)) {
      return repository.findAll();
    } else {
      return repository
          .findByOwnerLaboratory(authorizationService.getCurrentUser().getLaboratory());
    }
  }

  /**
   * Saves dataset into database.
   *
   * @param dataset
   *          dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void save(Dataset dataset) {
    LocalDateTime now = LocalDateTime.now();
    if (dataset.getId() == null) {
      User user = authorizationService.getCurrentUser();
      dataset.setOwner(user);
      dataset.setDate(now);
    }
    if (dataset.getSamples() != null) {
      for (Sample sample : dataset.getSamples()) {
        if (sample.getId() == null) {
          sample.setDate(now);
        }
        sample.setDataset(dataset);
      }
    }
    repository.save(dataset);
  }
}
