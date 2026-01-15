package ca.qc.ircm.lanaseq.message;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link MessageService}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class MessageServiceTest {

  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private MessageService service;
  @Autowired
  private MessageRepository repository;
  @MockitoBean
  private PermissionEvaluator permissionEvaluator;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
  }

  @Test
  public void get() {
    Message message = service.get(2L).orElseThrow();

    assertEquals(2L, message.getId());
    assertEquals(3L, message.getOwner().getId());
    assertEquals("Second message", message.getMessage());
    assertTrue(message.isUnread());
    assertEquals(LocalDateTime.of(2026, 1, 15, 11, 20, 0), message.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(message), eq(READ));
  }

  @Test
  public void get_0() {
    assertFalse(service.get(0).isPresent());
  }

  @Test
  public void all() {
    List<Message> messages = service.all();

    assertEquals(2, messages.size());
    assertTrue(find(messages, 1L).isPresent());
    assertTrue(find(messages, 2L).isPresent());
    for (Message message : messages) {
      verify(permissionEvaluator).hasPermission(any(), eq(message), eq(READ));
    }
  }

  @Test
  public void save_New() {
    Message message = new Message();
    message.setMessage("test");

    service.save(message);

    assertNotEquals(0, message.getId());
    message = repository.findById(message.getId()).orElseThrow();
    assertNotEquals(0, message.getId());
    assertEquals(3L, message.getOwner().getId());
    assertEquals("test", message.getMessage());
    assertTrue(message.isUnread());
    assertTrue(LocalDateTime.now().minusMinutes(1).isBefore(message.getDate()));
    assertTrue(LocalDateTime.now().plusMinutes(1).isAfter(message.getDate()));
    verify(permissionEvaluator).hasPermission(any(), eq(message), eq(WRITE));
  }

  @Test
  public void save_Update() {
    Message message = repository.findById(2L).orElseThrow();
    message.setMessage("test");
    message.setUnread(false);

    service.save(message);

    message = repository.findById(2L).orElseThrow();
    assertEquals(2L, message.getId());
    assertEquals(3L, message.getOwner().getId());
    assertEquals("test", message.getMessage());
    assertFalse(message.isUnread());
    assertEquals(LocalDateTime.of(2026, 1, 15, 11, 20, 0), message.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(message), eq(WRITE));
  }
}
