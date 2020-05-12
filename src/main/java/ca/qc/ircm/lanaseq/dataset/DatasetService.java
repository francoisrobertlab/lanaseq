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

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AuthorizationService authorizationService;

  protected DatasetService() {
  }

  protected DatasetService(DatasetRepository repository,
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
   * Returns all datasets.
   *
   * @return all datasets
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Dataset> all() {
    return repository.findAll();
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
    User user = authorizationService.getCurrentUser();
    if (dataset.getId() == null) {
      dataset.setOwner(user);
      dataset.setDate(now);
    }
    if (dataset.getSamples() != null) {
      for (Sample sample : dataset.getSamples()) {
        if (sample.getId() == null) {
          sample.setOwner(user);
          sample.setDate(now);
        }
        sample.setDataset(dataset);
        sample.setAssay(dataset.getAssay());
        sample.setType(dataset.getType());
        sample.setTarget(dataset.getTarget());
        sample.setStrain(dataset.getStrain());
        sample.setStrainDescription(dataset.getStrainDescription());
        sample.setTreatment(dataset.getTreatment());
        sample.setProtocol(dataset.getProtocol());
      }
    }
    repository.save(dataset);
  }
}
