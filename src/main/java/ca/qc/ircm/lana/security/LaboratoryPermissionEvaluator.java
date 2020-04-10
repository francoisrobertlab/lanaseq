package ca.qc.ircm.lana.security;

import static ca.qc.ircm.lana.security.UserRole.ADMIN;
import static ca.qc.ircm.lana.security.UserRole.MANAGER;

import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.User;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * {@link PermissionEvaluator} that can evaluate permission for {@link Laboratory}.
 */
@Component
public class LaboratoryPermissionEvaluator extends AbstractPermissionEvaluator {
  @Autowired
  private LaboratoryRepository repository;
  @Autowired
  private AuthorizationService authorizationService;

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if ((authentication == null) || !(targetDomainObject instanceof Laboratory)
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Laboratory laboratory = (Laboratory) targetDomainObject;
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(laboratory, currentUser, realPermission);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if ((authentication == null) || !(targetId instanceof Long)
        || !targetType.equals(Laboratory.class.getName())
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Laboratory laboratory = repository.findById((Long) targetId).orElse(null);
    if (laboratory == null) {
      return false;
    }
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(laboratory, currentUser, realPermission);
  }

  private boolean hasPermission(Laboratory laboratory, User currentUser, Permission permission) {
    if (currentUser == null) {
      return false;
    }
    if (authorizationService.hasRole(ADMIN)) {
      return true;
    }
    if (laboratory.getId() == null) {
      return false;
    }
    boolean authorized = false;
    authorized |= permission.equals(BasePermission.READ);
    authorized |= permission.equals(BasePermission.WRITE)
        && authorizationService.hasAllRoles(MANAGER, UserAuthority.laboratoryMember(laboratory));
    return authorized;
  }
}
