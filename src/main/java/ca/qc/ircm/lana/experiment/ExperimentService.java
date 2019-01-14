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

package ca.qc.ircm.lana.experiment;

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.USER;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.User;
import java.time.LocalDateTime;
import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link Experiment}.
 */
@Service
@Transactional
public class ExperimentService {
  @Inject
  private ExperimentRepository repository;
  @Inject
  private AuthorizationService authorizationService;

  protected ExperimentService() {
  }

  protected ExperimentService(ExperimentRepository repository,
      AuthorizationService authorizationService) {
    this.repository = repository;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns experiment having specified id.
   *
   * @param id
   *          experiment's id
   * @return experiment having specified id
   */
  public Experiment get(Long id) {
    if (id == null) {
      return null;
    }

    Experiment experiment = repository.findById(id).orElse(null);
    authorizationService.checkRead(experiment);
    return experiment;
  }

  /**
   * Returns all experiments for current user.
   * <p>
   * If current user is a regular user, returns all experiments owned by him.
   * </p>
   * <p>
   * If current user is a manager, returns all experiments made by users in his lab.
   * </p>
   * <p>
   * If current user is an admin, returns all experiments.
   * </p>
   *
   * @return all experiments for current user
   */
  public List<Experiment> all() {
    authorizationService.checkRole(USER);

    if (authorizationService.hasRole(ADMIN)) {
      return repository.findAll();
    } else {
      return repository.findByOwnerLaboratory(authorizationService.currentUser().getLaboratory());
    }
  }

  /**
   * Saves experiment into database.
   *
   * @param experiment
   *          experiment
   */
  public void save(Experiment experiment) {
    authorizationService.checkRole(USER);

    User user = authorizationService.currentUser();
    experiment.setOwner(user);
    experiment.setDate(LocalDateTime.now());
    repository.save(experiment);
  }
}
