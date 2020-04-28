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

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ProtocolPermissionEvaluatorTest {
  private static final String PROTOCOL_CLASS = Protocol.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = BasePermission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = BasePermission.WRITE;
  @Autowired
  private ProtocolPermissionEvaluator permissionEvaluator;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Captor
  private ArgumentCaptor<List<Permission>> permissionsCaptor;

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_ReadProtocol_Anonymous() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadProtocol_Owner() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadProtocol_OtherLabMember() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_ReadProtocol_NotOwner() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadProtocol_Manager() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void hasPermission_ReadProtocol_ManagerOtherLab() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_ReadProtocol_Admin() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewProtocol_Anonymous() throws Throwable {
    Protocol protocol = new Protocol("new protocol");
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewProtocol() throws Throwable {
    Protocol protocol = new Protocol("new protocol");
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteProtocol_Anonymous() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteProtocol_Owner() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteProtocol_OtherLabMember() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("christian.poitras@ircm.qc.ca")
  public void hasPermission_WriteProtocol_NotOwner() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteProtocol_Manager() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void hasPermission_WriteProtocol_ManagerOtherLab() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_WriteProtocol_Admin() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
  }

  @Test
  public void hasPermission_NullAuthentication() throws Throwable {
    Protocol protocol = protocolRepository.findById(1L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(null, protocol, READ));
    assertFalse(permissionEvaluator.hasPermission(null, protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, protocol, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(null, protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, protocol.getId(), PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(null, protocol.getId(), PROTOCOL_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(null, protocol.getId(), PROTOCOL_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(null, protocol.getId(), PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Null_Anonymous() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_Null() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), null, PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_NotProtocol() throws Throwable {
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
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_NotLongId() throws Throwable {
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS,
        BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS,
        BASE_WRITE));
  }
}
