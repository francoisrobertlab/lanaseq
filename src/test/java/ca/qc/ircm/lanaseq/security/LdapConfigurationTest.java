package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link LdapConfiguration}.
 */
@NonTransactionalTestAnnotations
public class LdapConfigurationTest {
  @Autowired
  private LdapConfiguration ldapConfiguration;

  @Test
  public void defaultProperties() {
    assertTrue(ldapConfiguration.enabled());
    assertEquals("uid", ldapConfiguration.idAttribute());
    assertEquals("mail", ldapConfiguration.mailAttribute());
    assertEquals("person", ldapConfiguration.objectClass());
  }
}
