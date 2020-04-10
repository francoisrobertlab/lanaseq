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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.experiment.Experiment;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SpringAuthorizationService;
import ca.qc.ircm.lanaseq.security.UserAuthority;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class SpringAuthorizationServiceTest {
  private static final String DEFAULT_ROLE = USER;
  private SpringAuthorizationService authorizationService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserDetailsService userDetailsService;
  @Mock
  private PermissionEvaluator permissionEvaluator;
  @Mock
  private Laboratory laboratory;
  @Mock
  private User user;
  @Mock
  private Experiment experiment;
  @Mock
  private Object object;
  @Mock
  private Permission permission;
  @Captor
  private ArgumentCaptor<List<Permission>> permissionsCaptor;
  @Captor
  private ArgumentCaptor<List<Sid>> sidsCaptor;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    authorizationService =
        new SpringAuthorizationService(userRepository, userDetailsService, permissionEvaluator);
  }

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
    assertNull(authorizationService.currentUser());
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void currentUser() throws Throwable {
    User user = authorizationService.currentUser();
    assertNotNull(authorizationService.currentUser());
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
    assertFalse(authorizationService.hasRole(ADMIN));
  }

  @Test
  @WithMockUser
  public void hasRole_True() throws Throwable {
    assertTrue(authorizationService.hasRole(DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasRole_SwitchedUser() throws Throwable {
    switchToUser("francois.robert@ircm.qc.ca");
    assertTrue(authorizationService.hasRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_False() throws Throwable {
    assertFalse(authorizationService.hasAnyRole(ADMIN, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_TrueFirst() throws Throwable {
    assertTrue(authorizationService.hasAnyRole(DEFAULT_ROLE, MANAGER));
  }

  @Test
  @WithMockUser
  public void hasAnyRole_TrueLast() throws Throwable {
    assertTrue(authorizationService.hasAnyRole(ADMIN, DEFAULT_ROLE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasAnyRole_SwitchedUser() throws Throwable {
    switchToUser("francois.robert@ircm.qc.ca");
    assertTrue(authorizationService.hasAnyRole(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR));
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
    assertEquals(3, oldAuthentication.getAuthorities().size());
    assertTrue(findAuthority(oldAuthentication.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(findAuthority(oldAuthentication.getAuthorities(),
        UserAuthority.laboratoryMember(new Laboratory(3L))).isPresent());
    assertTrue(
        findAuthority(oldAuthentication.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD)
            .isPresent());
    assertTrue(oldAuthentication.getPrincipal() instanceof AuthenticatedUser);
    AuthenticatedUser user = (AuthenticatedUser) oldAuthentication.getPrincipal();
    assertEquals("christian.poitras@ircm.qc.ca", user.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getPassword());
    assertEquals((Long) 6L, user.getId());
    assertEquals(3, user.getAuthorities().size());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(
        findAuthority(user.getAuthorities(), UserAuthority.laboratoryMember(new Laboratory(3L)))
            .isPresent());
    assertTrue(
        findAuthority(user.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());

    authorizationService.reloadAuthorities();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotSame(oldAuthentication, authentication);
    assertEquals("christian.poitras@ircm.qc.ca", authentication.getName());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        authentication.getCredentials());
    assertTrue(authentication.isAuthenticated());
    assertEquals(2, authentication.getAuthorities().size());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(findAuthority(authentication.getAuthorities(),
        UserAuthority.laboratoryMember(new Laboratory(3L))).isPresent());
    assertFalse(findAuthority(authentication.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD)
        .isPresent());
    assertTrue(authentication.getPrincipal() instanceof AuthenticatedUser);
    user = (AuthenticatedUser) authentication.getPrincipal();
    assertEquals("christian.poitras@ircm.qc.ca", user.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getPassword());
    assertEquals((Long) 6L, user.getId());
    assertEquals(2, user.getAuthorities().size());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(
        findAuthority(user.getAuthorities(), UserAuthority.laboratoryMember(new Laboratory(3L)))
            .isPresent());
    assertFalse(
        findAuthority(user.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());
    assertEquals(oldAuthentication.getDetails(), authentication.getDetails());
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void removeForceChangePasswordRole_NoForceChangePasswordRole() throws Throwable {
    Authentication oldAuthentication = SecurityContextHolder.getContext().getAuthentication();

    authorizationService.reloadAuthorities();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertSame(oldAuthentication, authentication);
    assertEquals("lana@ircm.qc.ca", authentication.getName());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2,
        authentication.getCredentials());
    assertTrue(authentication.isAuthenticated());
    assertEquals(4, authentication.getAuthorities().size());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.MANAGER).isPresent());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.ADMIN).isPresent());
    assertTrue(findAuthority(authentication.getAuthorities(),
        UserAuthority.laboratoryMember(new Laboratory(1L))).isPresent());
    assertFalse(findAuthority(authentication.getAuthorities(), UserAuthority.FORCE_CHANGE_PASSWORD)
        .isPresent());
    assertTrue(authentication.getPrincipal() instanceof AuthenticatedUser);
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    assertEquals("lana@ircm.qc.ca", user.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2, user.getPassword());
    assertEquals((Long) 1L, user.getId());
    assertEquals(4, user.getAuthorities().size());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.MANAGER).isPresent());
    assertTrue(findAuthority(user.getAuthorities(), UserRole.ADMIN).isPresent());
    assertTrue(
        findAuthority(user.getAuthorities(), UserAuthority.laboratoryMember(new Laboratory(1L)))
            .isPresent());
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
  }

  @Test
  @WithMockUser
  public void isAuthorized_UserRole_True() throws Throwable {
    assertTrue(authorizationService.isAuthorized(UserRoleTest.class));
  }

  @Test
  @WithMockUser(roles = {})
  public void isAuthorized_UserRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(UserRoleTest.class));
  }

  @Test
  @WithMockUser(authorities = { MANAGER })
  public void isAuthorized_ManagerRole_True() throws Throwable {
    assertTrue(authorizationService.isAuthorized(ManagerRoleTest.class));
  }

  @Test
  @WithMockUser
  public void isAuthorized_ManagerRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(ManagerRoleTest.class));
  }

  @Test
  @WithMockUser(authorities = { ADMIN })
  public void isAuthorized_AdminRole_True() throws Throwable {
    assertTrue(authorizationService.isAuthorized(AdminRoleTest.class));
  }

  @Test
  @WithMockUser
  public void isAuthorized_AdminRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(AdminRoleTest.class));
  }

  @Test
  @WithMockUser(authorities = { MANAGER })
  public void isAuthorized_ManagerOrAdminRole_Manager() throws Throwable {
    assertTrue(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
  }

  @Test
  @WithMockUser(authorities = { ADMIN })
  public void isAuthorized_ManagerOrAdminRole_Admin() throws Throwable {
    assertTrue(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
  }

  @Test
  @WithMockUser
  public void isAuthorized_ManagerOrAdminRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void isAuthorized_SwitchedUser() throws Throwable {
    switchToUser("francois.robert@ircm.qc.ca");
    assertTrue(authorizationService.isAuthorized(UserRoleTest.class));
    assertTrue(authorizationService.isAuthorized(ManagerRoleTest.class));
    assertFalse(authorizationService.isAuthorized(AdminRoleTest.class));
    assertTrue(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_False() throws Throwable {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertFalse(authorizationService.hasPermission(object, permission));
    verify(permissionEvaluator).hasPermission(authentication, object, permission);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_True() throws Throwable {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    assertTrue(authorizationService.hasPermission(object, permission));
    verify(permissionEvaluator).hasPermission(authentication, object, permission);
  }

  public static final class NoRoleTest {
  }

  @RolesAllowed(USER)
  public static final class UserRoleTest {
  }

  @RolesAllowed(MANAGER)
  public static final class ManagerRoleTest {
  }

  @RolesAllowed(ADMIN)
  public static final class AdminRoleTest {
  }

  @RolesAllowed({ MANAGER, ADMIN })
  public static final class ManagerOrAdminRoleTest {
  }
}
