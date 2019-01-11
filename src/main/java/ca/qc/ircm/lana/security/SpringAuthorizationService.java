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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
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
  @Inject
  private AclService aclService;

  protected SpringAuthorizationService() {
  }

  protected SpringAuthorizationService(UserRepository userRepository, AclService aclService) {
    this.userRepository = userRepository;
    this.aclService = aclService;
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
  public void checkAnyRole(String... roles) throws AccessDeniedException {
    if (!hasAnyRole(roles)) {
      User user = currentUser();
      throw new AccessDeniedException(
          "User " + user + " does not have any of roles " + Arrays.toString(roles));
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

  private boolean isAuthorized(Owned owned, boolean check, Permission permission) {
    if (owned == null || owned.getOwner() == null || owned.getOwner().getId() == null) {
      return check;
    }
    User owner = owned.getOwner();
    User user = currentUser();
    if (user == null || user.getId() == null) {
      return false;
    }
    if (hasRole(ADMIN)) {
      return true;
    }
    boolean authorized = owner.getId().equals(user.getId());
    if (!authorized && hasRole(MANAGER)) {
      Laboratory ownedLaboratory = owner.getLaboratory();
      Laboratory userLaboratory = user.getLaboratory();
      if (ownedLaboratory != null && ownedLaboratory.getId() != null && userLaboratory != null
          && userLaboratory.getId() != null) {
        authorized |= ownedLaboratory.getId().equals(userLaboratory.getId());
      }
    }
    if (!authorized) {
      try {
        Acl acl = aclService.readAclById(new ObjectIdentityImpl(owned.getClass(), owned.getId()));
        authorized |=
            acl.isGranted(list(permission), list(new PrincipalSid(user.getEmail())), false);
      } catch (NotFoundException e) {
        // Assume not authorized.
      }
    }
    return authorized;
  }

  private boolean isAuthorized(Laboratory laboratory, boolean check, boolean write) {
    if (laboratory == null || laboratory.getId() == null) {
      return check;
    }
    User user = currentUser();
    if (user == null || user.getId() == null) {
      return false;
    }
    if (hasRole(ADMIN)) {
      return true;
    }
    boolean authorized = true;
    if (write) {
      authorized &= hasRole(MANAGER);
    }
    authorized &= laboratory.getId().equals(user.getLaboratory().getId());
    return authorized;
  }

  @Override
  public void checkRead(Object object) {
    if (object instanceof Owned) {
      if (!isAuthorized((Owned) object, true, BasePermission.READ)) {
        User user = currentUser();
        throw new AccessDeniedException("User " + user + " does not have access to " + object);
      }
    } else if (object instanceof Laboratory) {
      if (!isAuthorized((Laboratory) object, true, false)) {
        User user = currentUser();
        throw new AccessDeniedException("User " + user + " does not have access to " + object);
      }
    }
  }

  @Override
  public boolean hasWrite(Object object) {
    if (object instanceof Owned) {
      return isAuthorized((Owned) object, false, BasePermission.WRITE);
    } else if (object instanceof Laboratory) {
      return isAuthorized((Laboratory) object, false, true);
    } else {
      return false;
    }
  }

  @Override
  public void checkWrite(Object object) throws AccessDeniedException {
    boolean canWrite = true;
    if (object instanceof Owned) {
      canWrite &= isAuthorized((Owned) object, true, BasePermission.WRITE);
    } else if (object instanceof Laboratory) {
      canWrite &= isAuthorized((Laboratory) object, true, true);
    }
    if (!canWrite) {
      User user = currentUser();
      throw new AccessDeniedException("User " + user + " does not have access to " + object);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> list(T... values) {
    return Stream.of(values).collect(Collectors.toList());
  }
}
