/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lana.security;

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for authentifications.
 */
public class AuthenticationService {
  private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
  @Inject
  private UserRepository userRepository;
  @Inject
  private ShiroLdapService shiroLdapService;
  @Inject
  private SecurityConfiguration securityConfiguration;
  @Inject
  private LdapConfiguration ldapConfiguration;
  /**
   * Used to generate salt for passwords.
   */
  private SecureRandom random = new SecureRandom();

  protected AuthenticationService() {
  }

  protected AuthenticationService(UserRepository userRepository,
      ShiroLdapService shiroLdapService, SecurityConfiguration securityConfiguration,
      LdapConfiguration ldapConfiguration) {
    this.userRepository = userRepository;
    this.shiroLdapService = shiroLdapService;
    this.securityConfiguration = securityConfiguration;
    this.ldapConfiguration = ldapConfiguration;
  }

  private Subject getSubject() {
    return SecurityUtils.getSubject();
  }

  private Optional<User> getUser(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    return userRepository.findById(id);
  }

  private Optional<User> getUser(String email) {
    if (email == null) {
      return Optional.empty();
    }

    return userRepository.findByEmail(email);
  }

  /**
   * Sign user.
   *
   * @param email
   *          user's email
   * @param password
   *          user's password
   * @param rememberMe
   *          true if user is to be remembered between sessions
   * @throws AuthenticationException
   *           user cannot be authenticated
   */
  public void sign(String email, String password, boolean rememberMe)
      throws AuthenticationException {
    if (email == null || password == null) {
      throw new AuthenticationException("username and password cannot be null");
    }

    UsernamePasswordToken token = new UsernamePasswordToken(email, password);
    token.setRememberMe(rememberMe);
    getSubject().login(token);
  }

  /**
   * Signout user.
   */
  public void signout() {
    getSubject().logout();
  }

  /**
   * Run application as another user.
   *
   * @param user
   *          other user
   */
  public void runAs(User user) {
    if (user == null) {
      throw new NullPointerException("user cannot be null");
    }
    getSubject().checkRole(ADMIN);
    user = getUser(user.getId()).orElse(null);
    if (user.isAdmin()) {
      throw new UnauthorizedException("Cannot run as an admin user");
    }

    getSubject().runAs(new SimplePrincipalCollection(user.getId(), realmName()));
  }

  /**
   * Stop running application as another user.
   *
   * @return id of assumed identity, if any
   */
  public Long stopRunAs() {
    PrincipalCollection principalCollection = getSubject().releaseRunAs();
    return (Long) principalCollection.getPrimaryPrincipal();
  }

  /**
   * Selects authentication information based on user's name and password.
   *
   * @param token
   *          authentication token
   * @return authentication information
   * @throws UnknownAccountException
   *           user is unknown
   * @throws InvalidAccountException
   *           user has not been validated yet
   * @throws DisabledAccountException
   *           user is disabled
   * @throws IncorrectCredentialsException
   *           user's password is invalid
   */
  public AuthenticationInfo getAuthenticationInfo(UsernamePasswordToken token) {
    if (token == null || token.getPrincipal() == null || token.getCredentials() == null) {
      throw new UnknownAccountException("No account found for user []");
    }
    if (ldapConfiguration.isEnabled()) {
      try {
        return getShiroLdapAuthenticationInfo(token);
      } catch (AuthenticationException ldapAuthenticationException) {
        return getLocalAuthenticationInfo(token);
      }
    } else {
      return getLocalAuthenticationInfo(token);
    }
  }

  private AuthenticationInfo getShiroLdapAuthenticationInfo(UsernamePasswordToken token) {
    String username = token.getUsername();
    String email = shiroLdapService.getEmail(username, String.valueOf(token.getPassword()));
    User user = getUser(email).orElse(null);
    if (user == null) {
      throw new UnknownAccountException("No account found for user [" + username + "]");
    }
    user.setSignAttempts(0);
    user.setLastSignAttempt(Instant.now());
    userRepository.save(user);
    return new SimpleAuthenticationInfo(user.getId(), user.getHashedPassword(), null, realmName());
  }

  private AuthenticationInfo getLocalAuthenticationInfo(UsernamePasswordToken token) {
    String username = token.getUsername();
    User user = getUser(username).orElse(null);
    if (user == null) {
      throw new UnknownAccountException("No account found for user [" + username + "]");
    }
    if (!user.isActive()) {
      throw new DisabledAccountException("Account for user [" + username + "] is disabled");
    }
    if (!canAttempt(user)) {
      throw new ExcessiveAttemptsException("To many attemps for user [" + username + "]");
    }
    try {
      if (!credentialMatches(token, user)) {
        user.setSignAttempts(user.getSignAttempts() + 1);
        if (user.getSignAttempts() >= securityConfiguration.getDisableSignAttemps()) {
          user.setActive(false);
        }
        throw new IncorrectCredentialsException("Submitted credentials for token [" + token
            + "] did not match the expected credentials.");
      }

      user.setSignAttempts(0);
      return new SimpleAuthenticationInfo(user.getId(), user.getHashedPassword(), null,
          realmName());
    } finally {
      user.setLastSignAttempt(Instant.now());
      userRepository.save(user);
    }
  }

  private boolean canAttempt(User user) {
    if (user.getSignAttempts() > 0
        && user.getSignAttempts() % securityConfiguration.getMaximumSignAttemps() == 0) {
      if (Instant.now().isBefore(user.getLastSignAttempt()
          .plusMillis(securityConfiguration.getMaximumSignAttempsDelay()))) {
        return false;
      }
    }
    return true;
  }

  private boolean credentialMatches(UsernamePasswordToken token, User user) {
    return BCrypt.checkpw(String.valueOf(token.getPassword()), user.getHashedPassword());
  }

  /**
   * Returns user's authorization information.
   *
   * @param principals
   *          user's principals
   * @return user's authorization information
   */
  public AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
    if (principals == null) {
      return new SimpleAuthorizationInfo();
    }

    Long userId = (Long) principals.getPrimaryPrincipal();
    User user = getUser(userId)
        .orElseThrow(() -> new IllegalStateException("User " + userId + " does not exists"));
    SimpleAuthorizationInfo authorization = new SimpleAuthorizationInfo();
    if (!user.isActive()) {
      authorization.setRoles(new HashSet<>());
      authorization.setObjectPermissions(new HashSet<>());
      return authorization;
    } else {
      authorization.setRoles(selectRoles(user));
      authorization.setObjectPermissions(selectPermissions(user));
      return authorization;
    }
  }

  private Set<String> selectRoles(User user) {
    Set<String> roles = new HashSet<>();
    roles.add(USER);
    if (user.isManager()) {
      roles.add(MANAGER);
    }
    if (user.isAdmin()) {
      roles.add(ADMIN);
    }

    Set<String> lowerUpperRoles = new HashSet<>();
    for (String role : roles) {
      lowerUpperRoles.add(role.toLowerCase());
      lowerUpperRoles.add(role.toUpperCase());
    }
    logger.trace("User {} has roles {}", user, lowerUpperRoles);
    return lowerUpperRoles;
  }

  private Set<Permission> selectPermissions(User user) {
    Set<Permission> permissions = new HashSet<>();

    permissions.add(new WildcardPermission("user:*:" + user.getId()));
    if (user.getLaboratory() != null) {
      permissions.add(new WildcardPermission("laboratory:read:" + user.getLaboratory().getId()));
      if (user.isManager()) {
        permissions
            .add(new WildcardPermission("laboratory:manager:" + user.getLaboratory().getId()));
      }
    }
    if (user.isAdmin()) {
      permissions.add(new WildcardPermission("*"));
    }
    logger.trace("User {} has permissions {}", user, permissions);
    return permissions;
  }

  private String realmName() {
    return securityConfiguration.getRealmName();
  }

  /**
   * Hashes password. This method should be used before inserting password into database.
   *
   * @param password
   *          password as entered by user
   * @return hashed password
   */
  public String encode(String password) {
    if (password == null) {
      return null;
    }
    String salt = BCrypt.gensalt(securityConfiguration.getPasswordStrength(), random);
    return BCrypt.hashpw(password, salt);
  }
}
