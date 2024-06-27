package ca.qc.ircm.lanaseq.test.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.util.TestSocketUtils;

/**
 * Configures a random port for SMTP.
 */
public class SmtpPortRandomizer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    int randomPort = TestSocketUtils.findAvailableTcpPort();
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
        "spring.mail.port=" + randomPort);
  }
}
