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
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class LdapConfigurationTest {
  @Inject
  private LdapConfiguration ldapConfiguration;
  @Inject
  private LdapTemplate ldapTemplate;

  @Test
  public void defaultProperties() throws Throwable {
    LdapContextSource contextSource = (LdapContextSource) ldapTemplate.getContextSource();
    ldapConfiguration.setUrl(contextSource.getUrls()[0]);
    assertTrue(ldapConfiguration.isEnabled());
    assertEquals(contextSource.getUrls()[0], ldapConfiguration.getUrl());
    assertEquals("uid={0},ou=people,dc=mycompany,dc=com", ldapConfiguration.getUserDnTemplate());
    assertEquals("dc=mycompany,dc=com", ldapConfiguration.getBase());
    assertEquals("uid={0}", ldapConfiguration.getUserFilter());
    assertEquals("uid", ldapConfiguration.getIdAttribute());
    assertEquals("mail", ldapConfiguration.getMailAttribute());
  }
}
