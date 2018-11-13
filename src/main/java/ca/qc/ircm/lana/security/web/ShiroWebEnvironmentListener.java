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
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Creates Shiro's environment.
 */
public class ShiroWebEnvironmentListener extends EnvironmentLoaderListener {
  @Inject
  private SecurityConfiguration securityConfiguration;
  @Inject
  private Realm realm;

  public ShiroWebEnvironmentListener() {
  }

  protected ShiroWebEnvironmentListener(SecurityConfiguration securityConfiguration) {
    this.securityConfiguration = securityConfiguration;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    WebApplicationContext context =
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
    super.contextInitialized(sce);
  }

  @Override
  protected WebEnvironment determineWebEnvironment(ServletContext servletContext) {
    return new ShiroWebEnvironment();
  }

  @Override
  protected void customizeEnvironment(WebEnvironment environment) {
    if (!(environment instanceof ShiroWebEnvironment)) {
      throw new IllegalStateException(WebEnvironment.class.getSimpleName() + " must an instance of "
          + ShiroWebEnvironment.class.getName());
    }

    ShiroWebEnvironment customEnvironment = (ShiroWebEnvironment) environment;
    customEnvironment.setSecurityConfiguration(securityConfiguration);
    customEnvironment.setRealm(realm);
  }
}
