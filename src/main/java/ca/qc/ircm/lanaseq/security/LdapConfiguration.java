package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.UsedBy;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LDAP configuration.
 */
@ConfigurationProperties(prefix = LdapConfiguration.PREFIX)
@UsedBy(SPRING)
@SuppressWarnings("unused")
public record LdapConfiguration(boolean enabled, String idAttribute, String mailAttribute,
                                String objectClass) {

  public static final String PREFIX = "ldap";
}
