package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.message.Message;
import ca.qc.ircm.lanaseq.message.MessageRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link MessagePermissionEvaluator}.
 */
@ServiceTestAnnotations
public class MessagePermissionEvaluatorTest {

  private static final String MESSAGE_CLASS = Message.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = Permission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = Permission.WRITE;
  @Autowired
  private MessagePermissionEvaluator permissionEvaluator;
  @Autowired
  private MessageRepository messageRepository;

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_ReadMessage_Anonymous() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadMessage_Owner() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadMessage_NotOwner() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadMessage_Manager() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_READ));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_ReadMessage_Admin() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, BASE_READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewMessage_Anonymous() {
    Message message = new Message();
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewMessage() {
    Message message = new Message();
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteMessage_Anonymous() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteMessage_Owner() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteMessage_NotOwner() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteMessage_Manager() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteMessage_Admin() {
    Message message = messageRepository.findById(2L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), message.getId(), MESSAGE_CLASS,
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_NotMessage() {
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
        permissionEvaluator.hasPermission(authentication(), "Informatics", MESSAGE_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "Informatics", MESSAGE_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", MESSAGE_CLASS,
        BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "Informatics", MESSAGE_CLASS,
        BASE_WRITE));
  }
}
