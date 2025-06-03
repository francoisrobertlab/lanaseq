package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import jakarta.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link AuthenticatedUser}.
 */
@ServiceTestAnnotations
public class AuthenticatedUserTest {

  private static final String DEFAULT_ROLE = USER;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  @Autowired
  private UserRepository repository;
  @Autowired
  private UserDetailsService userDetailsService;
  @MockitoBean
  private RoleValidator roleValidator;
  @MockitoBean
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
    TestingAuthenticationToken authentication = new TestingAuthenticationToken(userDetails, null,
        authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private Optional<? extends GrantedAuthority> findAuthority(
      Collection<? extends GrantedAuthority> authorities, String authority) {
    return authorities.stream().filter(autho -> autho.getAuthority().equals(authority)).findFirst();
  }

  @Test
  @WithAnonymousUser
  public void getUser_Anonymous() {
    assertFalse(authenticatedUser.getUser().isPresent());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void getUser() {
    User user = authenticatedUser.getUser().orElseThrow();
    assertNotNull(authenticatedUser.getUser());
    assertEquals((Long) 1L, user.getId());
  }

  @Test
  @WithMockUser("lanaseq@ircm.qc.ca")
  public void getUser_NoId() {
    User user = authenticatedUser.getUser().orElseThrow();
    assertNotNull(user);
    assertEquals((Long) 1L, user.getId());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void getUser_UsernameMissmatchId() {
    User user = repository.findById(1L).orElseThrow();
    user.setEmail("other_email@ircm.qc.ca");
    repository.save(user);
    user = authenticatedUser.getUser().orElseThrow();
    assertEquals((Long) 1L, user.getId());
  }

  @Test
  @WithAnonymousUser
  public void isAnonymous_True() {
    assertTrue(authenticatedUser.isAnonymous());
  }

  @Test
  @WithMockUser
  public void isAnonymous_False() {
    assertFalse(authenticatedUser.isAnonymous());
  }

  @Test
  @WithMockUser
  public void hasRole_False() {
    assertFalse(authenticatedUser.hasRole(DEFAULT_ROLE));
    verify(roleValidator).hasRole(DEFAULT_ROLE);
  }

  @Test
  @WithMockUser
  public void hasRole_True() {
    when(roleValidator.hasRole(any())).thenReturn(true);
    assertTrue(authenticatedUser.hasRole(DEFAULT_ROLE));
    verify(roleValidator).hasRole(DEFAULT_ROLE);
  }

  @Test
  @WithMockUser
  public void hasAnyRole_False() {
    assertFalse(authenticatedUser.hasAnyRole(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAnyRole(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithMockUser
  public void hasAnyRole_True() {
    when(roleValidator.hasAnyRole(any(String[].class))).thenReturn(true);
    assertTrue(authenticatedUser.hasAnyRole(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAnyRole(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithMockUser
  public void hasAllRoles_False() {
    assertFalse(authenticatedUser.hasAllRoles(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAllRoles(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithMockUser
  public void hasAllRoles_True() {
    when(roleValidator.hasAllRoles(any(String[].class))).thenReturn(true);
    assertTrue(authenticatedUser.hasAllRoles(DEFAULT_ROLE, MANAGER));
    verify(roleValidator).hasAllRoles(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void removeForceChangePasswordRole() {
    repository.findById(6L).ifPresent(user -> {
      user.setExpiredPassword(false);
      repository.save(user);
    });
    Authentication oldAuthentication = SecurityContextHolder.getContext().getAuthentication();
    assertEquals("christian.poitras@ircm.qc.ca", oldAuthentication.getName());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        oldAuthentication.getCredentials());
    assertTrue(oldAuthentication.isAuthenticated());
    assertEquals(2, oldAuthentication.getAuthorities().size());
    assertTrue(findAuthority(oldAuthentication.getAuthorities(), UserRole.USER).isPresent());
    assertTrue(findAuthority(oldAuthentication.getAuthorities(),
        UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertInstanceOf(UserDetailsWithId.class, oldAuthentication.getPrincipal());
    UserDetailsWithId user = (UserDetailsWithId) oldAuthentication.getPrincipal();
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

    authenticatedUser.reloadAuthorities();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotSame(oldAuthentication, authentication);
    assertEquals("christian.poitras@ircm.qc.ca", authentication.getName());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        authentication.getCredentials());
    assertTrue(authentication.isAuthenticated());
    assertEquals(1, authentication.getAuthorities().size());
    assertTrue(findAuthority(authentication.getAuthorities(), UserRole.USER).isPresent());
    assertFalse(findAuthority(authentication.getAuthorities(),
        UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertInstanceOf(UserDetailsWithId.class, authentication.getPrincipal());
    user = (UserDetailsWithId) authentication.getPrincipal();
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
  public void removeForceChangePasswordRole_NoForceChangePasswordRole() {
    Authentication oldAuthentication = SecurityContextHolder.getContext().getAuthentication();

    authenticatedUser.reloadAuthorities();

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
    assertFalse(findAuthority(authentication.getAuthorities(),
        UserAuthority.FORCE_CHANGE_PASSWORD).isPresent());
    assertInstanceOf(UserDetailsWithId.class, authentication.getPrincipal());
    UserDetailsWithId user = (UserDetailsWithId) authentication.getPrincipal();
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
  public void isAuthorized_NoRole() {
    assertTrue(authenticatedUser.isAuthorized(NoRoleTest.class));
    verify(roleValidator, never()).hasAnyRole(any());
  }

  @Test
  @WithMockUser
  public void isAuthorized_UserRole_True() {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authenticatedUser.isAuthorized(UserRoleTest.class));
    verify(roleValidator).hasAnyRole(USER);
  }

  @Test
  @WithMockUser(roles = {})
  public void isAuthorized_UserRole_False() {
    assertFalse(authenticatedUser.isAuthorized(UserRoleTest.class));
    verify(roleValidator).hasAnyRole(USER);
  }

  @Test
  @WithMockUser(authorities = {MANAGER})
  public void isAuthorized_ManagerRole_True() {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authenticatedUser.isAuthorized(ManagerRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER);
  }

  @Test
  @WithMockUser
  public void isAuthorized_ManagerRole_False() {
    assertFalse(authenticatedUser.isAuthorized(ManagerRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER);
  }

  @Test
  @WithMockUser(authorities = {ADMIN})
  public void isAuthorized_AdminRole_True() {
    when(roleValidator.hasAnyRole(any())).thenReturn(true);
    assertTrue(authenticatedUser.isAuthorized(AdminRoleTest.class));
    verify(roleValidator).hasAnyRole(ADMIN);
  }

  @Test
  @WithMockUser
  public void isAuthorized_AdminRole_False() {
    assertFalse(authenticatedUser.isAuthorized(AdminRoleTest.class));
    verify(roleValidator).hasAnyRole(ADMIN);
  }

  @Test
  @WithMockUser(authorities = {MANAGER})
  public void isAuthorized_ManagerOrAdminRole_True() {
    when(roleValidator.hasAnyRole(any(String[].class))).thenReturn(true);
    assertTrue(authenticatedUser.isAuthorized(ManagerOrAdminRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER, ADMIN);
  }

  @Test
  @WithMockUser
  public void isAuthorized_ManagerOrAdminRole_False() {
    assertFalse(authenticatedUser.isAuthorized(ManagerOrAdminRoleTest.class));
    verify(roleValidator).hasAnyRole(MANAGER, ADMIN);
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void isAuthorized_SwitchedUser() {
    when(roleValidator.hasAnyRole(USER)).thenReturn(true);
    when(roleValidator.hasAnyRole(MANAGER)).thenReturn(true);
    when(roleValidator.hasAnyRole(MANAGER, ADMIN)).thenReturn(true);
    switchToUser("francois.robert@ircm.qc.ca");
    assertTrue(authenticatedUser.isAuthorized(UserRoleTest.class));
    assertTrue(authenticatedUser.isAuthorized(ManagerRoleTest.class));
    assertFalse(authenticatedUser.isAuthorized(AdminRoleTest.class));
    assertTrue(authenticatedUser.isAuthorized(ManagerOrAdminRoleTest.class));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_False() {
    Permission permission = Permission.READ;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertFalse(authenticatedUser.hasPermission(object, permission));
    verify(permissionEvaluator).hasPermission(authentication, object, permission);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_True() {
    Permission permission = Permission.READ;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    assertTrue(authenticatedUser.hasPermission(object, permission));
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
  @RolesAllowed({MANAGER, ADMIN})
  public static final class ManagerOrAdminRoleTest {

  }
}
