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

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.PermissionEvaluator;
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
 * Tests for {@link SpringAuthorizationService}.
 */
@ServiceTestAnnotations
public class SpringAuthorizationServiceTest {
  private static final String DEFAULT_ROLE = USER;
  @Autowired
  private SpringAuthorizationService authorizationService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserDetailsService userDetailsService;
  @MockBean
  private RoleValidator roleValidator;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @Mock
  private User user;
  @Mock
  private Dataset dataset;
  @Mock
  private Object object;
  @Captor
  private ArgumentCaptor<List<Permission>> permissionsCaptor;

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

  private Optional<? extends GrantedAuthority>
      findAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
    return authorities.stream().filter(autho -> autho.getAuthority().equals(authority)).findFirst();
  }

  @Test
  @WithAnonymousUser
  public void currentUser_Anonymous() throws Throwable {
    assertTrue(!authorizationService.getCurrentUser().isPresent());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void currentUser() throws Throwable {
    User user = authorizationService.getCurrentUser().orElse(null);
    assertNotNull(authorizationService.getCurrentUser());
    assertEquals((Long) 1L, user.getId());
  }

  @Test
  @WithAnonymousUser
  public void isAnonymous_True() throws Throwable {
    assertTrue(authorizationService.isAnonymous());
  }

  @Test
  @WithMockUser
  public void isAnonymous_False() throws Throwable {
    assertFalse(authorizationService.isAnonymous());
  }

  @Test
  @WithMockUser
  public void hasRole_False() throws Throwable {
    assertFalse(authorizationService.hasRole(DEFAULT_ROLE));
    verify(roleValidator).hasRole(DEFAULT_ROLE);
  }

  @Test
  @WithMockUser
  public void hasRole_True() throws Throwable {
    when(roleValidator.hasRole(any())).thenReturn(true);
    assertTrue(authorizationService.hasRole(DEFAULT_ROLE));
    verify(roleValidator).hasRole(DEFAULT_ROLE);
  }

  @Test
  @WithMockUser
  public void hasAnyRole_False() throws Throwable {
    assertFalse(authorizationService.hasAnyRole(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAnyRole(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithMockUser
  public void hasAnyRole_True() throws Throwable {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authorizationService.hasAnyRole(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAnyRole(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithMockUser
  public void hasAllRoles_False() throws Throwable {
    assertFalse(authorizationService.hasAllRoles(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAllRoles(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithMockUser
  public void hasAllRoles_True() throws Throwable {
    when(roleValidator.hasAllRoles(any())).thenReturn(true);
    assertTrue(authorizationService.hasAllRoles(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAllRoles(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void removeForceChangePasswordRole() throws Throwable {
    userRepository.findById(6L).ifPresent(user -> {
      user.setExpiredPassword(false);
      userRepository.save(user);
    });
    Authentication oldAuthentication = SecurityContextHolder.getContext().getAuthentication();
    assertEquals("christian.poitras@ircm.qc.ca", oldAuthentication.getName());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        oldAuthentication.getCredentials());
    assertTrue(oldAuthentication.isAuthenticated());
    assertEquals(2, oldAuthentication.getAuthorities().size());
    assertTrue(findAuthority(oldAuthentication.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(
        findAuthority(oldAuthentication.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD)
            .isPresent());
    assertTrue(oldAuthentication.getPrincipal() instanceof AuthenticatedUser);
    AuthenticatedUser user = (AuthenticatedUser) oldAuthentication.getPrincipal();
    assertEquals("christian.poitras@ircm.qc.ca", user.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getPassword());
    assertEquals((Long) 6L, user.getId());
    assertEquals(2, user.getAuthorities().size());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(
        findAuthority(user.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());
    when(roleValidator.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)).thenReturn(true);

    authorizationService.reloadAuthorities();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotSame(oldAuthentication, authentication);
    assertEquals("christian.poitras@ircm.qc.ca", authentication.getName());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        authentication.getCredentials());
    assertTrue(authentication.isAuthenticated());
    assertEquals(1, authentication.getAuthorities().size());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.USER).isPresent());
    assertFalse(findAuthority(authentication.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD)
        .isPresent());
    assertTrue(authentication.getPrincipal() instanceof AuthenticatedUser);
    user = (AuthenticatedUser) authentication.getPrincipal();
    assertEquals("christian.poitras@ircm.qc.ca", user.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getPassword());
    assertEquals((Long) 6L, user.getId());
    assertEquals(1, user.getAuthorities().size());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.USER).isPresent());
    assertFalse(
        findAuthority(user.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());
    assertEquals(oldAuthentication.getDetails(), authentication.getDetails());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void removeForceChangePasswordRole_NoForceChangePasswordRole() throws Throwable {
    Authentication oldAuthentication = SecurityContextHolder.getContext().getAuthentication();

    authorizationService.reloadAuthorities();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertSame(oldAuthentication, authentication);
    assertEquals("lanaseq@ircm.qc.ca", authentication.getName());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2,
        authentication.getCredentials());
    assertTrue(authentication.isAuthenticated());
    assertEquals(3, authentication.getAuthorities().size());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.MANAGER).isPresent());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.ADMIN).isPresent());
    assertFalse(findAuthority(authentication.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD)
        .isPresent());
    assertTrue(authentication.getPrincipal() instanceof AuthenticatedUser);
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    assertEquals("lanaseq@ircm.qc.ca", user.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2, user.getPassword());
    assertEquals((Long) 1L, user.getId());
    assertEquals(3, user.getAuthorities().size());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.MANAGER).isPresent());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.ADMIN).isPresent());
    assertFalse(
        findAuthority(user.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());
  }

  @Test
  @WithMockUser
  public void isAuthorized_NoRole() throws Throwable {
    assertTrue(authorizationService.isAuthorized(NoRoleTest.class));
    verify(roleValidator, never()).hasAnyRole(any());
  }

  @Test
  @WithMockUser
  public void isAuthorized_UserRole_True() throws Throwable {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(UserRoleTest.class));
    verify(roleValidator).hasAnyRole(USER);
  }

  @Test
  @WithMockUser(roles = {})
  public void isAuthorized_UserRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(UserRoleTest.class));
    verify(roleValidator).hasAnyRole(USER);
  }

  @Test
  @WithMockUser(authorities = { MANAGER })
  public void isAuthorized_ManagerRole_True() throws Throwable {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(ManagerRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER);
  }

  @Test
  @WithMockUser
  public void isAuthorized_ManagerRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(ManagerRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER);
  }

  @Test
  @WithMockUser(authorities = { ADMIN })
  public void isAuthorized_AdminRole_True() throws Throwable {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(AdminRoleTest.class));
    verify(roleValidator).hasAnyRole(ADMIN);
  }

  @Test
  @WithMockUser
  public void isAuthorized_AdminRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(AdminRoleTest.class));
    verify(roleValidator).hasAnyRole(ADMIN);
  }

  @Test
  @WithMockUser(authorities = { MANAGER })
  public void isAuthorized_ManagerOrAdminRole_True() throws Throwable {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER, ADMIN);
  }

  @Test
  @WithMockUser
  public void isAuthorized_ManagerOrAdminRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER, ADMIN);
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void isAuthorized_SwitchedUser() throws Throwable {
    when(roleValidator.hasAnyRole(USER)).thenReturn(true);
    when(roleValidator.hasAnyRole(MANAGER)).thenReturn(true);
    when(roleValidator.hasAnyRole(MANAGER, ADMIN)).thenReturn(true);
    switchToUser("francois.robert@ircm.qc.ca");
    assertTrue(authorizationService.isAuthorized(UserRoleTest.class));
    assertTrue(authorizationService.isAuthorized(ManagerRoleTest.class));
    assertFalse(authorizationService.isAuthorized(AdminRoleTest.class));
    assertTrue(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_False() throws Throwable {
    Permission permission = Permission.READ;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertFalse(authorizationService.hasPermission(object, permission));
    verify(permissionEvaluator).hasPermission(authentication, object, permission);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_True() throws Throwable {
    Permission permission = Permission.READ;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    assertTrue(authorizationService.hasPermission(object, permission));
    verify(permissionEvaluator).hasPermission(authentication, object, permission);
  }

  /**
   * Class with no {@link RolesAllowed} annotation.
   */
  public static final class NoRoleTest {
  }

  /**
   * Class accessible only by users.
   */
  @RolesAllowed(USER)
  public static final class UserRoleTest {
  }

  /**
   * Class accessible only by managers.
   */
  @RolesAllowed(MANAGER)
  public static final class ManagerRoleTest {
  }

  /**
   * Class accessible only by administrators.
   */
  @RolesAllowed(ADMIN)
  public static final class AdminRoleTest {
  }

  /**
   * Class accessible only by managers or administrators.
   */
  @RolesAllowed({ MANAGER, ADMIN })
  public static final class ManagerOrAdminRoleTest {
  }
}
