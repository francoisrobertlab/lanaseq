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
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;

/**
 * Services for LDAP (active directory).
 */
@Component
public class LdapService {
  private static final Logger logger = LoggerFactory.getLogger(LdapService.class);
  private LdapOperations ldapOperations;
  private LdapConfiguration ldapConfiguration;

  @Autowired
  protected LdapService(LdapOperations ldapOperations, LdapConfiguration ldapConfiguration) {
    this.ldapOperations = ldapOperations;
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
      LdapQuery query = query().where(ldapConfiguration.idAttribute()).is(username);
      ldapOperations.authenticate(query, password);
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
    ContainerCriteria builder = query().attributes(ldapConfiguration.mailAttribute())
        .where(ldapConfiguration.idAttribute()).is(username);
    if (ldapConfiguration.objectClass() != null) {
      builder = builder.and("objectclass").is(ldapConfiguration.objectClass());
    }
    LdapQuery query = builder;
    AttributesMapper<String> mapper =
        attrs -> Optional.ofNullable(attrs.get(ldapConfiguration.mailAttribute())).map(attr -> {
          try {
            return attr.get();
          } catch (NamingException e) {
            return null;
          }
        }).map(value -> value.toString()).orElse(null);
    Optional<String> email =
        ldapOperations.search(query, mapper).stream().filter(value -> value != null).findFirst();
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
    ContainerCriteria builder = query().attributes(ldapConfiguration.idAttribute())
        .where(ldapConfiguration.mailAttribute()).is(email);
    if (ldapConfiguration.objectClass() != null) {
      builder = builder.and("objectclass").is(ldapConfiguration.objectClass());
    }
    LdapQuery query = builder;
    AttributesMapper<String> mapper =
        attrs -> Optional.ofNullable(attrs.get(ldapConfiguration.idAttribute())).map(attr -> {
          try {
            return attr.get();
          } catch (NamingException e) {
            return null;
          }
        }).map(value -> value.toString()).orElse(null);
    Optional<String> username =
        ldapOperations.search(query, mapper).stream().filter(value -> value != null).findFirst();
    logger.debug("Found LDAP username {} for user [{}]", username, email);
    return username;
  }
}
