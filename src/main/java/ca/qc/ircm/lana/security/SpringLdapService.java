/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Optional;
import javax.inject.Inject;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;

/**
 * Services for LDAP (active directory) based on Spring.
 */
@Component
public class SpringLdapService implements LdapService {
  private static final Logger logger = LoggerFactory.getLogger(SpringLdapService.class);
  @Inject
  private LdapTemplate ldapTemplate;
  @Inject
  private LdapConfiguration ldapConfiguration;

  protected SpringLdapService() {
  }

  public SpringLdapService(LdapTemplate ldapTemplate, LdapConfiguration ldapConfiguration) {
    this.ldapTemplate = ldapTemplate;
    this.ldapConfiguration = ldapConfiguration;
  }

  /**
   * Returns true if user exists in LDAP and password is valid, false otherwise.
   *
   * @param username
   *          username
   * @param password
   *          password
   * @return true if user exists in LDAP and password is valid, false otherwise
   */
  @Override
  public boolean isPasswordValid(String username, String password) {
    try {
      LdapQuery query = query().where(ldapConfiguration.getIdAttribute()).is(username);
      ldapTemplate.authenticate(query, password);
      logger.debug("Valid LDAP password for user [{}]", username);
      return true;
    } catch (Exception e) {
      logger.debug("Invalid LDAP password for user [{}]", username, e);
      return false;
    }
  }

  @Override
  public String getEmail(String username) {
    LdapQuery query = query().attributes(ldapConfiguration.getMailAttribute())
        .where(ldapConfiguration.getIdAttribute()).is(username);
    AttributesMapper<String> mapper =
        attrs -> Optional.ofNullable(attrs.get(ldapConfiguration.getMailAttribute())).map(attr -> {
          try {
            return attr.get();
          } catch (NamingException e) {
            return null;
          }
        }).map(value -> value.toString()).orElse(null);
    String email = ldapTemplate.search(query, mapper).stream().findFirst().orElse(null);
    logger.debug("Found LDAP email {} for user [{}]", username, email);
    return email;
  }

  @Override
  public String getUsername(String email) {
    LdapQuery query = query().attributes(ldapConfiguration.getIdAttribute())
        .where(ldapConfiguration.getMailAttribute()).is(email);
    AttributesMapper<String> mapper =
        attrs -> Optional.ofNullable(attrs.get(ldapConfiguration.getIdAttribute())).map(attr -> {
          try {
            return attr.get();
          } catch (NamingException e) {
            return null;
          }
        }).map(value -> value.toString()).orElse(null);
    String username = ldapTemplate.search(query, mapper).stream().findFirst().orElse(null);
    logger.debug("Found LDAP username {} for user [{}]", username, email);
    return username;
  }
}
