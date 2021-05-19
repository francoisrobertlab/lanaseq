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
  public void hasPermission_ReadDataset_Anonymous() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadDataset_Owner() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadDataset_NotOwner() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadDataset_Manager() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_ReadDataset_Admin() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewDataset_Anonymous() throws Throwable {
    Dataset dataset = new Dataset();
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewDataset() throws Throwable {
    Dataset dataset = new Dataset();
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteDataset_Anonymous() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteDataset_Owner() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteDataset_NotOwner() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteDataset_Manager() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteDataset_Admin() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  public void hasPermission_NullAuthentication() throws Throwable {
    Dataset dataset = datasetRepository.findById(2L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(null, dataset, READ));
    assertFalse(permissionEvaluator.hasPermission(null, dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, dataset, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(null, dataset, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, dataset.getId(), DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(null, dataset.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, dataset.getId(), DATASET_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(null, dataset.getId(), DATASET_CLASS, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Null_Anonymous() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_Null() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, DATASET_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_NotDataset() throws Throwable {
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
  public void hasPermission_NotLongId() throws Throwable {
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
