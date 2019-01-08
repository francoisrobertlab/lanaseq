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

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;

import ca.qc.ircm.lana.security.AuthorizationService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link User}.
 */
@Service
@Transactional
public class UserService {
  @Inject
  private UserRepository repository;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  @Inject
  private PasswordEncoder passwordEncoder;
  @Inject
  private AuthorizationService authorizationService;

  protected UserService() {
  }

  protected UserService(UserRepository repository, LaboratoryRepository laboratoryRepository,
      PasswordEncoder passwordEncoder, AuthorizationService authorizationService) {
    this.repository = repository;
    this.laboratoryRepository = laboratoryRepository;
    this.passwordEncoder = passwordEncoder;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns user having specified id.
   *
   * @param id
   *          user's id
   * @return user having specified id
   */
  public User get(Long id) {
    if (id == null) {
      return null;
    }

    User user = repository.findById(id).orElse(null);
    authorizationService.checkRead(user);
    return user;
  }

  /**
   * Returns user having specified email.
   *
   * @param email
   *          user's email
   * @return user having specified email
   */
  public User getByEmail(String email) {
    if (email == null) {
      return null;
    }

    User user = repository.findByEmail(email).orElse(null);
    authorizationService.checkRead(user);
    return user;
  }

  /**
   * Returns all users the user can access.
   *
   * @return all users the user can access
   */
  public List<User> all() {
    authorizationService.checkRole(USER);
    if (authorizationService.hasRole(ADMIN)) {
      return repository.findAll();
    } else if (authorizationService.hasRole(MANAGER)) {
      Laboratory laboratory = authorizationService.currentUser().getLaboratory();
      return repository.findByLaboratory(laboratory);
    } else {
      User user = authorizationService.currentUser();
      return Stream.of(user).collect(Collectors.toCollection(ArrayList::new));
    }
  }

  /**
   * Saves user into database.
   * <p>
   * If user is a normal user, his laboratory must exists.
   * </p>
   * <p>
   * If user is a manager, his laboratory will be created.
   * </p>
   *
   * @param user
   *          user
   * @param password
   *          user's unhashed password; required for new users; can be null to keep previous
   *          password
   */
  public void save(User user, String password) {
    if (user.getId() != null && user.getId() == 1L && !user.isAdmin()) {
      throw new AccessDeniedException("user 1 must be an admin");
    }
    if (user.getLaboratory() == null) {
      throw new IllegalArgumentException("users must be in a laboratory");
    }
    if (!user.isManager()
        && !laboratoryRepository.findById(user.getLaboratory().getId()).isPresent()) {
      throw new IllegalArgumentException(
          "laboratory " + user.getLaboratory().getId() + " does not exists");
    }

    authorizationService.checkWrite(user);
    user.setDate(LocalDateTime.now());
    if (user.getId() == null) {
      authorizationService.checkAnyRole(ADMIN, MANAGER);
      user.setActive(true);
    }
    if (user.isAdmin()) {
      authorizationService.checkRole(ADMIN);
    }
    if (password != null) {
      String hashedPassword = passwordEncoder.encode(password);
      user.setHashedPassword(hashedPassword);
    }
    if (user.getLaboratory().getId() == null) {
      authorizationService.checkRole(ADMIN);
      user.getLaboratory().setDate(LocalDateTime.now());
      laboratoryRepository.save(user.getLaboratory());
    }
    final Laboratory oldLaboratory = user.getId() != null
        ? repository.findById(user.getId()).map(old -> old.getLaboratory()).orElse(null)
        : null;
    if (oldLaboratory != null && !oldLaboratory.getId().equals(user.getLaboratory().getId())) {
      authorizationService.checkRole(ADMIN);
    }
    repository.save(user);
    deleteLaboratoryIfEmpty(oldLaboratory);
  }

  private void deleteLaboratoryIfEmpty(Laboratory laboratory) {
    if (laboratory != null && repository.countByLaboratory(laboratory) == 0) {
      laboratoryRepository.delete(laboratory);
    }
  }
}
