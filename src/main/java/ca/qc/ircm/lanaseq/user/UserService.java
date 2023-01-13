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

import static ca.qc.ircm.lanaseq.security.UserRole.USER;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link User}.
 */
@Service
@Transactional
public class UserService {
  @Autowired
  private UserRepository repository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private AuthenticatedUser authenticatedUser;

  protected UserService() {
  }

  protected UserService(UserRepository repository, PasswordEncoder passwordEncoder,
      AuthenticatedUser authenticatedUser) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * Returns user having specified id.
   *
   * @param id
   *          user's id
   * @return user having specified id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<User> get(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    return repository.findById(id);
  }

  /**
   * Returns user having specified email.
   *
   * @param email
   *          user's email
   * @return user having specified email
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<User> getByEmail(String email) {
    if (email == null) {
      return Optional.empty();
    }

    return repository.findByEmail(email);
  }

  /**
   * Returns true if a user exists with this email.
   *
   * @param email
   *          email
   * @return true if a user exists with this email
   */
  public boolean exists(String email) {
    if (email == null) {
      return false;
    }

    return repository.findByEmail(email).isPresent();
  }

  /**
   * Returns all users.
   *
   * @return all users
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<User> all() {
    return repository.findAll();
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
  @PreAuthorize("hasPermission(#user, 'write')")
  public void save(User user, String password) {
    if (user.getId() != null && user.getId() == 1L && (!user.isAdmin() || !user.isActive())) {
      throw new AccessDeniedException("user 1 must be an admin and active");
    }

    final boolean reloadAuthorities = user.isExpiredPassword() && password != null;
    if (user.getId() == null) {
      user.setActive(true);
      user.setCreationDate(LocalDateTime.now());
    }
    if (password != null) {
      String hashedPassword = passwordEncoder.encode(password);
      user.setHashedPassword(hashedPassword);
      user.setExpiredPassword(false);
    }
    repository.save(user);
    if (reloadAuthorities) {
      authenticatedUser.reloadAuthorities();
    }
  }

  /**
   * Saves new password for current user.
   *
   * @param password
   *          user's unhashed password
   */
  @PreAuthorize("hasAuthority('" + USER + "')")
  public void save(String password) {
    if (password == null) {
      throw new NullPointerException("password parameter cannot be null");
    }

    authenticatedUser.getUser().ifPresent(user -> {
      final boolean reloadAuthorities = user.isExpiredPassword();
      String hashedPassword = passwordEncoder.encode(password);
      user.setHashedPassword(hashedPassword);
      user.setExpiredPassword(false);
      repository.save(user);
      if (reloadAuthorities) {
        authenticatedUser.reloadAuthorities();
      }
    });
  }
}
