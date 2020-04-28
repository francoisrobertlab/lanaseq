package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;

import ca.qc.ircm.lanaseq.dataset.Experiment;
import ca.qc.ircm.lanaseq.dataset.ExperimentRepository;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * {@link PermissionEvaluator} that can evaluate permission for {@link Experiment}.
 */
@Component
public class ExperimentPermissionEvaluator extends AbstractPermissionEvaluator {
  @Autowired
  private ExperimentRepository repository;
  @Autowired
  private AuthorizationService authorizationService;
  @Autowired
  private AclService aclService;

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if ((authentication == null) || !(targetDomainObject instanceof Experiment)
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Experiment experiment = (Experiment) targetDomainObject;
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(experiment, currentUser, realPermission);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if ((authentication == null) || !(targetId instanceof Long)
        || !targetType.equals(Experiment.class.getName())
        || (!(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Experiment experiment = repository.findById((Long) targetId).orElse(null);
    if (experiment == null) {
      return false;
    }
    User currentUser = getUser(authentication);
    Permission realPermission = resolvePermission(permission);
    return hasPermission(experiment, currentUser, realPermission);
  }

  private boolean hasPermission(Experiment experiment, User currentUser, Permission permission) {
    if (currentUser == null) {
      return false;
    }
    if (authorizationService.hasRole(ADMIN)) {
      return true;
    }
    if (experiment.getId() == null) {
      return true;
    }
    User owner = experiment.getOwner();
    boolean authorized = owner.getId().equals(currentUser.getId());
    authorized |= permission.equals(BasePermission.READ)
        && authorizationService.hasRole(UserAuthority.laboratoryMember(owner.getLaboratory()));
    authorized |= permission.equals(BasePermission.WRITE) && authorizationService
        .hasAllRoles(MANAGER, UserAuthority.laboratoryMember(owner.getLaboratory()));
    if (!authorized) {
      authorized |= hasAclPermission(experiment, permission, currentUser);
    }
    return authorized;
  }

  private boolean hasAclPermission(Owned owned, Permission permission, User user) {
    try {
      Acl acl = aclService.readAclById(new ObjectIdentityImpl(owned.getClass(), owned.getId()));
      return acl.isGranted(list(permission),
          list(new GrantedAuthoritySid(UserAuthority.laboratoryMember(user.getLaboratory()))),
          false);
    } catch (NotFoundException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> list(T... values) {
    return Stream.of(values).collect(Collectors.toList());
  }
}
