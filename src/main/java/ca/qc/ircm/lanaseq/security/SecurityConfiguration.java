package ca.qc.ircm.lanaseq.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security configuration.
 */
@ConfigurationProperties(prefix = SecurityConfiguration.PREFIX)
public record SecurityConfiguration(int lockAttemps, Duration lockDuration, int disableSignAttemps,
    String rememberMeKey) {
  public static final String PREFIX = "security";
}
