package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link SecurityConfiguration}.
 */
@NonTransactionalTestAnnotations
public class SecurityConfigurationTest {
  @Autowired
  private SecurityConfiguration securityConfiguration;

  @Test
  public void defaultProperties() {
    assertEquals(5, securityConfiguration.lockAttemps());
    assertEquals(Duration.ofMinutes(3), securityConfiguration.lockDuration());
    assertEquals(20, securityConfiguration.disableSignAttemps());
    assertEquals("JfYMi0qUQVt8FObsZHW7", securityConfiguration.rememberMeKey());
  }
}
