package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
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
  @Autowired
  private SampleService service;
  @MockBean
  private PermissionEvaluator permissionEvaluator;

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
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getDate());
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
}
