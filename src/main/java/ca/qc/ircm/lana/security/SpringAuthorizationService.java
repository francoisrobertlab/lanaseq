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

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;

import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.Owned;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.util.Collection;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authorization service.
 */
@Service
@Transactional
public class SpringAuthorizationService implements AuthorizationService {
  private static final Logger logger = LoggerFactory.getLogger(SpringAuthorizationService.class);
  @Inject
  private UserRepository userRepository;

  protected SpringAuthorizationService() {
  }

  protected SpringAuthorizationService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  private UserDetails getUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
    UserDetails user = getUser();
    Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
    boolean hasRole = false;
    for (GrantedAuthority authority : authorities) {
      hasRole |= authority.getAuthority().equals(role);
    }
    logger.trace("user {} hasRole {}? {}", user.getUsername(), role, hasRole);
    return hasRole;
  }

  @Override
  public void checkRole(String role) throws AccessDeniedException {
    if (!hasRole(role)) {
      User user = currentUser();
      throw new AccessDeniedException("User " + user + " does not have role " + role);
    }
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
  public boolean isAuthorized(Class<?> type) {
    RolesAllowed rolesAllowed = AnnotationUtils.findAnnotation(type, RolesAllowed.class);
    if (rolesAllowed != null) {
      String[] roles = rolesAllowed.value();
      return hasAnyRole(roles);
    } else {
      return true;
    }
  }

  private boolean isAuthorized(Owned owned) {
    if (owned == null || owned.getOwner() == null || owned.getOwner().getId() == null) {
      return true;
    }
    User owner = owned.getOwner();
    User user = currentUser();
    if (user == null || user.getId() == null) {
      return false;
    }
    if (hasRole(ADMIN)) {
      return true;
    }
    if (hasRole(MANAGER)) {
      boolean authorized = false;
      authorized |= owner.getId().equals(user.getId());
      Laboratory ownedLaboratory = owner.getLaboratory();
      Laboratory userLaboratory = user.getLaboratory();
      if (ownedLaboratory != null && ownedLaboratory.getId() != null && userLaboratory != null
          && userLaboratory.getId() != null) {
        authorized |= ownedLaboratory.getId().equals(userLaboratory.getId());
      }
      return authorized;
    }
    return owner.getId().equals(user.getId());
  }

  @Override
  public void checkRead(Owned owned) {
    if (!isAuthorized(owned)) {
      User user = currentUser();
      throw new AccessDeniedException("User " + user + " does not have access to " + owned);
    }
  }
}
