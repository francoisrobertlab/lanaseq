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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LDAP configuration.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = LdapConfiguration.PREFIX)
public class LdapConfiguration {
  public static final String PREFIX = "ldap";
  private String url;
  private String base;
  private String userDnTemplate;
  private String userFilter;
  private String mailAttribute;

  public boolean isEnabled() {
    return url != null && url.length() > 0;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public String getUserDnTemplate() {
    return userDnTemplate;
  }

  public void setUserDnTemplate(String userDnTemplate) {
    this.userDnTemplate = userDnTemplate;
  }

  public String getUserFilter() {
    return userFilter;
  }

  public void setUserFilter(String userFilter) {
    this.userFilter = userFilter;
  }

  public String getMailAttribute() {
    return mailAttribute;
  }

  public void setMailAttribute(String mailAttribute) {
    this.mailAttribute = mailAttribute;
  }
}
