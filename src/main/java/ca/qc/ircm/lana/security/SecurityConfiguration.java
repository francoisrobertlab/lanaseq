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

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * Security configuration.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = SecurityConfiguration.PREFIX)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration {
  public static final String PREFIX = "security";
  private int lockAttemps;
  private Duration lockDuration;
  private int disableSignAttemps;
  private String rememberMeKey;

  public int getLockAttemps() {
    return lockAttemps;
  }

  public void setLockAttemps(int lockAttemps) {
    this.lockAttemps = lockAttemps;
  }

  public Duration getLockDuration() {
    return lockDuration;
  }

  public void setLockDuration(Duration lockDuration) {
    this.lockDuration = lockDuration;
  }

  public int getDisableSignAttemps() {
    return disableSignAttemps;
  }

  public void setDisableSignAttemps(int disableSignAttemps) {
    this.disableSignAttemps = disableSignAttemps;
  }

  public String getRememberMeKey() {
    return rememberMeKey;
  }

  public void setRememberMeKey(String rememberMeKey) {
    this.rememberMeKey = rememberMeKey;
  }
}
