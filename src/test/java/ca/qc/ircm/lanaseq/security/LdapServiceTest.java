package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

/**
 * Tests for {@link LdapService}.
 */
@ServiceTestAnnotations
public class LdapServiceTest {
  @Autowired
  private LdapService ldapService;
  @Autowired
  private LdapTemplate ldapTemplate;
  @Autowired
  private LdapConfiguration ldapConfiguration;

  @Test
  public void isPasswordValid_True() {
    assertTrue(ldapService.isPasswordValid("robertf", "secret"));
  }

  @Test
  public void isPasswordValid_InvalidUser() {
    assertFalse(ldapService.isPasswordValid("invalid", "secret"));
  }

  @Test
  public void isPasswordValid_InvalidPassword() {
    assertFalse(ldapService.isPasswordValid("robertf", "secret2"));
  }

  @Test
  public void getEmail() {
    assertEquals("francois.robert@ircm.qc.ca", ldapService.getEmail("robertf").orElse(null));
  }

  @Test
  public void getEmail_Invalid() {
    assertFalse(ldapService.getEmail("invalid").isPresent());
  }

  @Test
  public void getEmail_NullElementSearch() {
    LdapTemplate ldapTemplate = mock(LdapTemplate.class);
    when(ldapTemplate.search(any(LdapQuery.class), any(AttributesMapper.class)))
        .thenReturn(Collections.nCopies(1, (String) null));
    ldapService = new LdapService(ldapTemplate, ldapConfiguration);

    assertFalse(ldapService.getEmail("robertf").isPresent());
  }

  @Test
  public void getUsername() {
    assertEquals("robertf", ldapService.getUsername("francois.robert@ircm.qc.ca").orElse(null));
  }

  @Test
  public void getUsername_Invalid() {
    assertFalse(ldapService.getUsername("not.used@ircm.qc.ca").isPresent());
  }

  @Test
  public void getUsername_NullElementSearch() {
    LdapTemplate ldapTemplate = mock(LdapTemplate.class);
    when(ldapTemplate.search(any(LdapQuery.class), any(AttributesMapper.class)))
        .thenReturn(Collections.nCopies(1, (String) null));
    ldapService = new LdapService(ldapTemplate, ldapConfiguration);

    assertFalse(ldapService.getUsername("francois.robert@ircm.qc.ca").isPresent());
  }
}
