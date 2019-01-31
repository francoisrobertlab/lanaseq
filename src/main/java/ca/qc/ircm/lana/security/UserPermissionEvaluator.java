package ca.qc.ircm.lana.security;

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserAuthority;
import ca.qc.ircm.lana.user.UserRepository;
import java.io.Serializable;
import javax.inject.Inject;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * {@link PermissionEvaluator} that can evaluate permission for {@link User}.
 */
@Component
public class UserPermissionEvaluator extends AbstractPermissionEvaluator {
  @Inject
  private UserRepository repository;
  @Inject
  private AuthorizationService authorizationService;

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if ((authentication == null) || !(targetDomainObject instanceof User)
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    User user = (User) targetDomainObject;
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(user, currentUser, realPermission);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if ((authentication == null) || !(targetId instanceof Long)
        || !targetType.equals(User.class.getName())
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    User user = repository.findById((Long) targetId).orElse(null);
    if (user == null) {
      return false;
    }
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(user, currentUser, realPermission);
  }

  private boolean hasPermission(User user, User currentUser, Permission permission) {
    if (currentUser == null) {
      return false;
    }
    if (authorizationService.hasRole(ADMIN)) {
      return true;
    }
    if (user.getLaboratory() == null || user.getLaboratory().getId() == null || user.isAdmin()) {
      return false;
    }
    if (permission.equals(BasePermission.WRITE) && user.getId() != null) {
      User unmodified = repository.findById(user.getId()).orElse(null);
      if (!unmodified.getLaboratory().getId().equals(user.getLaboratory().getId())) {
        return false;
      }
    }
    boolean authorized = currentUser.getId().equals(user.getId());
    authorized |= permission.equals(BasePermission.READ);
    authorized |= permission.equals(BasePermission.WRITE) && authorizationService
        .hasAllRoles(MANAGER, UserAuthority.laboratoryMember(user.getLaboratory()));
    return authorized;
  }
}
