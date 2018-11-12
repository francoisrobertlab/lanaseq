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
import javax.inject.Inject;
import org.apache.shiro.mgt.AbstractRememberMeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Configures Shiro's cypher key.
 */
@Component
public class CypherKeyConfigurer {
  private static final Logger logger = LoggerFactory.getLogger(CypherKeyConfigurer.class);
  @Inject
  private AbstractRememberMeManager rememberMeManager;
  @Inject
  private SecurityConfiguration securityConfiguration;

  @EventListener({ ContextRefreshedEvent.class })
  void setCipherKey() {
    logger.debug("Set cipher key {} in remember-me manager", securityConfiguration.getCipherKey());
    rememberMeManager.setCipherKey(securityConfiguration.getCipherKeyBytes());
  }
}
