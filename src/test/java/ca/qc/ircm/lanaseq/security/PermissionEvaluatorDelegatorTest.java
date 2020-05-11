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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class PermissionEvaluatorDelegatorTest {
  private static final String LABORATORY_CLASS = Laboratory.class.getName();
  private static final String USER_CLASS = User.class.getName();
  private static final String DATASET_CLASS = Dataset.class.getName();
  private static final String PROTOCOL_CLASS = Protocol.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = Permission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = Permission.WRITE;
  @Autowired
  private PermissionEvaluatorDelegator permissionEvaluator;
  @MockBean
  private LaboratoryPermissionEvaluator laboratoryPermissionEvaluator;
  @MockBean
  private UserPermissionEvaluator userPermissionEvaluator;
  @MockBean
  private DatasetPermissionEvaluator datasetPermissionEvaluator;
  @MockBean
  private ProtocolPermissionEvaluator protocolPermissionEvaluator;
  @Mock
  private Laboratory laboratory;
  @Mock
  private User user;
  @Mock
  private Dataset dataset;
  @Mock
  private Protocol protocol;

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
  public void hasPermission_Dataset_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Dataset_True() throws Throwable {
    when(datasetPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(datasetPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), dataset.getId(), DATASET_CLASS,
        BASE_WRITE));
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset, BASE_WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_READ);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, WRITE);
    verify(datasetPermissionEvaluator).hasPermission(authentication(), dataset.getId(),
        DATASET_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Protocol_False() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE);
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Protocol_True() throws Throwable {
    when(protocolPermissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(protocolPermissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol, BASE_WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE);
    verify(protocolPermissionEvaluator).hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE);
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
