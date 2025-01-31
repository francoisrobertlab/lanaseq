package ca.qc.ircm.lanaseq.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LDAP configuration.
 */
@ConfigurationProperties(prefix = LdapConfiguration.PREFIX)
public record LdapConfiguration(boolean enabled, String idAttribute, String mailAttribute,
                                String objectClass) {

  public static final String PREFIX = "ldap";
}
