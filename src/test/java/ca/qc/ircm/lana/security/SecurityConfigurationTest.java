package ca.qc.ircm.lana.security;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import java.time.Duration;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class SecurityConfigurationTest {
  @Inject
  private SecurityConfiguration securityConfiguration;

  @Test
  public void defaultValues() {
    assertEquals(5, securityConfiguration.getLockAttemps());
    assertEquals(Duration.ofMinutes(3), securityConfiguration.getLockDuration());
  }
}
