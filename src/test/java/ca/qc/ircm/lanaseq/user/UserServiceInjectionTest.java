package ca.qc.ircm.lanaseq.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests injection for {@link UserService}.
 */
@ServiceTestAnnotations
public class UserServiceInjectionTest {
  @Autowired
  private UserService userService;

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void get() {
    User user = userService.get(1L).orElse(null);

    assertNotNull(user);
    assertEquals((Long) 1L, user.getId());
    assertEquals("LANAseq Administrator", user.getName());
    assertEquals("lanaseq@ircm.qc.ca", user.getEmail());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2, user.getHashedPassword());
    assertEquals(1, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(4, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minus(4, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals(true, user.isAdmin());
    assertNull(user.getLocale());
  }
}
