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

import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
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
public class DatasetServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private DatasetService service;
  @Autowired
  private DatasetRepository repository;
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
    Dataset dataset = service.get(1L);

    assertEquals((Long) 1L, dataset.getId());
    assertEquals("POLR2A DNA location", dataset.getName());
    assertEquals("polymerase", dataset.getProject());
    assertEquals(Assay.MNASE_SEQ, dataset.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, dataset.getType());
    assertEquals("polr2a", dataset.getTarget());
    assertEquals("yFR100", dataset.getStrain());
    assertEquals("WT", dataset.getStrainDescription());
    assertEquals("Rappa", dataset.getTreatment());
    assertEquals((Long) 1L, dataset.getProtocol().getId());
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(3, dataset.getSamples().size());
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 2L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getSamples().get(2).getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Null() {
    Dataset dataset = service.get(null);
    assertNull(dataset);
  }

  @Test
  @WithMockUser
  public void all() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);

    List<Dataset> datasets = service.all();

    assertEquals(3, datasets.size());
    assertTrue(find(datasets, 1L).isPresent());
    assertTrue(find(datasets, 2L).isPresent());
    assertTrue(find(datasets, 3L).isPresent());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void all_Manager() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    when(authorizationService.hasRole(MANAGER)).thenReturn(true);

    List<Dataset> datasets = service.all();

    assertEquals(3, datasets.size());
    assertTrue(find(datasets, 1L).isPresent());
    assertTrue(find(datasets, 2L).isPresent());
    assertTrue(find(datasets, 3L).isPresent());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void all_Admin() {
    User user = userRepository.findById(1L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    when(authorizationService.hasRole(ADMIN)).thenReturn(true);

    List<Dataset> datasets = service.all();

    assertEquals(5, datasets.size());
    assertTrue(find(datasets, 1L).isPresent());
    assertTrue(find(datasets, 2L).isPresent());
    assertTrue(find(datasets, 3L).isPresent());
    assertTrue(find(datasets, 4L).isPresent());
    assertTrue(find(datasets, 5L).isPresent());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void permissions() {
    Dataset dataset = repository.findById(2L).orElse(null);

    Set<Laboratory> laboratories = service.permissions(dataset);

    assertEquals(2, laboratories.size());
    assertTrue(find(laboratories, 2L).isPresent());
    assertTrue(find(laboratories, 3L).isPresent());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Dataset dataset = new Dataset();
    dataset.setName("New dataset");
    dataset.setProject("my project");
    dataset.setAssay(Assay.CHIP_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("my target");
    dataset.setStrain("yFR213");
    dataset.setStrainDescription("F56G");
    dataset.setTreatment("37C");
    dataset.setProtocol(protocolRepository.findById(1L).get());
    dataset.setSamples(new ArrayList<>());
    Sample sample1 = new Sample();
    sample1.setName("sample1");
    sample1.setReplicate("r1");
    dataset.getSamples().add(sample1);
    Sample sample2 = new Sample();
    sample2.setName("sample2");
    sample2.setReplicate("r2");
    dataset.getSamples().add(sample2);

    service.save(dataset);

    assertNotNull(dataset.getId());
    Dataset database = repository.findById(dataset.getId()).orElse(null);
    assertEquals(dataset.getName(), database.getName());
    assertEquals("my project", database.getProject());
    assertEquals(Assay.CHIP_SEQ, database.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, dataset.getType());
    assertEquals("my target", dataset.getTarget());
    assertEquals("yFR213", dataset.getStrain());
    assertEquals("F56G", dataset.getStrainDescription());
    assertEquals("37C", dataset.getTreatment());
    assertEquals((Long) 1L, database.getProtocol().getId());
    assertEquals(user.getId(), database.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(dataset.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(dataset.getDate()));
    assertEquals(2, database.getSamples().size());
    assertNotNull(database.getSamples().get(0).getId());
    assertEquals("sample1", database.getSamples().get(0).getName());
    assertEquals("r1", database.getSamples().get(0).getReplicate());
    assertNotNull(database.getSamples().get(1).getId());
    assertEquals("sample2", database.getSamples().get(1).getName());
    assertEquals("r2", database.getSamples().get(1).getReplicate());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_Update() {
    Dataset dataset = repository.findById(1L).orElse(null);
    dataset.setName("New name");
    dataset.setProject("my project");
    dataset.setAssay(Assay.CHIP_SEQ);
    dataset.setType(DatasetType.INPUT);
    dataset.setTarget("my target");
    dataset.setStrain("yFR213");
    dataset.setStrainDescription("F56G");
    dataset.setTreatment("37C");
    dataset.setProtocol(protocolRepository.findById(3L).get());
    Sample sample1 = dataset.getSamples().get(0);
    sample1.setName("sample1");
    sample1.setReplicate("r1");
    dataset.getSamples().remove(1);
    Sample sample3 = new Sample();
    sample3.setName("sample3");
    sample3.setReplicate("r3");
    dataset.getSamples().add(sample3);

    service.save(dataset);

    dataset = repository.findById(1L).orElse(null);
    assertEquals("New name", dataset.getName());
    assertEquals("my project", dataset.getProject());
    assertEquals(Assay.CHIP_SEQ, dataset.getAssay());
    assertEquals(DatasetType.INPUT, dataset.getType());
    assertEquals("my target", dataset.getTarget());
    assertEquals("yFR213", dataset.getStrain());
    assertEquals("F56G", dataset.getStrainDescription());
    assertEquals("37C", dataset.getTreatment());
    assertEquals((Long) 3L, dataset.getProtocol().getId());
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getDate());
    assertEquals(3, dataset.getSamples().size());
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals("sample1", dataset.getSamples().get(0).getName());
    assertEquals("r1", dataset.getSamples().get(0).getReplicate());
    assertEquals((Long) 3L, dataset.getSamples().get(1).getId());
    assertEquals("FR3", dataset.getSamples().get(1).getName());
    assertEquals("R3", dataset.getSamples().get(1).getReplicate());
    assertNotNull(dataset.getSamples().get(2).getId());
    assertEquals("sample3", dataset.getSamples().get(2).getName());
    assertEquals("r3", dataset.getSamples().get(2).getReplicate());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  @Transactional
  public void savePermissions() {
    Dataset dataset = repository.findById(5L).orElse(null);
    List<Laboratory> laboratories = new ArrayList<>();
    laboratories.add(laboratoryRepository.findById(2L).orElse(null));

    service.savePermissions(dataset, laboratories);

    ObjectIdentity oi = new ObjectIdentityImpl(dataset.getClass(), dataset.getId());
    Acl acl = aclService.readAclById(oi);
    assertFalse(granted(acl, BasePermission.READ, laboratoryRepository.findById(1L).orElse(null)));
    assertTrue(granted(acl, BasePermission.READ, laboratoryRepository.findById(2L).orElse(null)));
    assertFalse(granted(acl, BasePermission.READ, laboratoryRepository.findById(3L).orElse(null)));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
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
