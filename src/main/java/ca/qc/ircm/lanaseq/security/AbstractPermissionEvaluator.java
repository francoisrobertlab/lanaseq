package ca.qc.ircm.lanaseq.security;

import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Implements common methods to use for {@link PermissionEvaluator} implementations.
 */
public abstract class AbstractPermissionEvaluator implements PermissionEvaluator {
  private UserRepository userRepository;

  protected AbstractPermissionEvaluator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  protected Optional<UserDetails> getUserDetails(Authentication authentication) {
    return Optional.ofNullable(authentication)
        .filter(au -> au.getPrincipal() instanceof UserDetails)
        .map(au -> (UserDetails) au.getPrincipal());
  }

  protected Optional<User> getUser(Authentication authentication) {
    return getUserDetails(authentication).filter(ud -> ud instanceof UserDetailsWithId)
        .map(ud -> (UserDetailsWithId) ud).map(au -> au.getId())
        .map(id -> userRepository.findById(id).orElse(null));
  }

  protected Permission resolvePermission(Object permission) {
    if (permission instanceof Permission) {
      return (Permission) permission;
    }
    try {
      return Enum.valueOf(Permission.class, Objects.toString(permission).toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Permission " + permission + " does not exists in " + Permission.class.getSimpleName());
    }
  }
}
