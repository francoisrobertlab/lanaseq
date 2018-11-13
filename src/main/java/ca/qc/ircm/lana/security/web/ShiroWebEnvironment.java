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

package ca.qc.ircm.lana.security.web;

import ca.qc.ircm.lana.security.SecurityConfiguration;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.AbstractRememberMeManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom {@link WebEnvironment} for Shiro.
 */
public class ShiroWebEnvironment extends DefaultWebEnvironment implements Initializable {
  private static final Logger logger = LoggerFactory.getLogger(ShiroWebEnvironment.class);
  private SecurityConfiguration securityConfiguration;
  private Realm realm;

  @Override
  public void init() {
    DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
    manager.setCacheManager(new MemoryConstrainedCacheManager());
    logger.debug("Set realm {} in web environment", realm);
    manager.setRealm(realm);
    AbstractRememberMeManager rememberMeManager =
        (AbstractRememberMeManager) manager.getRememberMeManager();
    rememberMeManager.setCipherKey(securityConfiguration.getCipherKeyBytes());
    setWebSecurityManager(manager);
  }

  public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
    this.securityConfiguration = securityConfiguration;
  }

  public void setRealm(Realm realm) {
    this.realm = realm;
  }
}
