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

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
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
 * Tests for {@link SamplePermissionEvaluator}.
 */
@ServiceTestAnnotations
public class SamplePermissionEvaluatorTest {
  private static final String DATASET_CLASS = Sample.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = Permission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = Permission.WRITE;
  @Autowired
  private SamplePermissionEvaluator permissionEvaluator;
  @Autowired
  private SampleRepository repository;
  @Captor
  private ArgumentCaptor<List<Permission>> permissionsCaptor;

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_ReadSample_Anonymous() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadSample_Owner() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadSample_NotOwner() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadSample_Manager() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_ReadSample_Admin() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewSample_Anonymous() throws Throwable {
    Sample sample = new Sample("new sample");
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewSample() throws Throwable {
    Sample sample = new Sample("new sample");
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteSample_Anonymous() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteSample_Owner() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteSample_NotOwner() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteSample_Manager() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteSample_Admin() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), sample.getId(), DATASET_CLASS,
        BASE_WRITE));
  }

  @Test
  public void hasPermission_NullAuthentication() throws Throwable {
    Sample sample = repository.findById(4L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(null, sample, READ));
    assertFalse(permissionEvaluator.hasPermission(null, sample, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, sample, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(null, sample, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, sample.getId(), DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(null, sample.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, sample.getId(), DATASET_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(null, sample.getId(), DATASET_CLASS, BASE_WRITE));
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
  public void hasPermission_NotSample() throws Throwable {
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
