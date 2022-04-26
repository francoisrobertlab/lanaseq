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
  public void hasRole_Anonymous() throws Throwable {
    assertFalse(roleValidator.hasRole(ADMIN));
  }

  @Test
  @WithMockUser
  public void hasRole_False() throws Throwable {
    assertFalse(roleValidator.hasRole(ADMIN));
  }

  @Test
  @WithMockUser
  public void hasRole_True() throws Throwable {
    assertTrue(roleValidator.hasRole(DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasRole_SwitchedUser() throws Throwable {
    switchToUser("benoit.coulombe@ircm.qc.ca");
    assertTrue(roleValidator.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
  }

  @Test
  @WithAnonymousUser
  public void hasAnyRole_Anonymous() throws Throwable {
    assertFalse(roleValidator.hasAnyRole(ADMIN, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_False() throws Throwable {
    assertFalse(roleValidator.hasAnyRole(ADMIN, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_TrueFirst() throws Throwable {
    assertTrue(roleValidator.hasAnyRole(DEFAULT_ROLE, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_TrueLast() throws Throwable {
    assertTrue(roleValidator.hasAnyRole(ADMIN, DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasAnyRole_SwitchedUser() throws Throwable {
    switchToUser("benoit.coulombe@ircm.qc.ca");
    assertTrue(roleValidator.hasAnyRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
  }

  @Test
  @WithAnonymousUser
  public void hasAllRoles_Anonymous() throws Throwable {
    assertFalse(roleValidator.hasAllRoles(ADMIN, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAllRoles_FalseFirst() throws Throwable {
    assertFalse(roleValidator.hasAllRoles(ADMIN, DEFAULT_ROLE));
  }

  @Test
  @WithMockUser
  public void hasAllRoles_FalseLast() throws Throwable {
    assertFalse(roleValidator.hasAllRoles(DEFAULT_ROLE, MANAGER));
  }

  @Test
  @WithMockUser(roles = { "USER", "ADMIN" })
  public void hasAllRoles_True() throws Throwable {
    assertTrue(roleValidator.hasAllRoles(ADMIN, DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasAllRoles_SwitchedUser() throws Throwable {
    switchToUser("benoit.coulombe@ircm.qc.ca");
    assertTrue(roleValidator.hasAllRoles(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
  }
}
