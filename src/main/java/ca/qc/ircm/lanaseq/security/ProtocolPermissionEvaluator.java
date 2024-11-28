package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.Permission.READ;
import static ca.qc.ircm.lanaseq.security.Permission.WRITE;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * {@link PermissionEvaluator} that can evaluate permission for {@link Protocol}.
 */
@Component
public class ProtocolPermissionEvaluator extends AbstractPermissionEvaluator {
  private ProtocolRepository repository;
  private RoleValidator roleValidator;

  @Autowired
  protected ProtocolPermissionEvaluator(UserRepository userRepository,
      ProtocolRepository repository, RoleValidator roleValidator) {
    super(userRepository);
    this.repository = repository;
    this.roleValidator = roleValidator;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if (!(targetDomainObject instanceof Protocol)
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Protocol protocol = (Protocol) targetDomainObject;
    User currentUser = getUser(authentication).orElse(null);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(protocol, currentUser, realPermission);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if (!(targetId instanceof Long) || !targetType.equals(Protocol.class.getName())
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Protocol protocol = repository.findById((Long) targetId).orElse(null);
    if (protocol == null) {
      return false;
    }
    User currentUser = getUser(authentication).orElse(null);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(protocol, currentUser, realPermission);
  }

  private boolean hasPermission(Protocol protocol, @Nullable User currentUser,
      Permission permission) {
    if (currentUser == null) {
      return false;
    }
    if (roleValidator.hasRole(ADMIN)) {
      return true;
    }
    if (protocol.getId() == 0) {
      return true;
    }
    User owner = protocol.getOwner();
    boolean authorized = owner.getId() == currentUser.getId();
    authorized |= permission.equals(READ);
    authorized |= permission.equals(WRITE) && roleValidator.hasRole(MANAGER);
    return authorized;
  }
}
