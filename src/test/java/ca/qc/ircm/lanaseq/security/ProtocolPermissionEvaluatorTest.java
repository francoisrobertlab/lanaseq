package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
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
 * Tests for {@link ProtocolPermissionEvaluator}.
 */
@ServiceTestAnnotations
public class ProtocolPermissionEvaluatorTest {
  private static final String PROTOCOL_CLASS = Protocol.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = Permission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = Permission.WRITE;
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
  public void hasPermission_ReadProtocol_Anonymous() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadProtocol_Owner() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadProtocol_NotOwner() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadProtocol_Manager() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_ReadProtocol_Admin() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewProtocol_Anonymous() {
    Protocol protocol = new Protocol("new protocol");
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewProtocol() {
    Protocol protocol = new Protocol("new protocol");
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteProtocol_Anonymous() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteProtocol_Owner() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteProtocol_NotOwner() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), protocol.getId(),
        PROTOCOL_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteProtocol_Manager() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteProtocol_Admin() {
    Protocol protocol = protocolRepository.findById(1L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol, BASE_WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), protocol.getId(), PROTOCOL_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_NotProtocol() {
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
        permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS,
        BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", PROTOCOL_CLASS,
        BASE_WRITE));
  }
}
