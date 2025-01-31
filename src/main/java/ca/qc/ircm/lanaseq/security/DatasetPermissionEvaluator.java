package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.Permission.READ;
import static ca.qc.ircm.lanaseq.security.Permission.WRITE;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * {@link PermissionEvaluator} that can evaluate permission for {@link Dataset}.
 */
@Component
public class DatasetPermissionEvaluator extends AbstractPermissionEvaluator {

  private final DatasetRepository repository;
  private final RoleValidator roleValidator;

  @Autowired
  protected DatasetPermissionEvaluator(UserRepository userRepository, DatasetRepository repository,
      RoleValidator roleValidator) {
    super(userRepository);
    this.repository = repository;
    this.roleValidator = roleValidator;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if (!(targetDomainObject instanceof Dataset dataset)
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    User currentUser = getUser(authentication).orElse(null);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(dataset, currentUser, realPermission);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if (!(targetId instanceof Long) || !targetType.equals(Dataset.class.getName())
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Dataset dataset = repository.findById((Long) targetId).orElse(null);
    if (dataset == null) {
      return false;
    }
    User currentUser = getUser(authentication).orElse(null);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(dataset, currentUser, realPermission);
  }

  private boolean hasPermission(Dataset dataset, @Nullable User currentUser,
      Permission permission) {
    if (currentUser == null) {
      return false;
    }
    if (roleValidator.hasRole(ADMIN)) {
      return true;
    }
    if (dataset.getId() == 0) {
      return true;
    }
    User owner = dataset.getOwner();
    boolean authorized = owner.getId() == currentUser.getId();
    authorized |= permission.equals(READ);
    authorized |= permission.equals(WRITE) && roleValidator.hasRole(MANAGER);
    return authorized;
  }
}
