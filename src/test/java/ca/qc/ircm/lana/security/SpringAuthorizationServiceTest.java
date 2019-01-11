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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.Owned;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
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
  @Mock
  private AclService aclService;
  @Mock
  private Acl acl;
  @Captor
  private ArgumentCaptor<List<Permission>> permissionsCaptor;
  @Captor
  private ArgumentCaptor<List<Sid>> sidsCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  @Inject
  private LaboratoryRepository laboratoryRepository;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    authorizationService = new SpringAuthorizationService(userRepository, aclService);
    when(aclService.readAclById(any())).thenAnswer(i -> {
      if (i.getArgument(0) != null) {
        throw new NotFoundException("Cannot find ACL");
      }
      return null;
    });
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

  @Test(expected = AccessDeniedException.class)
  @WithMockUser
  public void checkAnyRole_False() throws Throwable {
    authorizationService.checkAnyRole(ADMIN, MANAGER);
  }

  @Test
  @WithMockUser
  public void checkAnyRole_TrueFirst() throws Throwable {
    authorizationService.hasAnyRole(DEFAULT_ROLE, MANAGER);
  }

  @Test
  @WithMockUser
  public void checkAnyRole_TrueLast() throws Throwable {
    authorizationService.hasAnyRole(ADMIN, DEFAULT_ROLE);
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
  public void checkRead_Experiment_Anonymous() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void checkRead_Experiment_Owner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkRead_Experiment_NotOwner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void checkRead_Experiment_Manager() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void checkRead_Experiment_ManagerOtherLab() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void checkRead_Experiment_Admin() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkRead_Experiment_AclAllowed() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(true);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    authorizationService.checkRead(experiment);

    verify(aclService).readAclById(new ObjectIdentityImpl(Experiment.class, 2L));
    verify(acl).isGranted(permissionsCaptor.capture(), sidsCaptor.capture(), eq(false));
    List<Permission> permissions = permissionsCaptor.getValue();
    assertEquals(1, permissions.size());
    assertEquals(BasePermission.READ, permissions.get(0));
    List<Sid> sids = sidsCaptor.getValue();
    assertEquals(1, sids.size());
    assertEquals(new PrincipalSid("christian.poitras@ircm.qc.ca"), sids.get(0));
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkRead_Experiment_AclDenied() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(false);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    authorizationService.checkRead(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void checkRead_Laboratory_Anonymous() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkRead(laboratory);
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void checkRead_Laboratory_Member() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkRead(laboratory);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkRead_Laboratory_NotMember() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkRead(laboratory);
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void checkRead_Laboratory_Admin() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkRead(laboratory);
  }

  @Test
  @WithAnonymousUser
  public void checkRead_NullOwned_Anonymous() throws Throwable {
    authorizationService.checkRead((Owned) null);
  }

  @Test
  @WithMockUser
  public void checkRead_NullOwned() throws Throwable {
    authorizationService.checkRead((Owned) null);
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkRead_NullOwner() throws Throwable {
    Experiment experiment = new Experiment();
    authorizationService.checkRead(experiment);
  }

  @Test
  @WithAnonymousUser
  public void checkRead_NullLaboratory_Anonymous() throws Throwable {
    authorizationService.checkRead((Laboratory) null);
  }

  @Test
  @WithMockUser
  public void checkRead_NullLaboratory() throws Throwable {
    authorizationService.checkRead((Laboratory) null);
  }

  @Test
  @WithAnonymousUser
  public void hasWrite_Experiment_Anonymous() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasWrite_Experiment_Owner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasWrite_Experiment_NotOwner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasWrite_Experiment_Manager() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void hasWrite_Experiment_ManagerOtherLab() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasWrite_Experiment_Admin() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasWrite_Experiment_AclAllowed() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(true);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertTrue(authorizationService.hasWrite(experiment));

    verify(aclService).readAclById(new ObjectIdentityImpl(Experiment.class, 2L));
    verify(acl).isGranted(permissionsCaptor.capture(), sidsCaptor.capture(), eq(false));
    List<Permission> permissions = permissionsCaptor.getValue();
    assertEquals(1, permissions.size());
    assertEquals(BasePermission.WRITE, permissions.get(0));
    List<Sid> sids = sidsCaptor.getValue();
    assertEquals(1, sids.size());
    assertEquals(new PrincipalSid("christian.poitras@ircm.qc.ca"), sids.get(0));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasWrite_Experiment_AclDenied() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(false);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertFalse(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithAnonymousUser
  public void hasWrite_Laboratory_Anonymous() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    assertFalse(authorizationService.hasWrite(laboratory));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasWrite_Laboratory_MemberManager() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    assertTrue(authorizationService.hasWrite(laboratory));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasWrite_Laboratory_MemberNotManager() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    assertFalse(authorizationService.hasWrite(laboratory));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasWrite_Laboratory_NotMember() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    assertFalse(authorizationService.hasWrite(laboratory));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasWrite_Laboratory_Admin() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    assertTrue(authorizationService.hasWrite(laboratory));
  }

  @Test
  @WithAnonymousUser
  public void hasWrite_NullOwned_Anonymous() throws Throwable {
    assertFalse(authorizationService.hasWrite((Owned) null));
  }

  @Test
  @WithMockUser
  public void hasWrite_NullOwned() throws Throwable {
    assertFalse(authorizationService.hasWrite((Owned) null));
  }

  @Test
  @WithMockUser
  public void hasWrite_NullOwner() throws Throwable {
    Experiment experiment = new Experiment();
    assertFalse(authorizationService.hasWrite(experiment));
  }

  @Test
  @WithAnonymousUser
  public void hasWrite_NullLaboratory_Anonymous() throws Throwable {
    assertFalse(authorizationService.hasWrite((Laboratory) null));
  }

  @Test
  @WithMockUser
  public void hasWrite_NullLaboratory() throws Throwable {
    assertFalse(authorizationService.hasWrite((Laboratory) null));
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void checkWrite_Experiment_Anonymous() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(experiment);
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void checkWrite_Experiment_Owner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkWrite_Experiment_NotOwner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(experiment);
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void checkWrite_Experiment_Manager() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void checkWrite_Experiment_ManagerOtherLab() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(experiment);
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void checkWrite_Experiment_Admin() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(experiment);
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkWrite_Experiment_AclAllowed() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(true);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    authorizationService.checkWrite(experiment);

    verify(aclService).readAclById(new ObjectIdentityImpl(Experiment.class, 2L));
    verify(acl).isGranted(permissionsCaptor.capture(), sidsCaptor.capture(), eq(false));
    List<Permission> permissions = permissionsCaptor.getValue();
    assertEquals(1, permissions.size());
    assertEquals(BasePermission.WRITE, permissions.get(0));
    List<Sid> sids = sidsCaptor.getValue();
    assertEquals(1, sids.size());
    assertEquals(new PrincipalSid("christian.poitras@ircm.qc.ca"), sids.get(0));
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkWrite_Experiment_AclDenied() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(false);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    authorizationService.checkWrite(experiment);
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void checkWrite_Laboratory_Anonymous() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(laboratory);
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void checkWrite_Laboratory_MemberManager() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(laboratory);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void checkWrite_Laboratory_MemberNotManager() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(laboratory);
  }

  @Test(expected = AccessDeniedException.class)
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkWrite_Laboratory_NotMember() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(laboratory);
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void checkWrite_Laboratory_Admin() throws Throwable {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    authorizationService.checkWrite(laboratory);
  }

  @Test
  @WithAnonymousUser
  public void checkWrite_NullOwned_Anonymous() throws Throwable {
    authorizationService.checkWrite((Owned) null);
  }

  @Test
  @WithMockUser
  public void checkWrite_NullOwned() throws Throwable {
    authorizationService.checkWrite((Owned) null);
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void checkWrite_NullOwner() throws Throwable {
    Experiment experiment = new Experiment();
    authorizationService.checkWrite(experiment);
  }

  @Test
  @WithAnonymousUser
  public void checkWrite_NullLaboratory_Anonymous() throws Throwable {
    authorizationService.checkWrite((Laboratory) null);
  }

  @Test
  @WithMockUser
  public void checkWrite_NullLaboratory() throws Throwable {
    authorizationService.checkWrite((Laboratory) null);
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
