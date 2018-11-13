package ca.qc.ircm.lana.security;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

/**
 * Authorization service.
 */
@Service
@Transactional
public class AuthorizationService {
  @Inject
  private UserRepository userRepository;

  protected AuthorizationService() {
  }

  protected AuthorizationService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  private Subject getSubject() {
    return SecurityUtils.getSubject();
  }

  /**
   * Returns current user or null for anonymous.
   *
   * @return current user or null for anonymous
   */
  public User currentUser() {
    Long userId = (Long) getSubject().getPrincipal();
    if (userId == null) {
      return null;
    }

    return userRepository.findById(userId).orElse(null);
  }

  /**
   * Returns true if current user is anonymous, false otherwise.
   *
   * @return true if current user is anonymous, false otherwise
   */
  public boolean isAnonymous() {
    return getSubject().getPrincipal() == null;
  }

  /**
   * Returns true if current user is authorized to access class, false otherwise.
   *
   * @param type
   *          class
   * @return true if current user is authorized to access class, false otherwise
   */
  public boolean isAuthorized(Class<?> type) {
    RolesAllowed rolesAllowed = AnnotationUtils.findAnnotation(type, RolesAllowed.class);
    if (rolesAllowed != null) {
      Subject subject = getSubject();
      String[] roles = rolesAllowed.value();
      boolean allowed = false;
      for (String role : roles) {
        allowed |= subject.hasRole(role);
      }
      return allowed;
    } else {
      return true;
    }
  }
}
