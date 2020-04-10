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

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Security configuration.
 */
@Configuration
public class AclConfiguration {
  @Autowired
  private DataSource dataSource;
  @Autowired
  private CacheManager cacheManager;

  /**
   * Returns {@link AclService} instance.
   *
   * @return {@link AclService} instance
   */
  @Bean
  public JdbcMutableAclService aclService() {
    JdbcMutableAclService aclService =
        new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
    aclService.setClassIdentityQuery("SELECT LAST_INSERT_ID()");
    aclService.setSidIdentityQuery("SELECT LAST_INSERT_ID()");
    return aclService;
  }

  @Bean
  public LookupStrategy lookupStrategy() {
    return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(),
        new ConsoleAuditLogger());
  }

  @Bean
  public AclAuthorizationStrategy aclAuthorizationStrategy() {
    return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority(UserRole.ADMIN));
  }

  @Bean
  public SpringCacheBasedAclCache aclCache() {
    return new SpringCacheBasedAclCache(cacheManager.getCache("acl"), permissionGrantingStrategy(),
        aclAuthorizationStrategy());
  }

  @Bean
  public PermissionGrantingStrategy permissionGrantingStrategy() {
    return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
  }
}
