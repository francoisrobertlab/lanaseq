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

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.Assert.assertEquals;
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
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    assertEquals("polymerase", dataset.getProject());
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
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Dataset dataset = new Dataset();
    dataset.setProject("my project");
    dataset.setSamples(new ArrayList<>());
    Sample sample1 = new Sample();
    sample1.setSampleId("sample1");
    sample1.setReplicate("r1");
    sample1.setAssay(Assay.CHIP_SEQ);
    sample1.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample1.setTarget("my target");
    sample1.setStrain("yFR213");
    sample1.setStrainDescription("F56G");
    sample1.setTreatment("37C");
    sample1.setProtocol(protocolRepository.findById(1L).get());
    dataset.getSamples().add(sample1);
    Sample sample2 = new Sample();
    sample2.setSampleId("sample2");
    sample2.setReplicate("r2");
    sample2.setAssay(Assay.CHIP_SEQ);
    sample2.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample2.setTarget("my target");
    sample2.setStrain("yFR213");
    sample2.setStrainDescription("F56G");
    sample2.setTreatment("37C");
    sample2.setProtocol(protocolRepository.findById(1L).get());
    dataset.getSamples().add(sample2);

    service.save(dataset);

    repository.flush();
    assertNotNull(dataset.getId());
    dataset = repository.findById(dataset.getId()).orElse(null);
    assertEquals("my project", dataset.getProject());
    assertEquals(user.getId(), dataset.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(dataset.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(dataset.getDate()));
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertNotNull(sample.getId());
    assertEquals("sample1", sample.getSampleId());
    assertEquals("r1", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    assertEquals("sample1_ChIPSeq_IP_mytarget_yFR213_F56G_37C_r1_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(sample.getDate()), sample.getName());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(user.getId(), sample.getOwner().getId());
    sample = dataset.getSamples().get(1);
    assertNotNull(sample.getId());
    assertEquals("sample2", sample.getSampleId());
    assertEquals("r2", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    assertEquals("sample2_ChIPSeq_IP_mytarget_yFR213_F56G_37C_r2_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(sample.getDate()), sample.getName());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(user.getId(), sample.getOwner().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_Update() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Dataset dataset = repository.findById(1L).orElse(null);
    dataset.setProject("my project");
    Sample sample1 = dataset.getSamples().get(0);
    sample1.setSampleId("sample1");
    sample1.setReplicate("r1");
    sample1.setAssay(Assay.CHIP_SEQ);
    sample1.setType(DatasetType.INPUT);
    sample1.setTarget("my target");
    sample1.setStrain("yFR213");
    sample1.setStrainDescription("F56G");
    sample1.setTreatment("37C");
    sample1.setProtocol(protocolRepository.findById(3L).get());
    dataset.getSamples().remove(1);
    Sample sample3 = new Sample();
    sample3.setSampleId("sample4");
    sample3.setReplicate("r4");
    sample3.setAssay(Assay.CHIP_SEQ);
    sample3.setType(DatasetType.INPUT);
    sample3.setTarget("my target");
    sample3.setStrain("yFR213");
    sample3.setStrainDescription("F56G");
    sample3.setTreatment("37C");
    sample3.setProtocol(protocolRepository.findById(3L).get());
    dataset.getSamples().add(sample3);

    service.save(dataset);

    repository.flush();
    dataset = repository.findById(1L).orElse(null);
    assertEquals("my project", dataset.getProject());
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getDate());
    assertEquals(3, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals((Long) 1L, sample.getId());
    assertEquals("sample1", sample.getSampleId());
    assertEquals("r1", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(DatasetType.INPUT, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getDate());
    assertEquals("sample1_ChIPSeq_Input_mytarget_yFR213_F56G_37C_r1_20181020", sample.getName());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    sample = dataset.getSamples().get(1);
    assertEquals((Long) 3L, sample.getId());
    assertEquals("FR3", sample.getSampleId());
    assertEquals("R3", sample.getReplicate());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 30, 23), sample.getDate());
    assertEquals("FR3_MNaseSeq_IP_polr2a_yFR100_WT_Rappa_R3_20181020", sample.getName());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    sample = dataset.getSamples().get(2);
    assertNotNull(sample.getId());
    assertEquals("sample4", sample.getSampleId());
    assertEquals("r4", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(DatasetType.INPUT, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    assertEquals("sample4_ChIPSeq_Input_mytarget_yFR213_F56G_37C_r4_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(sample.getDate()), sample.getName());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }
}
