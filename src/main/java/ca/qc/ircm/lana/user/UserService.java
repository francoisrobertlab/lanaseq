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

import ca.qc.ircm.lana.security.SecurityConfiguration;
import java.security.SecureRandom;
import java.util.List;
import javax.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
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
  private SecurityConfiguration securityConfiguration;
  /**
   * Used to generate salt for passwords.
   */
  private SecureRandom random = new SecureRandom();

  protected UserService() {
  }

  protected UserService(UserRepository repository, LaboratoryRepository laboratoryRepository,
      SecurityConfiguration securityConfiguration) {
    this.repository = repository;
    this.laboratoryRepository = laboratoryRepository;
    this.securityConfiguration = securityConfiguration;
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

    return repository.findById(id).orElse(null);
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

    return repository.findByEmail(email).orElse(null);
  }

  /**
   * Returns all users.
   *
   * @return all users
   */
  public List<User> all() {
    return repository.findAll();
  }

  /**
   * Saves user into database.
   * <p>
   * If user is a biologist, his laboratory must be defined.
   * </p>
   * <p>
   * If user is a manager, his laboratory will be saved as well.
   * </p>
   * <p>
   * If user is a not a manager, his laboratory must exists.
   * </p>
   *
   * @param user
   *          user
   * @param password
   *          user's unhashed password, can be null to keep previous password
   */
  public void save(User user, String password) {
    if (user.isAdmin()) {
      if (user.getLaboratory() != null) {
        throw new IllegalArgumentException("administrators cannot be in a laboratory");
      }
      if (user.isManager()) {
        throw new IllegalArgumentException("administrators cannot be managers");
      }
    } else {
      if (user.getLaboratory() == null) {
        throw new IllegalArgumentException("biologists must be in a laboratory");
      }
      if (!user.isManager()
          && laboratoryRepository.findById(user.getLaboratory().getId()) == null) {
        throw new IllegalArgumentException(
            "laboratory " + user.getLaboratory().getId() + " does not exists");
      }
    }

    if (user.getId() == null) {
      user.setActive(true);
    }
    if (password != null) {
      String salt = BCrypt.gensalt(securityConfiguration.getPasswordStrength(), random);
      String hashedPassword = BCrypt.hashpw(password, salt);
      user.setHashedPassword(hashedPassword);
    }
    if (user.isManager()) {
      laboratoryRepository.save(user.getLaboratory());
    }
    repository.save(user);
  }
}
