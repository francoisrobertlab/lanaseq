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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class PermissionEvaluatorDelegatorTest {
  private static final String LABORATORY_CLASS = Laboratory.class.getName();
  private static final String USER_CLASS = User.class.getName();
  private static final String EXPERIMENT_CLASS = Experiment.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = BasePermission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = BasePermission.WRITE;
  @Autowired
  private PermissionEvaluatorDelegator permissionEvaluator;
  @MockBean
  private LaboratoryPermissionEvaluator laboratoryPermissionEvaluator;
  @MockBean
  private UserPermissionEvaluator userPermissionEvaluator;
  @MockBean
  private ExperimentPermissionEvaluator experimentPermissionEvaluator;
  @Mock
  private Laboratory laboratory;
  @Mock
  private User user;
  @Mock
  private Experiment experiment;

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Laboratory_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_WRITE));
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, BASE_READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, WRITE);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, BASE_WRITE);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, WRITE);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Laboratory_True() throws Throwable {
    when(laboratoryPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(laboratoryPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_WRITE));
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, BASE_READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, WRITE);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory, BASE_WRITE);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_READ);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, WRITE);
    verify(laboratoryPermissionEvaluator).hasPermission(authentication(), laboratory.getId(),
        LABORATORY_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_User_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
    verify(userPermissionEvaluator).hasPermission(authentication(), user, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_User_True() throws Throwable {
    when(userPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
    verify(userPermissionEvaluator).hasPermission(authentication(), user, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user, BASE_WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS, READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_READ);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        WRITE);
    verify(userPermissionEvaluator).hasPermission(authentication(), user.getId(), USER_CLASS,
        BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Experiment_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, BASE_READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, WRITE);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, BASE_WRITE);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Experiment_True() throws Throwable {
    when(experimentPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(experimentPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE));
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, BASE_READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, WRITE);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment, BASE_WRITE);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_READ);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, WRITE);
    verify(experimentPermissionEvaluator).hasPermission(authentication(), experiment.getId(),
        EXPERIMENT_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Other() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "test", BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(), READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(), BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(), WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), 1L, String.class.getName(),
        BASE_WRITE));
  }
}
