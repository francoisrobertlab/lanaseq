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

  private boolean isAuthorized(Owned owned, User currentUser, boolean check,
      Permission permission) {
    if (owned == null || owned.getOwner() == null || owned.getOwner().getId() == null) {
      return check;
    }
    User owner = owned.getOwner();
    if (currentUser == null || currentUser.getId() == null) {
      return false;
    }
    if (hasRole(ADMIN)) {
      return true;
    }
    boolean authorized = owner.getId().equals(currentUser.getId());
    if (!authorized && hasRole(MANAGER)) {
      authorized |= isAuthorized(owner.getLaboratory(), currentUser, check, BasePermission.READ);
    }
    if (!authorized) {
      authorized |= isAclAuthorized(owned, permission, currentUser);
    }
    return authorized;
  }

  private boolean isAuthorized(Laboratory laboratory, User currentUser, boolean check,
      Permission permission) {
    if (laboratory == null || laboratory.getId() == null) {
      return check;
    }
    if (currentUser == null || currentUser.getId() == null) {
      return false;
    }
    if (hasRole(ADMIN)) {
      return true;
    }
    boolean authorized = true;
    if (permission != BasePermission.READ) {
      authorized &= hasRole(MANAGER);
    }
    authorized &= laboratory.getId().equals(currentUser.getLaboratory().getId());
    return authorized;
  }

  private boolean isAclAuthorized(Owned owned, Permission permission, User user) {
    try {
      Acl acl = aclService.readAclById(new ObjectIdentityImpl(owned.getClass(), owned.getId()));
      return acl.isGranted(list(permission), list(new PrincipalSid(user.getEmail())), false);
    } catch (NotFoundException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> list(T... values) {
    return Stream.of(values).collect(Collectors.toList());
  }

  @Override
  public void checkRead(Object object) {
    User currentUser = currentUser();
    if (object instanceof Owned) {
      if (!isAuthorized((Owned) object, currentUser, true, BasePermission.READ)) {
        User user = currentUser();
        throw new AccessDeniedException("User " + user + " does not have access to " + object);
      }
    } else if (object instanceof Laboratory) {
      if (!isAuthorized((Laboratory) object, currentUser, true, BasePermission.READ)) {
        User user = currentUser();
        throw new AccessDeniedException("User " + user + " does not have access to " + object);
      }
    }
  }

  @Override
  public boolean hasWrite(Object object) {
    User currentUser = currentUser();
    if (object instanceof Owned) {
      return isAuthorized((Owned) object, currentUser, false, BasePermission.WRITE);
    } else if (object instanceof Laboratory) {
      return isAuthorized((Laboratory) object, currentUser, false, BasePermission.WRITE);
    } else {
      return false;
    }
  }

  @Override
  public void checkWrite(Object object) throws AccessDeniedException {
    User currentUser = currentUser();
    boolean canWrite = true;
    if (object instanceof Owned) {
      canWrite &= isAuthorized((Owned) object, currentUser, true, BasePermission.WRITE);
    } else if (object instanceof Laboratory) {
      canWrite &= isAuthorized((Laboratory) object, currentUser, true, BasePermission.WRITE);
    }
    if (!canWrite) {
      User user = currentUser();
      throw new AccessDeniedException("User " + user + " does not have access to " + object);
    }
  }
}
