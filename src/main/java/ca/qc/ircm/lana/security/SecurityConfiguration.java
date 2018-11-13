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

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = SecurityConfiguration.PREFIX)
public class SecurityConfiguration {
  public static final String PREFIX = "security";
  private static final String HEX_BEGIN_TOKEN = "0x";
  private String cipherKey;
  private int passwordStrength;
  private int maximumSignAttemps;
  private long maximumSignAttempsDelay;
  private int disableSignAttemps;
  private String realmName;

  public String getRealmName() {
    return realmName;
  }

  public String getCipherKey() {
    return cipherKey;
  }

  public void setCipherKey(String cipherKey) {
    this.cipherKey = cipherKey;
  }

  /**
   * Returns cipher key as a byte array.
   *
   * @return cipher key as a byte array
   */
  public byte[] getCipherKeyBytes() {
    if (cipherKey.startsWith(HEX_BEGIN_TOKEN)) {
      return Hex.decode(cipherKey.substring(HEX_BEGIN_TOKEN.length()));
    } else {
      return Base64.decode(cipherKey);
    }
  }

  public int getPasswordStrength() {
    return passwordStrength;
  }

  public void setPasswordStrength(int passwordStrength) {
    this.passwordStrength = passwordStrength;
  }

  public int getMaximumSignAttemps() {
    return maximumSignAttemps;
  }

  public void setMaximumSignAttemps(int maximumSignAttemps) {
    this.maximumSignAttemps = maximumSignAttemps;
  }

  public long getMaximumSignAttempsDelay() {
    return maximumSignAttempsDelay;
  }

  public void setMaximumSignAttempsDelay(long maximumSignAttempsDelay) {
    this.maximumSignAttempsDelay = maximumSignAttempsDelay;
  }

  public int getDisableSignAttemps() {
    return disableSignAttemps;
  }

  public void setDisableSignAttemps(int disableSignAttemps) {
    this.disableSignAttemps = disableSignAttemps;
  }

  public void setRealmName(String realmName) {
    this.realmName = realmName;
  }
}
