package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserAuthority.FORCE_CHANGE_PASSWORD;

import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Authenticated user.
 */
@Service
public class AuthenticatedUser {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticatedUser.class);
  private final UserRepository repository;
  private final UserDetailsService userDetailsService;
  private final RoleValidator roleValidator;
  private final PermissionEvaluator permissionEvaluator;

  @Autowired
  protected AuthenticatedUser(UserRepository repository, UserDetailsService userDetailsService,
      RoleValidator roleValidator, PermissionEvaluator permissionEvaluator) {
    this.repository = repository;
    this.userDetailsService = userDetailsService;
    this.roleValidator = roleValidator;
    this.permissionEvaluator = permissionEvaluator;
  }

  private Optional<Authentication> getAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
  }

  private Optional<UserDetails> getUserDetails() {
    return getAuthentication().filter(au -> au.getPrincipal() instanceof UserDetails)
        .map(au -> ((UserDetails) au.getPrincipal()));
  }

  /**
   * Returns authenticated user or empty for anonymous.
   *
   * @return authenticated user or empty for anonymous
   */
  public Optional<User> getUser() {
    Optional<UserDetails> optionalUserDetails = getUserDetails();
    if (optionalUserDetails.isPresent()) {
      UserDetails userDetails = optionalUserDetails.get();
      if (userDetails instanceof UserDetailsWithId) {
        return repository.findById(((UserDetailsWithId) userDetails).getId());
      } else {
        logger.warn("UserDetails {} is not an instanceof {}", userDetails,
            UserDetailsWithId.class.getSimpleName());
        return repository.findByEmail(userDetails.getUsername());
      }
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns true if authenticated user is anonymous, false otherwise.
   *
   * @return true if authenticated user is anonymous, false otherwise
   */
  public boolean isAnonymous() {
    return getUserDetails().isEmpty();
  }

  /**
   * Returns true if authenticated user has specified role, false otherwise.
   *
   * @param role role
   * @return true if authenticated user has specified role, false otherwise
   */
  public boolean hasRole(String role) {
    return roleValidator.hasRole(role);
  }

  /**
   * Returns true if authenticated user has any of the specified roles, false otherwise.
   *
   * @param roles roles
   * @return true if authenticated user has any of the specified roles, false otherwise
   */
  public boolean hasAnyRole(String... roles) {
    return roleValidator.hasAnyRole(roles);
  }

  /**
   * Returns true if authenticated user has all of the specified roles, false otherwise.
   *
   * @param roles roles
   * @return true if authenticated user has all of the specified roles, false otherwise
   */
  public boolean hasAllRoles(String... roles) {
    return roleValidator.hasAllRoles(roles);
  }

  /**
   * Reload authenticated user's authorities.
   */
  public void reloadAuthorities() {
    if (roleValidator.hasRole(FORCE_CHANGE_PASSWORD)) {
      getAuthentication().ifPresent(oldAuthentication -> {
        logger.debug("reload authorities from user {}", oldAuthentication.getName());
        UserDetails userDetails = userDetailsService.loadUserByUsername(
            oldAuthentication.getName());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, oldAuthentication.getCredentials(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      });
    }
  }

  /**
   * Returns true if authenticated user is authorized to access class, false otherwise.
   *
   * @param type class
   * @return true if authenticated user is authorized to access class, false otherwise
   */
  public boolean isAuthorized(Class<?> type) {
    RolesAllowed rolesAllowed = AnnotationUtils.findAnnotation(type, RolesAllowed.class);
    if (rolesAllowed != null) {
      String[] roles = rolesAllowed.value();
      return roleValidator.hasAnyRole(roles);
    } else {
      return true;
    }
  }

  /**
   * Returns true if authenticated user has permission on object, false otherwise.
   *
   * @param object     object
   * @param permission permission
   * @return true if authenticated user has permission on object, false otherwise
   */
  public boolean hasPermission(Object object, Permission permission) {
    return getAuthentication().map(au -> permissionEvaluator.hasPermission(au, object, permission))
        .orElse(false);
  }
}
