
package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.user.User;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * {@link PermissionEvaluator} that can evaluate permission for {@link Protocol}.
 */
@Component
public class ProtocolPermissionEvaluator extends AbstractPermissionEvaluator {
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private AuthorizationService authorizationService;

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if ((authentication == null) || !(targetDomainObject instanceof Protocol)
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Protocol protocol = (Protocol) targetDomainObject;
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(protocol, currentUser, realPermission);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if ((authentication == null) || !(targetId instanceof Long)
        || !targetType.equals(Protocol.class.getName())
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Protocol protocol = repository.findById((Long) targetId).orElse(null);
    if (protocol == null) {
      return false;
    }
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(protocol, currentUser, realPermission);
  }

  private boolean hasPermission(Protocol protocol, User currentUser, Permission permission) {
    if (currentUser == null) {
      return false;
    }
    if (authorizationService.hasRole(ADMIN)) {
      return true;
    }
    if (protocol.getId() == null) {
      return true;
    }
    User owner = protocol.getOwner();
    boolean authorized = owner.getId().equals(currentUser.getId());
    authorized |= permission.equals(BasePermission.READ)
        && authorizationService.hasRole(UserAuthority.laboratoryMember(owner.getLaboratory()));
    authorized |= permission.equals(BasePermission.WRITE) && authorizationService
        .hasAllRoles(MANAGER, UserAuthority.laboratoryMember(owner.getLaboratory()));
    return authorized;
  }
}