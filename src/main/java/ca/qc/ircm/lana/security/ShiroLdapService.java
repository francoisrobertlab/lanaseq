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

import java.text.MessageFormat;
import java.util.Collection;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Services for LDAP (active directory) based on Shiro.
 */
@Component
public class ShiroLdapService {
  private static final Logger logger = LoggerFactory.getLogger(ShiroLdapService.class);
  private final LdapConfiguration ldapConfiguration;

  public ShiroLdapService(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
  }

  private String usernameWithDomain(String username) {
    String usernameWithDomain;
    if (ldapConfiguration.getUserDnTemplate() == null
        || ldapConfiguration.getUserDnTemplate().isEmpty()) {
      usernameWithDomain = username;
    } else {
      usernameWithDomain = MessageFormat.format(ldapConfiguration.getUserDnTemplate(), username);
    }
    return usernameWithDomain;
  }

  private LdapContext createLdapContext(String username, String password)
      throws IllegalStateException, NamingException {
    String usernameWithDomain = usernameWithDomain(username);
    JndiLdapContextFactory ldapContextFactory = new JndiLdapContextFactory();
    ldapContextFactory.setUrl(ldapConfiguration.getUrl());
    logger.trace("create ldap context for user {} with url {}", usernameWithDomain,
        ldapConfiguration.getUrl());
    return ldapContextFactory.getLdapContext((Object) usernameWithDomain, password);
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
      LdapContext context = createLdapContext(username, password);
      try {
        return context != null;
      } finally {
        LdapUtils.closeContext(context);
      }
    } catch (Exception e) {
      e.printStackTrace();
      // Ignore and assume false.
      return false;
    }
  }

  /**
   * Returns user email from LDAP.<br>
   * Null is returned if user does not exists or if password is invalid.
   *
   * @param username
   *          username
   * @param password
   *          password
   * @return user email from LDAP or null if user does not exists or if password is invalid
   */
  public String getEmail(String username, String password) {
    logger.debug("Retrieving email for user [{}]", username);
    try {
      LdapContext context = createLdapContext(username, password);
      try {
        logger.debug("LDAP accessed successfully for user [{}]", username);
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Object[] searchArguments = new Object[] { username };
        NamingEnumeration<SearchResult> answer = context.search(ldapConfiguration.getBase(),
            ldapConfiguration.getUserFilter(), searchArguments, searchCtls);
        while (answer.hasMoreElements()) {
          SearchResult sr = answer.next();
          Attributes attrs = sr.getAttributes();
          if (attrs != null) {
            NamingEnumeration<? extends Attribute> ae = attrs.getAll();
            while (ae.hasMore()) {
              Attribute attr = ae.next();
              if (attr.getID().equals(ldapConfiguration.getMailAttribute())) {
                Collection<String> rawMail = LdapUtils.getAllAttributeValues(attr);
                String mail = rawMail.iterator().next();
                logger.debug("Found email {} for user [{}]", mail, username);
                return mail;
              }
            }
          }
        }
        return null;
      } finally {
        LdapUtils.closeContext(context);
      }
    } catch (Throwable e) {
      // Ignore and return null.
      return null;
    }
  }
}
