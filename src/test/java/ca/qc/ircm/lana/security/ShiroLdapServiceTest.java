/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lana.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class ShiroLdapServiceTest {
  private ShiroLdapService ldapService;
  @Mock
  private LdapConfiguration ldapConfiguration;
  @Inject
  private LdapTemplate ldapTemplate;

  @Before
  public void beforeTest() {
    LdapContextSource contextSource = (LdapContextSource) ldapTemplate.getContextSource();
    ldapService = new ShiroLdapService(ldapConfiguration);
    when(ldapConfiguration.getUrl()).thenReturn(contextSource.getUrls()[0]);
    when(ldapConfiguration.getBase()).thenReturn("dc=mycompany,dc=com");
    when(ldapConfiguration.getUserDnTemplate()).thenReturn("uid={0},ou=people,dc=mycompany,dc=com");
    when(ldapConfiguration.getUserFilter()).thenReturn("uid={0}");
    when(ldapConfiguration.getMailAttribute()).thenReturn("mail");
  }

  @Test
  public void isPasswordValid_Username_True() {
    assertTrue(ldapService.isPasswordValid("robertf", "secret"));
  }

  @Test
  public void isPasswordValid_Username_InvalidUser() {
    assertFalse(ldapService.isPasswordValid("invalid", "secret"));
  }

  @Test
  public void isPasswordValid_Username_InvalidPassword() {
    assertFalse(ldapService.isPasswordValid("robertf", "secret2"));
  }

  @Test
  public void getEmail() {
    assertEquals("francois.robert@ircm.qc.ca", ldapService.getEmail("robertf", "secret"));
  }

  @Test
  public void getEmail_InvalidUser() {
    assertEquals(null, ldapService.getEmail("invalid", "secret"));
  }

  @Test
  public void getEmail_InvalidPassword() {
    assertEquals(null, ldapService.getEmail("robertf", "secret2"));
  }
}
