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

package ca.qc.ircm.lanaseq.security;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Optional;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;

/**
 * Services for LDAP (active directory).
 */
@Component
public class LdapService {
  private static final Logger logger = LoggerFactory.getLogger(LdapService.class);
  @Autowired
  private LdapTemplate ldapTemplate;
  @Autowired
  private LdapConfiguration ldapConfiguration;

  protected LdapService() {
  }

  public LdapService(LdapTemplate ldapTemplate, LdapConfiguration ldapConfiguration) {
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
  public boolean isPasswordValid(String username, String password) {
    try {
      LdapQuery query = query().where(ldapConfiguration.getIdAttribute()).is(username);
      ldapTemplate.authenticate(query, password);
      logger.debug("Valid LDAP password for user [{}]", username);
      return true;
    } catch (Exception e) {
      logger.debug("Invalid LDAP password for user [{}]", username);
      return false;
    }
  }

  /**
   * Returns user's email from LDAP.
   *
   * @param username
   *          username
   * @return user's email from LDAP or null if user does not exists
   */
  public Optional<String> getEmail(String username) {
    ContainerCriteria builder = query().attributes(ldapConfiguration.getMailAttribute())
        .where(ldapConfiguration.getIdAttribute()).is(username);
    if (ldapConfiguration.getObjectClass() != null) {
      builder = builder.and("objectclass").is(ldapConfiguration.getObjectClass());
    }
    LdapQuery query = builder;
    AttributesMapper<String> mapper =
        attrs -> Optional.ofNullable(attrs.get(ldapConfiguration.getMailAttribute())).map(attr -> {
          try {
            return attr.get();
          } catch (NamingException e) {
            return null;
          }
        }).map(value -> value.toString()).orElse(null);
    Optional<String> email =
        ldapTemplate.search(query, mapper).stream().filter(value -> value != null).findFirst();
    logger.debug("Found LDAP email {} for user [{}]", email, username);
    return email;
  }

  /**
   * Returns user's username on LDAP.
   *
   * @param email
   *          user's email
   * @return user's username on LDAP or null if user does not exists
   */
  public Optional<String> getUsername(String email) {
    ContainerCriteria builder = query().attributes(ldapConfiguration.getIdAttribute())
        .where(ldapConfiguration.getMailAttribute()).is(email);
    if (ldapConfiguration.getObjectClass() != null) {
      builder = builder.and("objectclass").is(ldapConfiguration.getObjectClass());
    }
    LdapQuery query = builder;
    AttributesMapper<String> mapper =
        attrs -> Optional.ofNullable(attrs.get(ldapConfiguration.getIdAttribute())).map(attr -> {
          try {
            return attr.get();
          } catch (NamingException e) {
            return null;
          }
        }).map(value -> value.toString()).orElse(null);
    Optional<String> username =
        ldapTemplate.search(query, mapper).stream().filter(value -> value != null).findFirst();
    logger.debug("Found LDAP username {} for user [{}]", username, email);
    return username;
  }
}
