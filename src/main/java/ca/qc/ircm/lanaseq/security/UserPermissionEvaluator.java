package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;

import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
  private static final Logger logger = LoggerFactory.getLogger(UserPermissionEvaluator.class);
  @Autowired
  private UserRepository repository;
  @Autowired
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
    if (realPermission.equals(BasePermission.WRITE)) {
      logger.debug("hasPermission={} for user {} and current user {}",
          hasPermission(user, currentUser, realPermission), user, currentUser);
    }
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
    logger.debug("before current user test for user {} and current user {}", user, currentUser);
    if (currentUser == null) {
      return false;
    }
    logger.debug("before admin test for user {} and current user {}", user, currentUser);
    if (authorizationService.hasRole(ADMIN)) {
      return true;
    }
    logger.debug("before new laboratory test for user {} and current user {}", user, currentUser);
    if (user.getLaboratory() == null || user.getLaboratory().getId() == null || user.isAdmin()) {
      return false;
    }
    logger.debug("before labid test for user {} and current user {}", user, currentUser);
    if (permission.equals(BasePermission.WRITE) && user.getId() != null) {
      User unmodified = repository.findById(user.getId()).orElse(null);
      if (!unmodified.getLaboratory().getId().equals(user.getLaboratory().getId())) {
        return false;
      }
    }
    logger.debug("before role test for user {} and current user {}", user, currentUser);
    boolean authorized = currentUser.getId().equals(user.getId());
    authorized |= permission.equals(BasePermission.READ);
    authorized |= permission.equals(BasePermission.WRITE) && authorizationService
        .hasAllRoles(MANAGER, UserAuthority.laboratoryMember(user.getLaboratory()));
    return authorized;
  }
}
