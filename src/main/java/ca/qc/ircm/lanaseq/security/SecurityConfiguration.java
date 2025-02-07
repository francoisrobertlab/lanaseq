package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.UsedBy;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security configuration.
 */
@ConfigurationProperties(prefix = SecurityConfiguration.PREFIX)
@UsedBy(SPRING)
@SuppressWarnings("unused")
public record SecurityConfiguration(int lockAttemps, Duration lockDuration, int disableSignAttemps,
                                    String rememberMeKey) {

  public static final String PREFIX = "security";
}
