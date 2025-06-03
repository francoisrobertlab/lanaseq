package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import ca.qc.ircm.lanaseq.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Switch user service.
 *
 * <p>Most code was copied from
 * org.springframework.security.web.authentication.switchuser.SwitchUserFilter.</p>
 */
@Service
public class SwitchUserService {

  public static final String ROLE_PREVIOUS_ADMINISTRATOR = "ROLE_PREVIOUS_ADMINISTRATOR";
  private static final Logger logger = LoggerFactory.getLogger(SwitchUserService.class);
  private final String switchAuthorityRole = ROLE_PREVIOUS_ADMINISTRATOR;
  private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
  private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();
  private final UserDetailsService userDetailsService;
  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public SwitchUserService(UserDetailsService userDetailsService,
      ApplicationEventPublisher eventPublisher) {
    this.userDetailsService = userDetailsService;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Switches to switchTo user.
   *
   * @param switchTo user to switch to
   * @param request  HTTP request
   */
  @PreAuthorize("hasAuthority('" + ADMIN + "')")
  public void switchUser(User switchTo, HttpServletRequest request) {
    Objects.requireNonNull(switchTo, "switchTo parameter cannot be null");
    Objects.requireNonNull(request, "request parameter cannot be null");
    Authentication targetUser = attemptSwitchUser(switchTo, request);
    // update the current context to the new target user
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(targetUser);
    SecurityContextHolder.setContext(context);
    HttpSession session = request.getSession(true);
    session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
    logger.debug("set SecurityContextHolder to {}", targetUser);
  }

  private Authentication attemptSwitchUser(User switchTo, HttpServletRequest request) {
    String username = switchTo.getEmail();
    logger.debug("attempting to switch to user {}", username);
    UserDetails targetUser = userDetailsService.loadUserByUsername(username);
    userDetailsChecker.check(targetUser);
    // OK, create the switch user token
    UsernamePasswordAuthenticationToken targetUserRequest = createSwitchUserToken(targetUser);
    // publish event
    eventPublisher.publishEvent(
        new AuthenticationSwitchUserEvent(SecurityContextHolder.getContext().getAuthentication(),
            targetUser));
    // set details
    targetUserRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    return targetUserRequest;
  }

  private UsernamePasswordAuthenticationToken createSwitchUserToken(UserDetails targetUser) {
    UsernamePasswordAuthenticationToken targetUserRequest;
    // grant an additional authority that contains the original Authentication object
    // which will be used to 'exit' from the current switched user.
    Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
    GrantedAuthority switchAuthority = new SwitchUserGrantedAuthority(switchAuthorityRole,
        currentAuthentication);
    // get the original authorities
    Collection<? extends GrantedAuthority> orig = targetUser.getAuthorities();
    // add the new switch user authority
    List<GrantedAuthority> newAuths = new ArrayList<>(orig);
    newAuths.add(switchAuthority);
    // create the new authentication token
    targetUserRequest = UsernamePasswordAuthenticationToken.authenticated(targetUser,
        targetUser.getPassword(), newAuths);
    return targetUserRequest;
  }

  /**
   * Exits switch user.
   *
   * @param request HTTP request
   */
  public void exitSwitchUser(HttpServletRequest request) {
    // get the original authentication object (if exists)
    Authentication originalUser = attemptExitUser();
    // update the current context back to the original user
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(originalUser);
    SecurityContextHolder.setContext(context);
    HttpSession session = request.getSession(true);
    session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
    logger.debug("Set SecurityContextHolder to {}", originalUser);
  }

  private Authentication attemptExitUser() throws AuthenticationCredentialsNotFoundException {
    // need to check to see if the current user has a SwitchUserGrantedAuthority
    Authentication current = SecurityContextHolder.getContext().getAuthentication();
    if (current == null) {
      throw new AuthenticationCredentialsNotFoundException(
          "No current user associated with this request");
    }
    // check to see if the current user did actual switch to another user
    // if so, get the original source user so we can switch back
    Authentication original = getSourceAuthentication(current);
    if (original == null) {
      logger.debug("Failed to find original user");
      throw new AuthenticationCredentialsNotFoundException("Failed to find original user");
    }
    // get the source user details
    UserDetails originalUser = null;
    Object obj = original.getPrincipal();
    if (obj instanceof UserDetails) {
      originalUser = (UserDetails) obj;
    }
    // publish event
    eventPublisher.publishEvent(new AuthenticationSwitchUserEvent(current, originalUser));
    return original;
  }

  @Nullable
  private Authentication getSourceAuthentication(Authentication current) {
    Authentication original = null;
    // iterate over granted authorities and find the 'switch user' authority
    Collection<? extends GrantedAuthority> authorities = current.getAuthorities();
    for (GrantedAuthority auth : authorities) {
      // check for switch user type of authority
      if (auth instanceof SwitchUserGrantedAuthority) {
        original = ((SwitchUserGrantedAuthority) auth).getSource();
        logger.debug("Found original switch user granted authority {}", original);
      }
    }
    return original;
  }

  public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
    Objects.requireNonNull(userDetailsChecker, "userDetailsChecker parameter cannot be null");
    this.userDetailsChecker = userDetailsChecker;
  }

  public void setAuthenticationDetailsSource(
      AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
    Objects.requireNonNull(authenticationDetailsSource,
        "authenticationDetailsSource parameter cannot be null");
    this.authenticationDetailsSource = authenticationDetailsSource;
  }
}
