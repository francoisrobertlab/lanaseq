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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.test.config.WithSubject;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class AuthorizationServiceTest {
  @Inject
  private UserRepository userRepository;
  private AuthorizationService authorizationService;
  private Subject subject;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    authorizationService = new AuthorizationService(userRepository);
    subject = SecurityUtils.getSubject();
  }

  @Test
  @WithSubject(anonymous = true)
  public void currentUser_Anonymous() throws Throwable {
    assertNull(authorizationService.currentUser());
  }

  @Test
  @WithSubject(userId = 1)
  public void currentUser() throws Throwable {
    User user = authorizationService.currentUser();
    assertNotNull(authorizationService.currentUser());
    assertEquals((Long) 1L, user.getId());
  }

  @Test
  @WithSubject(anonymous = true)
  public void isAnonymous_True() throws Throwable {
    assertTrue(authorizationService.isAnonymous());
  }

  @Test
  @WithSubject(anonymous = false)
  public void isAnonymous_False() throws Throwable {
    assertFalse(authorizationService.isAnonymous());
  }

  @Test
  @WithSubject
  public void hasRole_False() throws Throwable {
    assertFalse(authorizationService.hasRole(ADMIN));
    verify(subject).hasRole(ADMIN);
  }

  @Test
  @WithSubject
  public void hasRole_True() throws Throwable {
    when(subject.hasRole(ADMIN)).thenReturn(true);
    assertTrue(authorizationService.hasRole(ADMIN));
    verify(subject).hasRole(ADMIN);
  }

  @Test
  @WithSubject
  public void hasAnyRole_False() throws Throwable {
    assertFalse(authorizationService.hasAnyRole(ADMIN, MANAGER));
    verify(subject).hasRole(ADMIN);
    verify(subject).hasRole(MANAGER);
  }

  @Test
  @WithSubject
  public void hasAnyRole_TrueFirst() throws Throwable {
    when(subject.hasRole(ADMIN)).thenReturn(true);
    assertTrue(authorizationService.hasAnyRole(ADMIN, MANAGER));
    verify(subject).hasRole(ADMIN);
  }

  @Test
  @WithSubject
  public void hasAnyRole_TrueLast() throws Throwable {
    when(subject.hasRole(MANAGER)).thenReturn(true);
    assertTrue(authorizationService.hasAnyRole(ADMIN, MANAGER));
    verify(subject).hasRole(MANAGER);
  }

  @Test
  public void isAuthorized_NoRole() throws Throwable {
    assertTrue(authorizationService.isAuthorized(NoRoleTest.class));
    verifyZeroInteractions(subject);
  }

  @Test
  public void isAuthorized_UserRole_True() throws Throwable {
    when(subject.hasRole(USER)).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(UserRoleTest.class));
    verify(subject).hasRole(USER);
  }

  @Test
  public void isAuthorized_UserRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(UserRoleTest.class));
    verify(subject).hasRole(USER);
  }

  @Test
  public void isAuthorized_ManagerRole_True() throws Throwable {
    when(subject.hasRole(MANAGER)).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(ManagerRoleTest.class));
    verify(subject).hasRole(MANAGER);
  }

  @Test
  public void isAuthorized_ManagerRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(ManagerRoleTest.class));
    verify(subject).hasRole(MANAGER);
  }

  @Test
  public void isAuthorized_AdminRole_True() throws Throwable {
    when(subject.hasRole(ADMIN)).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(AdminRoleTest.class));
    verify(subject).hasRole(ADMIN);
  }

  @Test
  public void isAuthorized_AdminRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(AdminRoleTest.class));
    verify(subject).hasRole(ADMIN);
  }

  @Test
  public void isAuthorized_ManagerOrAdminRole_Manager() throws Throwable {
    when(subject.hasRole(MANAGER)).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
    verify(subject).hasRole(MANAGER);
  }

  @Test
  public void isAuthorized_ManagerOrAdminRole_Admin() throws Throwable {
    when(subject.hasRole(ADMIN)).thenReturn(true);
    assertTrue(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
    verify(subject).hasRole(ADMIN);
  }

  @Test
  public void isAuthorized_ManagerOrAdminRole_False() throws Throwable {
    assertFalse(authorizationService.isAuthorized(ManagerOrAdminRoleTest.class));
    verify(subject).hasRole(MANAGER);
    verify(subject).hasRole(ADMIN);
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
