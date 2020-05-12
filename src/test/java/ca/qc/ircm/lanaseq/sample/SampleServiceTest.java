package ca.qc.ircm.lanaseq.sample;

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

import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.time.LocalDateTime;
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
public class SampleServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private SampleService service;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @MockBean
  private AuthorizationService authorizationService;

  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
  }

  @Test
  @WithMockUser
  public void get() {
    Sample sample = service.get(1L);

    assertEquals((Long) 1L, sample.getId());
    assertEquals("FR1", sample.getName());
    assertEquals("R1", sample.getReplicate());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getDate());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    assertEquals((Long) 1L, sample.getDataset().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Null() {
    Sample sample = service.get(null);
    assertNull(sample);
  }

  @Test
  @WithMockUser
  public void exists_True() {
    Dataset dataset = new Dataset(1L);
    assertTrue(service.exists("FR1", dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void exists_False() {
    Dataset dataset = new Dataset(1L);
    assertFalse(service.exists("FR4", dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void exists_NameNull() {
    Dataset dataset = new Dataset(1L);
    assertFalse(service.exists(null, dataset));
  }

  @Test
  @WithMockUser
  public void exists_DatasetNull() {
    assertFalse(service.exists("FR1", null));
  }

  @Test
  @WithMockUser
  public void existsReplicate_True() {
    Dataset dataset = new Dataset(1L);
    assertTrue(service.existsReplicate("R1", dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void existsReplicate_False() {
    Dataset dataset = new Dataset(1L);
    assertFalse(service.existsReplicate("R4", dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void existsReplicate_NameNull() {
    Dataset dataset = new Dataset(1L);
    assertFalse(service.existsReplicate(null, dataset));
  }

  @Test
  @WithMockUser
  public void existsReplicate_DatasetNull() {
    assertFalse(service.existsReplicate("FR1", null));
  }

  @Test
  @WithMockUser
  public void all() {
    Dataset dataset = new Dataset(1L);

    List<Sample> samples = service.all(dataset);

    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void all_Null() {
    List<Sample> samples = service.all(null);
    assertTrue(samples.isEmpty());
  }

  @Test
  @WithMockUser
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Sample sample = new Sample();
    sample.setName("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setProtocol(protocolRepository.findById(1L).get());
    sample.setDataset(datasetRepository.findById(1L).get());

    service.save(sample);

    repository.flush();
    assertNotNull(sample.getId());
    sample = repository.findById(sample.getId()).orElse(null);
    assertEquals("my sample", sample.getName());
    assertEquals("my replicate", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals((Long) 1L, sample.getDataset().getId());
    assertEquals(user.getId(), sample.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_Update() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Sample sample = repository.findById(1L).orElse(null);
    sample.setName("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(DatasetType.INPUT);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setProtocol(protocolRepository.findById(3L).get());

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElse(null);
    assertEquals("my sample", sample.getName());
    assertEquals("my replicate", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(DatasetType.INPUT, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    assertEquals((Long) 1L, sample.getDataset().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }
}
