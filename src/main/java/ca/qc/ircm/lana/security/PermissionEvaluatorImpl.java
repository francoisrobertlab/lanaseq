package ca.qc.ircm.lana.security;

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.CREATE;
import static org.springframework.security.acls.domain.BasePermission.DELETE;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

import java.io.Serializable;
import javax.inject.Inject;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link PermissionEvaluator}.
 */
@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {
  @Inject
  private AuthorizationService authorizationService;

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if ((authentication == null) || (targetDomainObject == null)
        || !(permission instanceof String)) {
      return false;
    }
    return authorizationService.hasPermission(targetDomainObject,
        resolvePermission((String) permission));
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    throw new UnsupportedOperationException(
        "Use hasPermission(Authentication, Object, Object) instead");
  }

  private Permission resolvePermission(String permission) {
    switch (permission.toUpperCase()) {
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
