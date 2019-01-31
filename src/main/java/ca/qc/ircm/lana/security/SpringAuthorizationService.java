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

import static ca.qc.ircm.lana.user.UserAuthority.FORCE_CHANGE_PASSWORD;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.util.Collection;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authorization service using Spring.
 */
@Service("authorizationService")
@Transactional
public class SpringAuthorizationService implements AuthorizationService {
  private static final Logger logger = LoggerFactory.getLogger(SpringAuthorizationService.class);
  @Inject
  private UserRepository userRepository;
  @Inject
  private UserDetailsService userDetailsService;
  @Inject
  private PermissionEvaluator permissionEvaluator;

  protected SpringAuthorizationService() {
  }

  protected SpringAuthorizationService(UserRepository userRepository,
      UserDetailsService userDetailsService, PermissionEvaluator permissionEvaluator) {
    this.userRepository = userRepository;
    this.userDetailsService = userDetailsService;
    this.permissionEvaluator = permissionEvaluator;
  }

  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  private UserDetails getUser() {
    Authentication authentication = getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      return (UserDetails) authentication.getPrincipal();
    } else {
      return null;
    }
  }

  @Override
  public User currentUser() {
    UserDetails user = getUser();
    if (user instanceof AuthenticatedUser) {
      Long userId = ((AuthenticatedUser) user).getId();
      if (userId == null) {
        return null;
      }

      return userRepository.findById(userId).orElse(null);
    } else {
      return null;
    }
  }

  @Override
  public boolean isAnonymous() {
    return getUser() == null;
  }

  @Override
  public boolean hasRole(String role) {
    Authentication authentication = getAuthentication();
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    boolean hasRole = false;
    for (GrantedAuthority authority : authorities) {
      hasRole |= authority.getAuthority().equals(role);
    }
    logger.trace("user {} hasRole {}? {}", authentication.getName(), role, hasRole);
    return hasRole;
  }

  @Override
  public boolean hasAnyRole(String... roles) {
    boolean hasAnyRole = false;
    for (String role : roles) {
      hasAnyRole |= hasRole(role);
    }
    return hasAnyRole;
  }

  @Override
  public boolean hasAllRoles(String... roles) {
    boolean hasAllRole = true;
    for (String role : roles) {
      hasAllRole &= hasRole(role);
    }
    return hasAllRole;
  }

  @Override
  public void reloadAuthorities() {
    if (hasRole(FORCE_CHANGE_PASSWORD)) {
      Authentication oldAuthentication = getAuthentication();
      logger.debug("reload authorities from user {}", oldAuthentication.getName());
      UserDetails userDetails = userDetailsService.loadUserByUsername(oldAuthentication.getName());
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, oldAuthentication.getCredentials(), userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  }

  @Override
  public boolean isAuthorized(Class<?> type) {
    RolesAllowed rolesAllowed = AnnotationUtils.findAnnotation(type, RolesAllowed.class);
    if (rolesAllowed != null) {
      String[] roles = rolesAllowed.value();
      return hasAnyRole(roles);
    } else {
      return true;
    }
  }

  @Override
  public boolean hasPermission(Object object, Permission permission) {
    if (object == null) {
      return false;
    }
    return permissionEvaluator.hasPermission(getAuthentication(), object, permission);
  }
}
