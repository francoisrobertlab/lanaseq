package ca.qc.ircm.lanaseq.mail;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.UsedBy;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mail configuration.
 */
@ConfigurationProperties(prefix = MailConfiguration.PREFIX)
@UsedBy(SPRING)
@SuppressWarnings("unused")
public record MailConfiguration(boolean enabled, String from, String to, String subject) {

  public static final String PREFIX = "mail";
}
