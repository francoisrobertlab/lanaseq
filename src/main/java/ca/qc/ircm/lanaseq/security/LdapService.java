package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.FindbugsExplanations.SPRING_BOOT_EI_EXPOSE_REP;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
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
  private final LdapOperations ldapOperations;
  private final LdapConfiguration ldapConfiguration;

  @Autowired
  @SuppressFBWarnings(value = {"EI_EXPOSE_REP2"}, justification = SPRING_BOOT_EI_EXPOSE_REP)
  protected LdapService(LdapOperations ldapOperations, LdapConfiguration ldapConfiguration) {
    this.ldapOperations = ldapOperations;
    this.ldapConfiguration = ldapConfiguration;
  }

  /**
   * Returns true if user exists in LDAP and password is valid, false otherwise.
   *
   * @param username username
   * @param password password
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
   * @param username username
   * @return user's email from LDAP or null if user does not exists
   */
  public Optional<String> getEmail(String username) {
    ContainerCriteria builder = query().attributes(ldapConfiguration.mailAttribute())
        .where(ldapConfiguration.idAttribute()).is(username);
    builder = builder.and("objectclass").is(ldapConfiguration.objectClass());
    LdapQuery query = builder;
    AttributesMapper<String> mapper = attrs -> Optional.ofNullable(
        attrs.get(ldapConfiguration.mailAttribute())).map(attr -> {
      try {
        return attr.get();
      } catch (NamingException e) {
        return null;
      }
    }).map(Object::toString).orElse(null);
    Optional<String> email = ldapOperations.search(query, mapper).stream().filter(Objects::nonNull)
        .findFirst();
    logger.debug("Found LDAP email {} for user [{}]", email, username);
    return email;
  }

  /**
   * Returns user's username on LDAP.
   *
   * @param email user's email
   * @return user's username on LDAP or null if user does not exists
   */
  public Optional<String> getUsername(String email) {
    ContainerCriteria builder = query().attributes(ldapConfiguration.idAttribute())
        .where(ldapConfiguration.mailAttribute()).is(email);
    builder = builder.and("objectclass").is(ldapConfiguration.objectClass());
    LdapQuery query = builder;
    AttributesMapper<String> mapper = attrs -> Optional.ofNullable(
        attrs.get(ldapConfiguration.idAttribute())).map(attr -> {
      try {
        return attr.get();
      } catch (NamingException e) {
        return null;
      }
    }).map(Object::toString).orElse(null);
    Optional<String> username = ldapOperations.search(query, mapper).stream()
        .filter(Objects::nonNull).findFirst();
    logger.debug("Found LDAP username {} for user [{}]", username, email);
    return username;
  }
}
