package ca.qc.ircm.lana.security;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.time.Instant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * {@link DaoAuthenticationProvider} that also validates password using {@link LdapService}.
 */
public class LdapDaoAuthenticationProvider extends DaoAuthenticationProvider {
  private UserRepository userRepository;
  private LdapService ldapService;
  private SecurityConfiguration securityConfiguration;
  private LdapConfiguration ldapConfiguration;

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    User user = getUser(userDetails);
    if (!user.isActive()) {
      throw new DisabledException("User " + userDetails.getUsername() + " is inactive");
    }
    if (accountLocked(user)) {
      throw new LockedException("User " + userDetails.getUsername() + " account is locked");
    }

    try {
      super.additionalAuthenticationChecks(userDetails, authentication);
      resetSignAttemps(user);
    } catch (BadCredentialsException e) {
      // Try LDAP, if available.
      if (authentication.getCredentials() != null && ldapConfiguration.isEnabled()
          && isLdapPasswordValid(userDetails, authentication.getCredentials().toString())) {
        // User is valid.
        resetSignAttemps(user);
      } else {
        incrementSignAttemps(user);
        throw e;
      }
    }
  }

  private User getUser(UserDetails userDetails) {
    String email = userDetails.getUsername();
    return userRepository.findByEmail(email).orElse(null);
  }

  private boolean accountLocked(User user) {
    return user.getSignAttempts() % securityConfiguration.getLockAttemps() == 0
        && user.getLastSignAttempt() != null && user.getLastSignAttempt()
            .plusMillis(securityConfiguration.getLockDuration().toMillis()).isAfter(Instant.now());
  }

  private boolean isLdapPasswordValid(UserDetails userDetails, String password) {
    String email = userDetails.getUsername();
    String username = ldapService.getUsername(email);
    return username != null && ldapService.isPasswordValid(username, password);
  }

  private void resetSignAttemps(User user) {
    user.setSignAttempts(0);
    user.setLastSignAttempt(Instant.now());
    userRepository.save(user);
  }

  private void incrementSignAttemps(User user) {
    user.setSignAttempts(user.getSignAttempts() + 1);
    user.setLastSignAttempt(Instant.now());
    userRepository.save(user);
  }

  public UserRepository getUserRepository() {
    return userRepository;
  }

  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public LdapService getLdapService() {
    return ldapService;
  }

  public void setLdapService(LdapService ldapService) {
    this.ldapService = ldapService;
  }

  public LdapConfiguration getLdapConfiguration() {
    return ldapConfiguration;
  }

  public void setLdapConfiguration(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
  }

  public SecurityConfiguration getSecurityConfiguration() {
    return securityConfiguration;
  }

  public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
    this.securityConfiguration = securityConfiguration;
  }
}
