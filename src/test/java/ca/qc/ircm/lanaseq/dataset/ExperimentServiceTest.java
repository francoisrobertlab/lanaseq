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

package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Experiment;
import ca.qc.ircm.lanaseq.dataset.ExperimentRepository;
import ca.qc.ircm.lanaseq.dataset.ExperimentService;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserAuthority;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryRepository;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private ExperimentService service;
  @Autowired
  private ExperimentRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private LaboratoryRepository laboratoryRepository;
  @Autowired
  private MutableAclService aclService;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private AuthorizationService authorizationService;
  @MockBean
  private PermissionEvaluator permissionEvaluator;

  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
  }

  @Test
  @WithMockUser
  public void get() {
    Experiment experiment = service.get(1L);

    assertEquals((Long) 1L, experiment.getId());
    assertEquals("POLR2A DNA location", experiment.getName());
    assertEquals("polymerase", experiment.getProject());
    assertEquals((Long) 1L, experiment.getProtocol().getId());
    assertEquals((Long) 2L, experiment.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), experiment.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Null() {
    Experiment experiment = service.get(null);
    assertNull(experiment);
  }

  @Test
  @WithMockUser
  public void all() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);

    List<Experiment> experiments = service.all();

    assertEquals(3, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    for (Experiment experiment : experiments) {
      verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void all_Manager() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    when(authorizationService.hasRole(MANAGER)).thenReturn(true);

    List<Experiment> experiments = service.all();

    assertEquals(3, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    for (Experiment experiment : experiments) {
      verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void all_Admin() {
    User user = userRepository.findById(1L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    when(authorizationService.hasRole(ADMIN)).thenReturn(true);

    List<Experiment> experiments = service.all();

    assertEquals(5, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    assertTrue(find(experiments, 4L).isPresent());
    assertTrue(find(experiments, 5L).isPresent());
    for (Experiment experiment : experiments) {
      verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void permissions() {
    Experiment experiment = repository.findById(2L).orElse(null);

    Set<Laboratory> laboratories = service.permissions(experiment);

    assertEquals(2, laboratories.size());
    assertTrue(find(laboratories, 2L).isPresent());
    assertTrue(find(laboratories, 3L).isPresent());
    verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Experiment experiment = new Experiment();
    experiment.setName("New experiment");
    experiment.setProject("my project");
    experiment.setProtocol(protocolRepository.findById(1L).get());

    service.save(experiment);

    assertNotNull(experiment.getId());
    Experiment database = repository.findById(experiment.getId()).orElse(null);
    assertEquals(experiment.getName(), database.getName());
    assertEquals("my project", database.getProject());
    assertEquals((Long) 1L, database.getProtocol().getId());
    assertEquals(user.getId(), database.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(experiment.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(experiment.getDate()));
    verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_Update() {
    Experiment experiment = repository.findById(1L).orElse(null);
    experiment.setName("New name");
    experiment.setProject("my project");
    experiment.setProtocol(protocolRepository.findById(3L).get());

    service.save(experiment);

    experiment = repository.findById(1L).orElse(null);
    assertEquals("New name", experiment.getName());
    assertEquals("my project", experiment.getProject());
    assertEquals((Long) 3L, experiment.getProtocol().getId());
    assertEquals((Long) 2L, experiment.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), experiment.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(WRITE));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  @Transactional
  public void savePermissions() {
    Experiment experiment = repository.findById(5L).orElse(null);
    List<Laboratory> laboratories = new ArrayList<>();
    laboratories.add(laboratoryRepository.findById(2L).orElse(null));

    service.savePermissions(experiment, laboratories);

    ObjectIdentity oi = new ObjectIdentityImpl(experiment.getClass(), experiment.getId());
    Acl acl = aclService.readAclById(oi);
    assertFalse(granted(acl, BasePermission.READ, laboratoryRepository.findById(1L).orElse(null)));
    assertTrue(granted(acl, BasePermission.READ, laboratoryRepository.findById(2L).orElse(null)));
    assertFalse(granted(acl, BasePermission.READ, laboratoryRepository.findById(3L).orElse(null)));
    verify(permissionEvaluator).hasPermission(any(), eq(experiment), eq(WRITE));
  }

  private boolean granted(Acl acl, Permission permission, Laboratory laboratory) {
    try {
      return acl.isGranted(list(permission),
          list(new GrantedAuthoritySid(UserAuthority.laboratoryMember(laboratory))), false);
    } catch (NotFoundException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> list(T... values) {
    return Stream.of(values).collect(Collectors.toList());
  }
}
