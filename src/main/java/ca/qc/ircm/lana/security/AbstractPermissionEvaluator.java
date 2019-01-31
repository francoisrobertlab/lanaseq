package ca.qc.ircm.lana.security;

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.CREATE;
import static org.springframework.security.acls.domain.BasePermission.DELETE;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import javax.inject.Inject;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Implements common methods to use for {@link PermissionEvaluator} implementations.
 */
public abstract class AbstractPermissionEvaluator implements PermissionEvaluator {
  @Inject
  private UserRepository userRepository;

  protected UserDetails getUserDetails(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      return (UserDetails) authentication.getPrincipal();
    } else {
      return null;
    }
  }

  protected User getUser(Authentication authentication) {
    UserDetails user = getUserDetails(authentication);
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

  protected Permission resolvePermission(Object permission) {
    if (permission instanceof Permission) {
      return (Permission) permission;
    }
    switch (permission.toString().toUpperCase()) {
      case "READ":
        return READ;
      case "WRITE":
        return WRITE;
      case "CREATE":
        return CREATE;
      case "DELETE":
        return DELETE;
      case "ADMINISTRATION":
        return ADMINISTRATION;
      default:
        throw new IllegalArgumentException("Permission " + permission + " does not exists in "
            + BasePermission.class.getSimpleName());
    }
  }
}
