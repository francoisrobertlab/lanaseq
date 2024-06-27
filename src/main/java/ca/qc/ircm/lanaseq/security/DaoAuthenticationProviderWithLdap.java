package ca.qc.ircm.lanaseq.security;

import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DaoAuthenticationProviderWithLdap extends DaoAuthenticationProvider {
  private static final Logger logger =
      LoggerFactory.getLogger(DaoAuthenticationProviderWithLdap.class);
  private UserRepository userRepository;
  private LdapService ldapService;
  private SecurityConfiguration securityConfiguration;
  private LdapConfiguration ldapConfiguration;

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    String username = authentication.getName();
    logger.debug("user {} tries to authenticate", username);
    User user = getUser(userDetails);
    if (!user.isActive()) {
      logger.debug("user {} account is disabled", username);
      throw new DisabledException("User " + username + " is inactive");
    }
    if (accountLocked(user)) {
      logger.debug("user {} account is locked", username);
      throw new LockedException("User " + username + " account is locked");
    }

    try {
      super.additionalAuthenticationChecks(userDetails, authentication);
      resetSignAttemps(user);
      logger.debug("user {} authenticated successfully", username);
    } catch (BadCredentialsException e) {
      // Try LDAP, if available.
      if (authentication.getCredentials() != null && ldapConfiguration.enabled()
          && isLdapPasswordValid(userDetails, authentication.getCredentials().toString())) {
        // User is valid.
        resetSignAttemps(user);
        logger.debug("user {} authenticated successfully through JDAP", username);
      } else {
        incrementSignAttemps(user);
        logger.debug("user {} supplied wrong password for authentication", username);
        throw e;
      }
    }
  }

  private User getUser(UserDetails userDetails) {
    String email = userDetails.getUsername();
    return userRepository.findByEmail(email).orElse(null);
  }

  private boolean accountLocked(User user) {
    return user.getSignAttempts() > 0
        && user.getSignAttempts() % securityConfiguration.lockAttemps() == 0
        && user.getLastSignAttempt() != null
        && user.getLastSignAttempt()
            .plusSeconds(securityConfiguration.lockDuration().toMillis() / 1000)
            .isAfter(LocalDateTime.now());
  }

  private boolean isLdapPasswordValid(UserDetails userDetails, String password) {
    String email = userDetails.getUsername();
    return ldapService.getUsername(email)
        .map(username -> ldapService.isPasswordValid(username, password)).orElse(false);
  }

  private void resetSignAttemps(User user) {
    user.setSignAttempts(0);
    user.setLastSignAttempt(LocalDateTime.now());
    userRepository.save(user);
  }

  private void incrementSignAttemps(User user) {
    user.setSignAttempts(user.getSignAttempts() + 1);
    user.setLastSignAttempt(LocalDateTime.now());
    if (user.getSignAttempts() >= securityConfiguration.disableSignAttemps()) {
      user.setActive(false);
    }
    userRepository.save(user);
  }

  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void setLdapService(LdapService ldapService) {
    this.ldapService = ldapService;
  }

  public void setLdapConfiguration(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
  }

  public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
    this.securityConfiguration = securityConfiguration;
  }
}
