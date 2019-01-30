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

package ca.qc.ircm.lana.experiment;

import static ca.qc.ircm.lana.test.utils.SearchUtils.find;
import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserAuthority;
import ca.qc.ircm.lana.user.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentServiceTest {
  private ExperimentService service;
  @Inject
  private ExperimentRepository repository;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  @Inject
  private MutableAclService aclService;
  @Inject
  private UserRepository userRepository;
  @Mock
  private AuthorizationService authorizationService;

  @Before
  public void beforeTest() {
    service =
        new ExperimentService(repository, laboratoryRepository, aclService, authorizationService);
  }

  @Test
  public void get() {
    Experiment experiment = service.get(1L);

    assertEquals((Long) 1L, experiment.getId());
    assertEquals("POLR2A DNA location", experiment.getName());
    assertEquals((Long) 2L, experiment.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), experiment.getDate());
    verify(authorizationService).checkPermission(experiment, BasePermission.READ);
  }

  @Test
  public void get_Null() {
    Experiment experiment = service.get(null);
    assertNull(experiment);
  }

  @Test
  public void all() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);

    List<Experiment> experiments = service.all();

    assertEquals(3, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void all_Manager() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);
    when(authorizationService.hasRole(MANAGER)).thenReturn(true);

    List<Experiment> experiments = service.all();

    assertEquals(3, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void all_Admin() {
    User user = userRepository.findById(1L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);
    when(authorizationService.hasRole(ADMIN)).thenReturn(true);

    List<Experiment> experiments = service.all();

    assertEquals(5, experiments.size());
    assertTrue(find(experiments, 1L).isPresent());
    assertTrue(find(experiments, 2L).isPresent());
    assertTrue(find(experiments, 3L).isPresent());
    assertTrue(find(experiments, 4L).isPresent());
    assertTrue(find(experiments, 5L).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void permissions() {
    Experiment experiment = repository.findById(2L).orElse(null);

    Set<Laboratory> laboratories = service.permissions(experiment);

    assertEquals(2, laboratories.size());
    assertTrue(find(laboratories, 2L).isPresent());
    assertTrue(find(laboratories, 3L).isPresent());
    verify(authorizationService).checkPermission(experiment, BasePermission.WRITE);
  }

  @Test
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(user);
    Experiment experiment = new Experiment();
    experiment.setName("New experiment");

    service.save(experiment);

    assertNotNull(experiment.getId());
    Experiment database = repository.findById(experiment.getId()).orElse(null);
    assertEquals(experiment.getName(), database.getName());
    assertEquals(user.getId(), database.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(experiment.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(experiment.getDate()));
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void save_Update() {
    Experiment experiment = repository.findById(1L).orElse(null);
    experiment.setName("New name");

    service.save(experiment);

    experiment = repository.findById(1L).orElse(null);
    assertEquals("New name", experiment.getName());
    assertEquals((Long) 2L, experiment.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), experiment.getDate());
    verify(authorizationService).checkPermission(experiment, BasePermission.WRITE);
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
    verify(authorizationService).checkPermission(experiment, BasePermission.WRITE);
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
