package ca.qc.ircm.lanaseq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Tests for {@link AsyncConfiguration}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AsyncConfigurationTest {

  @Autowired
  private AsyncConfigurationTestBean bean;
  @MockitoSpyBean
  private UserRepository userRepository;

  @Test
  public void testSecurityContext() {
    String email = "jonh.smith@ircm.qc.ca";
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());
    bean.run().thenAccept(e -> {
      assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
          "authentication cannot be null");
      assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());
    }).join();
  }

  @Test
  public void testUserRepository() {
    String email = "jonh.smith@ircm.qc.ca";
    User user = userRepository.findByEmail(email).orElseThrow();
    // Keep the test in case Spring Boot fixes repository usage with @Async during tests.
    // Virtual threads from Java 21 do not fix the issue.
    assertThrows(CompletionException.class, () -> {
      bean.run().thenAccept(e -> {
        assertEquals(user, userRepository.findByEmail(email).orElseThrow());
      }).join();
    });
    // Patches UserRepository because when using @Async, repositories cannot access test database.
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    bean.run().thenAccept(e -> {
      assertEquals(user, userRepository.findByEmail(email).orElseThrow());
    }).join();
    verify(userRepository, atLeast(4)).findByEmail(email);
  }
}
