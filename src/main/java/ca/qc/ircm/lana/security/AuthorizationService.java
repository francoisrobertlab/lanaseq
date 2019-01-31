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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.Permission;

/**
 * Authorization service.
 */
public interface AuthorizationService {
  /**
   * Returns current user or null for anonymous.
   *
   * @return current user or null for anonymous
   */
  public User currentUser();

  /**
   * Returns true if current user is anonymous, false otherwise.
   *
   * @return true if current user is anonymous, false otherwise
   */
  public boolean isAnonymous();

  /**
   * Returns true if current user has specified role, false otherwise.
   *
   * @param role
   *          role
   * @return true if current user has specified role, false otherwise
   */
  public boolean hasRole(String role);

  /**
   * Returns true if current user has any of the specified roles, false otherwise.
   *
   * @param roles
   *          roles
   * @return true if current user has any of the specified roles, false otherwise
   */
  public boolean hasAnyRole(String... roles);

  /**
   * Returns true if current user has all of the specified roles, false otherwise.
   *
   * @param roles
   *          roles
   * @return true if current user has all of the specified roles, false otherwise
   */
  public boolean hasAllRoles(String... roles);

  /**
   * Checks if user has specified role.
   *
   * @param role
   *          role
   * @throws AccessDeniedException
   *           user does not have specified role
   */
  public void checkRole(String role) throws AccessDeniedException;

  /**
   * Checks if user has any of the specified roles.
   *
   * @param roles
   *          roles
   * @throws AccessDeniedException
   *           user does not have any of the specified roles
   */
  public void checkAnyRole(String... roles) throws AccessDeniedException;

  /**
   * Reload current user's authorities.
   */
  public void reloadAuthorities();

  /**
   * Returns true if current user is authorized to access class, false otherwise.
   *
   * @param type
   *          class
   * @return true if current user is authorized to access class, false otherwise
   */
  public boolean isAuthorized(Class<?> type);

  /**
   * Returns true if current user has permission on object, false otherwise.
   *
   * @param object
   *          object
   * @param permission
   *          permission
   * @return true if current user has permission on object, false otherwise
   */
  public boolean hasPermission(Object object, Permission permission);

  /**
   * Checks if current user has permission on object.
   *
   * @param object
   *          object
   * @param permission
   *          permission
   * @throws AccessDeniedException
   *           user cannot read object
   */
  public void checkPermission(Object object, Permission permission) throws AccessDeniedException;
}
