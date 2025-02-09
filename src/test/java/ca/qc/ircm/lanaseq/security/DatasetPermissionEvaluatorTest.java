package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link DatasetPermissionEvaluator}.
 */
@ServiceTestAnnotations
public class DatasetPermissionEvaluatorTest {

  private static final String DATASET_CLASS = Dataset.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = Permission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = Permission.WRITE;
  @Autowired
  private DatasetPermissionEvaluator permissionEvaluator;
  @Autowired
  private DatasetRepository datasetRepository;
  @Captor
  private ArgumentCaptor<List<Permission>> permissionsCaptor;

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_ReadDataset_Anonymous() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadDataset_Owner() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadDataset_NotOwner() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadDataset_Manager() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_ReadDataset_Admin() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewDataset_Anonymous() {
    Dataset dataset = new Dataset();
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewDataset() {
    Dataset dataset = new Dataset();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteDataset_Anonymous() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteDataset_Owner() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteDataset_NotOwner() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteDataset_Manager() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteDataset_Admin() {
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_NotDataset() {
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
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_NotLongId() {
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "Informatics", DATASET_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "Informatics", DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", DATASET_CLASS,
        BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", DATASET_CLASS,
        BASE_WRITE));
  }
}
