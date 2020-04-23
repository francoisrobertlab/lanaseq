package ca.qc.ircm.lanaseq.security;

import ca.qc.ircm.lanaseq.experiment.Experiment;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.User;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link PermissionEvaluator}.
 */
@Component
@Primary
public class PermissionEvaluatorDelegator implements PermissionEvaluator {
  @Autowired
  private LaboratoryPermissionEvaluator laboratoryPermissionEvaluator;
  @Autowired
  private UserPermissionEvaluator userPermissionEvaluator;
  @Autowired
  private ExperimentPermissionEvaluator experimentPermissionEvaluator;
  @Autowired
  private ProtocolPermissionEvaluator protocolPermissionEvaluator;

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if (targetDomainObject instanceof Laboratory) {
      return laboratoryPermissionEvaluator.hasPermission(authentication, targetDomainObject,
          permission);
    } else if (targetDomainObject instanceof User) {
      return userPermissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
    } else if (targetDomainObject instanceof Experiment) {
      return experimentPermissionEvaluator.hasPermission(authentication, targetDomainObject,
          permission);
    } else if (targetDomainObject instanceof Protocol) {
      return protocolPermissionEvaluator.hasPermission(authentication, targetDomainObject,
          permission);
    }
    return false;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if (targetType.equals(Laboratory.class.getName())) {
      return laboratoryPermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    } else if (targetType.equals(User.class.getName())) {
      return userPermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    } else if (targetType.equals(Experiment.class.getName())) {
      return experimentPermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    } else if (targetType.equals(Protocol.class.getName())) {
      return protocolPermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    }
    return false;
  }
}
