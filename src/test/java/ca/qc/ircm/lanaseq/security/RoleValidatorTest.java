package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;

/**
 * Tests for {@link RoleValidator}.
 */
@ServiceTestAnnotations
public class RoleValidatorTest {

  private static final String ADMIN = UserRole.ADMIN;
  private static final String MANAGER = UserRole.MANAGER;
  private static final String USER = UserRole.USER;
  private static final String DEFAULT_ROLE = USER;
  @Autowired
  private RoleValidator roleValidator;
  @Autowired
  private UserDetailsService userDetailsService;

  private void switchToUser(String username) {
    Authentication previousAuthentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
    authorities.add(new SwitchUserGrantedAuthority(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR,
        previousAuthentication));
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken(userDetails, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  @WithAnonymousUser
  public void hasRole_Anonymous() {
    assertFalse(roleValidator.hasRole(ADMIN));
  }

  @Test
  @WithMockUser
  public void hasRole_False() {
    assertFalse(roleValidator.hasRole(ADMIN));
  }

  @Test
  @WithMockUser
  public void hasRole_True() {
    assertTrue(roleValidator.hasRole(DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasRole_SwitchedUser() {
    switchToUser("benoit.coulombe@ircm.qc.ca");
    assertTrue(roleValidator.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
  }

  @Test
  @WithAnonymousUser
  public void hasAnyRole_Anonymous() {
    assertFalse(roleValidator.hasAnyRole(ADMIN, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_False() {
    assertFalse(roleValidator.hasAnyRole(ADMIN, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_TrueFirst() {
    assertTrue(roleValidator.hasAnyRole(DEFAULT_ROLE, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_TrueLast() {
    assertTrue(roleValidator.hasAnyRole(ADMIN, DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasAnyRole_SwitchedUser() {
    switchToUser("benoit.coulombe@ircm.qc.ca");
    assertTrue(roleValidator.hasAnyRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
  }

  @Test
  @WithAnonymousUser
  public void hasAllRoles_Anonymous() {
    assertFalse(roleValidator.hasAllRoles(ADMIN, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAllRoles_FalseFirst() {
    assertFalse(roleValidator.hasAllRoles(ADMIN, DEFAULT_ROLE));
  }

  @Test
  @WithMockUser
  public void hasAllRoles_FalseLast() {
    assertFalse(roleValidator.hasAllRoles(DEFAULT_ROLE, MANAGER));
  }

  @Test
  @WithMockUser(roles = {"USER", "ADMIN"})
  public void hasAllRoles_True() {
    assertTrue(roleValidator.hasAllRoles(ADMIN, DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasAllRoles_SwitchedUser() {
    switchToUser("benoit.coulombe@ircm.qc.ca");
    assertTrue(roleValidator.hasAllRoles(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
  }
}
