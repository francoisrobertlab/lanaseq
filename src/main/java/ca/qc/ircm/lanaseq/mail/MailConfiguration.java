package ca.qc.ircm.lanaseq.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mail configuration.
 */
@ConfigurationProperties(prefix = MailConfiguration.PREFIX)
public record MailConfiguration(boolean enabled, String from, String to, String subject) {

  public static final String PREFIX = "mail";
}
