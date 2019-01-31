/*
 * Copyright (c) 2016 Institut de recherches cliniques de Montreal (IRCM)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserAuthority;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentPermissionEvaluatorTest {
  private static final String EXPERIMENT_CLASS = Experiment.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = BasePermission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = BasePermission.WRITE;
  @Inject
  private ExperimentPermissionEvaluator permissionEvaluator;
  @Inject
  private ExperimentRepository experimentRepository;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  @MockBean
  private MutableAclService aclService;
  @Mock
  private Acl acl;
  @Captor
  private ArgumentCaptor<List<Permission>> permissionsCaptor;
  @Captor
  private ArgumentCaptor<List<Sid>> sidsCaptor;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(aclService.readAclById(any())).thenAnswer(i -> {
      if (i.getArgument(0) != null) {
        throw new NotFoundException("Cannot find ACL");
      }
      return null;
    });
  }

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_ReadExperiment_Anonymous() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadExperiment_Owner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadExperiment_OtherLabMember() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_ReadExperiment_NotOwner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadExperiment_Manager() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void hasPermission_ReadExperiment_ManagerOtherLab() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_ReadExperiment_Admin() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_ReadExperiment_AclAllowed() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(true);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));

    verify(aclService, times(4)).readAclById(new ObjectIdentityImpl(Experiment.class, 2L));
    verify(acl, times(4)).isGranted(permissionsCaptor.capture(), sidsCaptor.capture(), eq(false));
    List<Permission> permissions = permissionsCaptor.getValue();
    assertEquals(1, permissions.size());
    assertEquals(BASE_READ, permissions.get(0));
    Laboratory laboratory = laboratoryRepository.findById(3L).orElse(null);
    List<Sid> sids = sidsCaptor.getValue();
    assertEquals(1, sids.size());
    assertEquals(new GrantedAuthoritySid(UserAuthority.laboratoryMember(laboratory)), sids.get(0));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_ReadExperiment_AclDenied() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(false);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_ReadExperiment_AclNotFoundExceptionOnRead() throws Throwable {
    when(aclService.readAclById(any())).thenThrow(new NotFoundException("test"));
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(true);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_ReadExperiment_AclNotFoundExceptionOnIsGrandted() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenThrow(new NotFoundException("test"));
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewExperiment_Anonymous() throws Throwable {
    Experiment experiment = new Experiment("new experiment");
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewExperiment() throws Throwable {
    Experiment experiment = new Experiment("new experiment");
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteExperiment_Anonymous() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteExperiment_Owner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteExperiment_OtherLabMember() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_WriteExperiment_NotOwner() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteExperiment_Manager() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void hasPermission_WriteExperiment_ManagerOtherLab() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_WriteExperiment_Admin() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_WriteExperiment_AclAllowed() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(true);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));

    verify(aclService, times(4)).readAclById(new ObjectIdentityImpl(Experiment.class, 2L));
    verify(acl, times(4)).isGranted(permissionsCaptor.capture(), sidsCaptor.capture(), eq(false));
    List<Permission> permissions = permissionsCaptor.getValue();
    assertEquals(1, permissions.size());
    assertEquals(BASE_WRITE, permissions.get(0));
    Laboratory laboratory = laboratoryRepository.findById(3L).orElse(null);
    List<Sid> sids = sidsCaptor.getValue();
    assertEquals(1, sids.size());
    assertEquals(new GrantedAuthoritySid(UserAuthority.laboratoryMember(laboratory)), sids.get(0));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_WriteExperiment_AclDenied() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(false);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_WriteExperiment_AclNotFoundExceptionOnRead() throws Throwable {
    when(aclService.readAclById(any())).thenThrow(new NotFoundException("test"));
    when(acl.isGranted(any(), any(), anyBoolean())).thenReturn(true);
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_WriteExperiment_AclNotFoundExceptionOnIsGrandted() throws Throwable {
    when(aclService.readAclById(any())).thenReturn(acl);
    when(acl.isGranted(any(), any(), anyBoolean())).thenThrow(new NotFoundException("test"));
    Experiment experiment = experimentRepository.findById(2L).orElse(null);

    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  public void hasPermission_NullAuthentication() throws Throwable {
    Experiment experiment = experimentRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(null, experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(null, experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(null, experiment, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(null, experiment.getId(), EXPERIMENT_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(null, experiment.getId(), EXPERIMENT_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(null, experiment.getId(), EXPERIMENT_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(null, experiment.getId(), EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Null_Anonymous() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_Null() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, EXPERIMENT_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_NotExperiment() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), new User(1L), READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), new User(1L), WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), new User(1L), BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), new User(1L), BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, User.class.getName(), READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, User.class.getName(), WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, User.class.getName(), BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, User.class.getName(), BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_NotLongId() throws Throwable {
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "Informatics", EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", EXPERIMENT_CLASS,
        WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", EXPERIMENT_CLASS,
        BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", EXPERIMENT_CLASS,
        BASE_WRITE));
  }
}
