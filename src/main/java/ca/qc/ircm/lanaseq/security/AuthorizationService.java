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

package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserAuthority.FORCE_CHANGE_PASSWORD;

import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Authorization service.
 */
@Service
public class AuthorizationService {
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserDetailsService userDetailsService;
  @Autowired
  private RoleValidator roleValidator;
  @Autowired
  private PermissionEvaluator permissionEvaluator;

  protected AuthorizationService() {
  }

  protected AuthorizationService(UserRepository userRepository,
      UserDetailsService userDetailsService, RoleValidator roleValidator,
      PermissionEvaluator permissionEvaluator) {
    this.userRepository = userRepository;
    this.userDetailsService = userDetailsService;
    this.roleValidator = roleValidator;
    this.permissionEvaluator = permissionEvaluator;
  }

  private Optional<Authentication> getAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
  }

  private Optional<UserDetails> getUser() {
    return getAuthentication().filter(au -> au.getPrincipal() instanceof UserDetails)
        .map(au -> ((UserDetails) au.getPrincipal()));
  }

  /**
   * Returns current user or empty for anonymous.
   *
   * @return current user or empty for anonymous
   */
  public Optional<User> getCurrentUser() {
    return getUser().filter(user -> user instanceof UserDetailsWithId)
        .map(user -> ((UserDetailsWithId) user))
        .map(user -> userRepository.findById(user.getId()).orElse(null));
  }

  /**
   * Returns true if current user is anonymous, false otherwise.
   *
   * @return true if current user is anonymous, false otherwise
   */
  public boolean isAnonymous() {
    return !getUser().isPresent();
  }

  /**
   * Returns true if current user has specified role, false otherwise.
   *
   * @param role
   *          role
   * @return true if current user has specified role, false otherwise
   */
  public boolean hasRole(String role) {
    return roleValidator.hasRole(role);
  }

  /**
   * Returns true if current user has any of the specified roles, false otherwise.
   *
   * @param roles
   *          roles
   * @return true if current user has any of the specified roles, false otherwise
   */
  public boolean hasAnyRole(String... roles) {
    return roleValidator.hasAnyRole(roles);
  }

  /**
   * Returns true if current user has all of the specified roles, false otherwise.
   *
   * @param roles
   *          roles
   * @return true if current user has all of the specified roles, false otherwise
   */
  public boolean hasAllRoles(String... roles) {
    return roleValidator.hasAllRoles(roles);
  }

  /**
   * Reload current user's authorities.
   */
  public void reloadAuthorities() {
    if (roleValidator.hasRole(FORCE_CHANGE_PASSWORD)) {
      getAuthentication().ifPresent(oldAuthentication -> {
        logger.debug("reload authorities from user {}", oldAuthentication.getName());
        UserDetails userDetails =
            userDetailsService.loadUserByUsername(oldAuthentication.getName());
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, oldAuthentication.getCredentials(),
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      });
    }
  }

  /**
   * Returns true if current user is authorized to access class, false otherwise.
   *
   * @param type
   *          class
   * @return true if current user is authorized to access class, false otherwise
   */
  public boolean isAuthorized(Class<?> type) {
    RolesAllowed rolesAllowed = AnnotationUtils.findAnnotation(type, RolesAllowed.class);
    if (rolesAllowed != null) {
      String[] roles = rolesAllowed.value();
      return roleValidator.hasAnyRole(roles);
    } else {
      return true;
    }
  }

  /**
   * Returns true if current user has permission on object, false otherwise.
   *
   * @param object
   *          object
   * @param permission
   *          permission
   * @return true if current user has permission on object, false otherwise
   */
  public boolean hasPermission(Object object, Permission permission) {
    return getAuthentication().map(au -> permissionEvaluator.hasPermission(au, object, permission))
        .orElse(false);
  }
}
