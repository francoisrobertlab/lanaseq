package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;

import ca.qc.ircm.lanaseq.UsedBy;
import ca.qc.ircm.lanaseq.message.Message;
import ca.qc.ircm.lanaseq.message.MessageRepository;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * {@link PermissionEvaluator} that can evaluate permission for {@link Message}.
 */
@Component
public class MessagePermissionEvaluator extends AbstractPermissionEvaluator {

  private final MessageRepository repository;
  private final RoleValidator roleValidator;

  /**
   * Create MessagePermissionEvaluator.
   *
   * @param userRepository user repository
   * @param repository     message repository
   * @param roleValidator  role validator
   */
  @Autowired
  @UsedBy(SPRING)
  protected MessagePermissionEvaluator(UserRepository userRepository, MessageRepository repository,
      RoleValidator roleValidator) {
    super(userRepository);
    this.repository = repository;
    this.roleValidator = roleValidator;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if (!(targetDomainObject instanceof Message message) || (!(permission instanceof String)
        && !(permission instanceof Permission))) {
      return false;
    }
    User currentUser = getUser(authentication).orElse(null);
    return hasPermission(message, currentUser);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if (!(targetId instanceof Long) || !targetType.equals(Message.class.getName()) || (
        !(permission instanceof String) && !(permission instanceof Permission))) {
      return false;
    }
    Message message = repository.findById((Long) targetId).orElse(null);
    if (message == null) {
      return false;
    }
    User currentUser = getUser(authentication).orElse(null);
    return hasPermission(message, currentUser);
  }

  private boolean hasPermission(Message message, @Nullable User currentUser) {
    if (currentUser == null) {
      return false;
    }
    if (roleValidator.hasRole(ADMIN)) {
      return true;
    }
    if (message.getId() == 0) {
      return true;
    }
    User owner = message.getOwner();
    return owner.getId() == currentUser.getId();
  }
}
