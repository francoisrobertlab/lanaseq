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

package ca.qc.ircm.lana.security;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

/**
 * Authorization service.
 */
@Service
@Transactional
public class ShiroAuthorizationService implements AuthorizationService {
  @Inject
  private UserRepository userRepository;

  protected ShiroAuthorizationService() {
  }

  protected ShiroAuthorizationService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  private Subject getSubject() {
    return SecurityUtils.getSubject();
  }

  /**
   * Returns current user or null for anonymous.
   *
   * @return current user or null for anonymous
   */
  @Override
  public User currentUser() {
    Long userId = (Long) getSubject().getPrincipal();
    if (userId == null) {
      return null;
    }

    return userRepository.findById(userId).orElse(null);
  }

  /**
   * Returns true if current user is anonymous, false otherwise.
   *
   * @return true if current user is anonymous, false otherwise
   */
  @Override
  public boolean isAnonymous() {
    return getSubject().getPrincipal() == null;
  }

  /**
   * Returns true if current user has specified role, false otherwise.
   *
   * @param role
   *          role
   * @return true if current user has specified role, false otherwise
   */
  @Override
  public boolean hasRole(String role) {
    return getSubject().hasRole(role);
  }

  /**
   * Returns true if current user has any of the specified roles, false otherwise.
   *
   * @param roles
   *          roles
   * @return true if current user has any of the specified roles, false otherwise
   */
  @Override
  public boolean hasAnyRole(String... roles) {
    Subject subject = getSubject();
    boolean hasAnyRole = false;
    for (String role : roles) {
      hasAnyRole |= subject.hasRole(role);
    }
    return hasAnyRole;
  }

  /**
   * Returns true if current user is authorized to access class, false otherwise.
   *
   * @param type
   *          class
   * @return true if current user is authorized to access class, false otherwise
   */
  @Override
  public boolean isAuthorized(Class<?> type) {
    RolesAllowed rolesAllowed = AnnotationUtils.findAnnotation(type, RolesAllowed.class);
    if (rolesAllowed != null) {
      Subject subject = getSubject();
      String[] roles = rolesAllowed.value();
      boolean allowed = false;
      for (String role : roles) {
        allowed |= subject.hasRole(role);
      }
      return allowed;
    } else {
      return true;
    }
  }
}
