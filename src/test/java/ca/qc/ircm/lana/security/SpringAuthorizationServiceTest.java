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

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Owned;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class SpringAuthorizationServiceTest {
  private static final String DEFAULT_ROLE = USER;
  private SpringAuthorizationService authorizationService;
  @Inject
  private UserRepository userRepository;
  @Inject
  private ExperimentRepository experimentRepository;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    authorizationService = new SpringAuthorizationService(userRepository);
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

  @Test(expected = AccessDeniedException.class)
  @WithMockUser
  public void checkRole_Fail() throws Throwable {
    authorizationService.checkRole(ADMIN);
  }

  @Test
  @WithMockUser
  public void checkRole_Ok() throws Throwable {
    authorizationService.checkRole(DEFAULT_ROLE);
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

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void checkAuthorization_Experiment_Anonymous() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void checkAuthorization_Experiment_Owner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkAuthorization_Experiment_NotOwner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void checkAuthorization_Experiment_Manager() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void checkAuthorization_Experiment_ManagerOtherLab() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void checkAuthorization_Experiment_Admin() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithAnonymousUser
  public void checkAuthorization_Null_Anonymous() throws Throwable {
    authorizationService.checkRead((Owned) null);
  }

  @Test
  @WithMockUser
  public void checkAuthorization_Null() throws Throwable {
    authorizationService.checkRead((Owned) null);
  }

  @Test
  @WithMockUser
  public void checkAuthorization_NullOwner() throws Throwable {
    Experiment experiment = new Experiment();
    authorizationService.checkRead(experiment);
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
