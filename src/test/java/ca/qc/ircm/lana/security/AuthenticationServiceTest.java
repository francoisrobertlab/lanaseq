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

/*
; * Copyright (c) 2016 Institut de recherches cliniques de Montreal (IRCM)
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserRole;
import java.time.Instant;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class AuthenticationServiceTest {
  private AuthenticationService authenticationService;
  @Inject
  private UserRepository userRepository;
  @Mock
  private ShiroLdapService shiroLdapService;
  @Mock
  private SecurityConfiguration securityConfiguration;
  @Mock
  private LdapConfiguration ldapConfiguration;
  @Captor
  private ArgumentCaptor<PrincipalCollection> principalCollectionCaptor;
  @Captor
  private ArgumentCaptor<AuthenticationToken> tokenCaptor;
  private String realmName = "proviewRealm";
  private Subject subject;
  private int passwordStrength = 10;
  private int maximumSignAttemps = 3;
  private long maximumSignAttempsDelay = 300000;
  private int disableSignAttemps = 15;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    authenticationService = new AuthenticationService(userRepository, shiroLdapService,
        securityConfiguration, ldapConfiguration);
    when(securityConfiguration.getPasswordStrength()).thenReturn(passwordStrength);
    when(securityConfiguration.getRealmName()).thenReturn(realmName);
    when(securityConfiguration.getMaximumSignAttemps()).thenReturn(maximumSignAttemps);
    when(securityConfiguration.getMaximumSignAttempsDelay()).thenReturn(maximumSignAttempsDelay);
    when(securityConfiguration.getDisableSignAttemps()).thenReturn(disableSignAttemps);
    subject = SecurityUtils.getSubject();
  }

  @Test
  public void sign() throws Throwable {
    authenticationService.sign("francois.robert@ircm.qc.ca", "password", false);

    verify(subject).login(tokenCaptor.capture());
    assertEquals(true, tokenCaptor.getValue() instanceof UsernamePasswordToken);
    UsernamePasswordToken token = (UsernamePasswordToken) tokenCaptor.getValue();
    assertEquals("francois.robert@ircm.qc.ca", token.getUsername());
    assertArrayEquals("password".toCharArray(), token.getPassword());
    assertEquals(false, token.isRememberMe());
  }

  @Test
  public void sign_Remember() throws Throwable {
    authenticationService.sign("francois.robert@ircm.qc.ca", "password", true);

    verify(subject).login(tokenCaptor.capture());
    assertEquals(true, tokenCaptor.getValue() instanceof UsernamePasswordToken);
    UsernamePasswordToken token = (UsernamePasswordToken) tokenCaptor.getValue();
    assertEquals("francois.robert@ircm.qc.ca", token.getUsername());
    assertArrayEquals("password".toCharArray(), token.getPassword());
    assertEquals(true, token.isRememberMe());
  }

  @Test
  public void sign_AuthenticationException() throws Throwable {
    doThrow(new AuthenticationException("test")).when(subject).login(tokenCaptor.capture());

    try {
      authenticationService.sign("francois.robert@ircm.qc.ca", "password", true);
      fail("Expected AuthenticationException");
    } catch (AuthenticationException e) {
      // Ignore.
    }

    verify(subject).login(tokenCaptor.capture());
    assertEquals(true, tokenCaptor.getValue() instanceof UsernamePasswordToken);
    UsernamePasswordToken token = (UsernamePasswordToken) tokenCaptor.getValue();
    assertEquals("francois.robert@ircm.qc.ca", token.getUsername());
    assertArrayEquals("password".toCharArray(), token.getPassword());
    assertEquals(true, token.isRememberMe());
  }

  @Test
  public void sign_NullUsername() throws Throwable {
    try {
      authenticationService.sign(null, "password", false);
      fail("Expected AuthenticationException");
    } catch (AuthenticationException e) {
      // Ignore.
    }
  }

  @Test
  public void sign_NullPassword() throws Throwable {
    try {
      authenticationService.sign("francois.robert@ircm.qc.ca", null, false);
      fail("Expected AuthenticationException");
    } catch (AuthenticationException e) {
      // Ignore.
    }
  }

  @Test
  public void signout() throws Throwable {
    authenticationService.signout();

    verify(subject).logout();
  }

  @Test
  public void runAs() throws Throwable {
    User user = new User(3L);

    authenticationService.runAs(user);

    verify(subject).checkRole(UserRole.ADMIN);
    verify(subject).runAs(principalCollectionCaptor.capture());
    PrincipalCollection principalCollection = principalCollectionCaptor.getValue();
    assertEquals(user.getId(), principalCollection.getPrimaryPrincipal());
    assertEquals(1, principalCollection.fromRealm(realmName).size());
    assertEquals(user.getId(), principalCollection.fromRealm(realmName).iterator().next());
  }

  @Test
  public void runAs_Admin() throws Throwable {
    User user = new User(1L);

    try {
      authenticationService.runAs(user);
      fail("Expected AuthorizationException");
    } catch (AuthorizationException e) {
      // Ignore.
    }
  }

  @Test
  public void runAs_Null() throws Throwable {
    try {
      authenticationService.runAs(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Ignore.
    }
  }

  @Test
  public void stopRunAs() throws Throwable {
    when(subject.releaseRunAs()).thenReturn(new SimplePrincipalCollection(2L, realmName));

    Long userId = authenticationService.stopRunAs();

    verify(subject).releaseRunAs();
    assertEquals((Long) 2L, userId);
  }

  @Test
  public void getAuthenticationInfo_2() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken("francois.robert@ircm.qc.ca", "pass1");

    AuthenticationInfo authentication = authenticationService.getAuthenticationInfo(token);

    assertEquals(2L, authentication.getPrincipals().getPrimaryPrincipal());
    assertEquals(1, authentication.getPrincipals().fromRealm(realmName).size());
    assertEquals(2L, authentication.getPrincipals().fromRealm(realmName).iterator().next());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        authentication.getCredentials());
    assertTrue(authentication instanceof SaltedAuthenticationInfo);
    SaltedAuthenticationInfo saltedAuthentication = (SaltedAuthenticationInfo) authentication;
    assertNull(saltedAuthentication.getCredentialsSalt());
    User user = userRepository.findById(2L).orElse(null);
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
    verifyZeroInteractions(shiroLdapService);
  }

  @Test
  public void getAuthenticationInfo_3() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken("jonh.smith@ircm.qc.ca", "pass1");

    AuthenticationInfo authentication = authenticationService.getAuthenticationInfo(token);

    assertEquals(3L, authentication.getPrincipals().getPrimaryPrincipal());
    assertEquals(1, authentication.getPrincipals().fromRealm(realmName).size());
    assertEquals(3L, authentication.getPrincipals().fromRealm(realmName).iterator().next());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        authentication.getCredentials());
    assertTrue(authentication instanceof SaltedAuthenticationInfo);
    SaltedAuthenticationInfo saltedAuthentication = (SaltedAuthenticationInfo) authentication;
    assertNull(saltedAuthentication.getCredentialsSalt());
    User user = userRepository.findById(3L).orElse(null);
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
    verifyZeroInteractions(shiroLdapService);
  }

  @Test(expected = DisabledAccountException.class)
  public void getAuthenticationInfo_Inactive() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken("inactive.user@ircm.qc.ca", "pass1");

    authenticationService.getAuthenticationInfo(token);
  }

  @Test(expected = UnknownAccountException.class)
  public void getAuthenticationInfo_NotExists() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken("non.user@ircm.qc.ca", "pass1");

    authenticationService.getAuthenticationInfo(token);
  }

  @Test(expected = UnknownAccountException.class)
  public void getAuthenticationInfo_NullUsername() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken(null, "pass1");

    authenticationService.getAuthenticationInfo(token);
  }

  @Test(expected = UnknownAccountException.class)
  public void getAuthenticationInfo_NullPassword() throws Throwable {
    UsernamePasswordToken token =
        new UsernamePasswordToken("francois.robert@ircm.qc.ca", (String) null);

    authenticationService.getAuthenticationInfo(token);
  }

  @Test
  public void getAuthenticationInfo_InvalidPassword() throws Throwable {
    UsernamePasswordToken token =
        new UsernamePasswordToken("francois.robert@ircm.qc.ca", "password2");

    try {
      authenticationService.getAuthenticationInfo(token);
      fail("Expected IncorrectCredentialsException");
    } catch (IncorrectCredentialsException e) {
      // Success.
    }

    User user = userRepository.findById(2L).orElse(null);
    assertEquals(1, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
    verifyZeroInteractions(shiroLdapService);
  }

  @Test
  public void getAuthenticationInfo_NotTooManyAttempts() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    user.setSignAttempts(maximumSignAttemps - 1);
    Instant lastSignAttempt = Instant.now();
    user.setLastSignAttempt(lastSignAttempt);
    userRepository.save(user);
    UsernamePasswordToken token = new UsernamePasswordToken("francois.robert@ircm.qc.ca", "pass1");

    authenticationService.getAuthenticationInfo(token);

    user = userRepository.findById(2L).orElse(null);
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
    verifyZeroInteractions(shiroLdapService);
  }

  @Test
  public void getAuthenticationInfo_TooManyAttempts() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken("francois.robert@ircm.qc.ca", "pass1");
    User user = userRepository.findById(2L).orElse(null);
    user.setSignAttempts(maximumSignAttemps);
    Instant lastSignAttempt = Instant.now();
    user.setLastSignAttempt(lastSignAttempt);
    userRepository.save(user);

    try {
      authenticationService.getAuthenticationInfo(token);
      fail("Expected ExcessiveAttemptsException");
    } catch (ExcessiveAttemptsException e) {
      // Success.
    }

    user = userRepository.findById(2L).orElse(null);
    assertEquals(maximumSignAttemps, user.getSignAttempts());
    assertEquals(lastSignAttempt, user.getLastSignAttempt());
    verifyZeroInteractions(shiroLdapService);
  }

  @Test
  public void getAuthenticationInfo_CanAttemptAgain_Success() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    user.setSignAttempts(maximumSignAttemps);
    user.setLastSignAttempt(Instant.now().minusMillis(maximumSignAttempsDelay).minusMillis(10));
    userRepository.save(user);
    UsernamePasswordToken token = new UsernamePasswordToken("francois.robert@ircm.qc.ca", "pass1");

    authenticationService.getAuthenticationInfo(token);

    user = userRepository.findById(2L).orElse(null);
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
    verifyZeroInteractions(shiroLdapService);
  }

  @Test
  public void getAuthenticationInfo_CanAttemptAgain_Fail() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken("francois.robert@ircm.qc.ca", "wrong");
    User user = userRepository.findById(2L).orElse(null);
    user.setSignAttempts(maximumSignAttemps);
    user.setLastSignAttempt(Instant.now().minusMillis(maximumSignAttempsDelay).minusMillis(10));
    userRepository.save(user);
    doThrow(new AuthenticationException("test")).when(subject).login(tokenCaptor.capture());

    try {
      authenticationService.getAuthenticationInfo(token);
      fail("Expected IncorrectCredentialsException");
    } catch (IncorrectCredentialsException e) {
      // Success.
    }

    user = userRepository.findById(2L).orElse(null);
    assertEquals(maximumSignAttemps + 1, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
    verifyZeroInteractions(shiroLdapService);
  }

  @Test
  public void getAuthenticationInfo_CanAttemptAgain_Disable() throws Throwable {
    UsernamePasswordToken token = new UsernamePasswordToken("francois.robert@ircm.qc.ca", "wrong");
    User user = userRepository.findById(2L).orElse(null);
    user.setSignAttempts(disableSignAttemps - 1);
    user.setLastSignAttempt(Instant.now().minusMillis(maximumSignAttempsDelay).minusMillis(10));
    userRepository.save(user);
    doThrow(new AuthenticationException("test")).when(subject).login(tokenCaptor.capture());

    try {
      authenticationService.getAuthenticationInfo(token);
      fail("Expected IncorrectCredentialsException");
    } catch (IncorrectCredentialsException e) {
      // Success.
    }

    user = userRepository.findById(2L).orElse(null);
    assertEquals(disableSignAttemps, user.getSignAttempts());
    assertFalse(user.isActive());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
    verifyZeroInteractions(shiroLdapService);
  }

  @Test
  public void getAuthenticationInfo_ShiroLdap() throws Throwable {
    when(ldapConfiguration.isEnabled()).thenReturn(true);
    when(shiroLdapService.getEmail(any(String.class), any(String.class)))
        .thenReturn("francois.robert@ircm.qc.ca");
    UsernamePasswordToken token = new UsernamePasswordToken("poitrasc", "password2");

    AuthenticationInfo authentication = authenticationService.getAuthenticationInfo(token);

    verify(shiroLdapService).getEmail("poitrasc", "password2");
    assertEquals(2L, authentication.getPrincipals().getPrimaryPrincipal());
    assertEquals(1, authentication.getPrincipals().fromRealm(realmName).size());
    assertEquals(2L, authentication.getPrincipals().fromRealm(realmName).iterator().next());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        authentication.getCredentials());
    assertTrue(authentication instanceof SaltedAuthenticationInfo);
    SaltedAuthenticationInfo saltedAuthentication = (SaltedAuthenticationInfo) authentication;
    assertNull(saltedAuthentication.getCredentialsSalt());
    User user = userRepository.findById(2L).orElse(null);
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
  }

  @Test
  public void getAuthenticationInfo_ShiroLdapNotExists_LocalExists() throws Throwable {
    when(ldapConfiguration.isEnabled()).thenReturn(true);
    UsernamePasswordToken token = new UsernamePasswordToken("francois.robert@ircm.qc.ca", "pass1");

    AuthenticationInfo authentication = authenticationService.getAuthenticationInfo(token);

    verify(shiroLdapService).getEmail("francois.robert@ircm.qc.ca", "pass1");
    assertEquals(2L, authentication.getPrincipals().getPrimaryPrincipal());
    assertEquals(1, authentication.getPrincipals().fromRealm(realmName).size());
    assertEquals(2L, authentication.getPrincipals().fromRealm(realmName).iterator().next());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1,
        authentication.getCredentials());
    assertTrue(authentication instanceof SaltedAuthenticationInfo);
    SaltedAuthenticationInfo saltedAuthentication = (SaltedAuthenticationInfo) authentication;
    assertNull(saltedAuthentication.getCredentialsSalt());
    User user = userRepository.findById(2L).orElse(null);
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minusMillis(5000).isBefore(user.getLastSignAttempt()));
    assertTrue(Instant.now().plusMillis(5000).isAfter(user.getLastSignAttempt()));
  }

  @Test(expected = UnknownAccountException.class)
  public void getAuthenticationInfo_ShiroLdapInvalid_LocalNotExists() throws Throwable {
    when(ldapConfiguration.isEnabled()).thenReturn(true);
    UsernamePasswordToken token = new UsernamePasswordToken("poitrasc", "pass1");

    authenticationService.getAuthenticationInfo(token);
  }

  @Test
  public void getAuthorizationInfo_2() {
    AuthorizationInfo authorization =
        authenticationService.getAuthorizationInfo(new SimplePrincipalCollection(2L, realmName));

    assertEquals(true, authorization.getRoles().contains(USER));
    assertEquals(true, authorization.getRoles().contains(MANAGER));
    assertEquals(false, authorization.getRoles().contains(ADMIN));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:2")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:write:2")));
    assertEquals(true, implies(authorization.getObjectPermissions(),
        new WildcardPermission("user:write_password:2")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:10")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:1")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:2")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:3")));
    assertEquals(true, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:1")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:2")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:3")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("abc:read:1")));
  }

  @Test
  public void getAuthorizationInfo_1() {
    AuthorizationInfo authorization =
        authenticationService.getAuthorizationInfo(new SimplePrincipalCollection(1L, realmName));

    assertEquals(true, authorization.getRoles().contains(USER));
    assertEquals(false, authorization.getRoles().contains(MANAGER));
    assertEquals(true, authorization.getRoles().contains(ADMIN));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:1")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:write:1")));
    assertEquals(true, implies(authorization.getObjectPermissions(),
        new WildcardPermission("user:write_password:1")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:10")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:1")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:2")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:3")));
    assertEquals(true, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:1")));
    assertEquals(true, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:2")));
    assertEquals(true, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:3")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("abc:read:1")));
  }

  @Test
  public void getAuthorizationInfo_5() {
    AuthorizationInfo authorization =
        authenticationService.getAuthorizationInfo(new SimplePrincipalCollection(5L, realmName));

    assertEquals(true, authorization.getRoles().contains(USER));
    assertEquals(false, authorization.getRoles().contains(MANAGER));
    assertEquals(false, authorization.getRoles().contains(ADMIN));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:5")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:write:5")));
    assertEquals(true, implies(authorization.getObjectPermissions(),
        new WildcardPermission("user:write_password:5")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:10")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:1")));
    assertEquals(true,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:2")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:3")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:1")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:2")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:3")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("abc:read:1")));
  }

  @Test
  public void getAuthorizationInfo_Inactive() {
    AuthorizationInfo authorization =
        authenticationService.getAuthorizationInfo(new SimplePrincipalCollection(6L, realmName));

    assertEquals(false, authorization.getRoles().contains(USER));
    assertEquals(false, authorization.getRoles().contains(MANAGER));
    assertEquals(false, authorization.getRoles().contains(ADMIN));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:12")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:write:12")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("user:write_password:12")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("user:read:10")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:1")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:2")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("laboratory:read:3")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:1")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:2")));
    assertEquals(false, implies(authorization.getObjectPermissions(),
        new WildcardPermission("laboratory:manager:3")));
    assertEquals(false,
        implies(authorization.getObjectPermissions(), new WildcardPermission("abc:read:1")));
  }

  @Test
  public void getAuthorizationInfo_Null() {
    AuthorizationInfo authorization = authenticationService.getAuthorizationInfo(null);
    assertNull(authorization.getRoles());
    assertNull(authorization.getStringPermissions());
    assertNull(authorization.getObjectPermissions());
  }

  private boolean implies(Iterable<Permission> permissions, Permission permission) {
    for (Permission test : permissions) {
      if (test.implies(permission)) {
        return true;
      }
    }
    return false;
  }
}
