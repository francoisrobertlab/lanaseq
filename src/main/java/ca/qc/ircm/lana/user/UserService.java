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
import java.util.List;
import javax.inject.Inject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
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
    authorizationService.checkPermission(user, BasePermission.READ);
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
    authorizationService.checkPermission(user, BasePermission.READ);
    return user;
  }

  /**
   * Returns all users.
   *
   * @return all users
   */
  public List<User> all() {
    authorizationService.checkRole(USER);
    return repository.findAll();
  }

  /**
   * Returns all users in laboratory.
   *
   * @param laboratory
   *          laboratory
   * @return all users in laboratory
   */
  public List<User> all(Laboratory laboratory) {
    authorizationService.checkRole(USER);
    return repository.findByLaboratory(laboratory);
  }

  /**
   * Returns laboratory's manager. <br>
   * If laboratory has many managers, returns anyone of them.
   *
   * @param laboratory
   *          laboratory
   * @return laboratory's manager
   */
  public User manager(Laboratory laboratory) {
    authorizationService.checkRole(USER);
    List<User> users = repository.findByLaboratoryAndManagerTrueAndActiveTrue(laboratory);
    return !users.isEmpty() ? users.get(0) : null;
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
    if (user.getId() != null && user.getId() == 1L && (!user.isAdmin() || !user.isActive())) {
      throw new AccessDeniedException("user 1 must be an admin and active");
    }
    if (user.getLaboratory() == null) {
      throw new IllegalArgumentException("users must be in a laboratory");
    }
    if (!user.isManager()
        && !laboratoryRepository.findById(user.getLaboratory().getId()).isPresent()) {
      throw new IllegalArgumentException(
          "laboratory " + user.getLaboratory().getId() + " does not exists");
    }

    authorizationService.checkPermission(user, BasePermission.WRITE);
    final boolean reloadAuthorities = user.isExpiredPassword() && password != null;
    if (user.getId() == null) {
      authorizationService.checkAnyRole(ADMIN, MANAGER);
      user.setActive(true);
      user.setDate(LocalDateTime.now());
    }
    if (user.isAdmin()) {
      authorizationService.checkRole(ADMIN);
    }
    if (password != null) {
      String hashedPassword = passwordEncoder.encode(password);
      user.setHashedPassword(hashedPassword);
      user.setExpiredPassword(false);
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
    if (reloadAuthorities) {
      authorizationService.reloadAuthorities();
    }
  }

  /**
   * Saves new password for current user.
   *
   * @param password
   *          user's unhashed password
   */
  public void save(String password) {
    if (password == null) {
      throw new NullPointerException("password parameter cannot be null");
    }
    User user = authorizationService.currentUser();
    authorizationService.checkPermission(user, BasePermission.WRITE);

    final boolean reloadAuthorities = user.isExpiredPassword();
    String hashedPassword = passwordEncoder.encode(password);
    user.setHashedPassword(hashedPassword);
    user.setExpiredPassword(false);
    repository.save(user);
    if (reloadAuthorities) {
      authorizationService.reloadAuthorities();
    }
  }

  private void deleteLaboratoryIfEmpty(Laboratory laboratory) {
    if (laboratory != null && repository.countByLaboratory(laboratory) == 0) {
      laboratoryRepository.delete(laboratory);
    }
  }
}
