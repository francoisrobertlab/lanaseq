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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import javax.inject.Inject;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class SecurityConfigurationTest {
  @Inject
  private SecurityConfiguration securityConfiguration;

  @Test
  public void defaultProperties() throws Throwable {
    assertArrayEquals(Hex.decode("2c9bff536d5bb8ae5550c93cdadace1b"),
        securityConfiguration.getCipherKeyBytes());
    assertEquals("lana", securityConfiguration.getRealmName());
    assertEquals(10, securityConfiguration.getPasswordStrength());
    assertEquals(5, securityConfiguration.getMaximumSignAttemps());
    assertEquals(300000, securityConfiguration.getMaximumSignAttempsDelay());
    assertEquals(15, securityConfiguration.getDisableSignAttemps());
  }

  @Test
  public void base64CipherKey() throws Throwable {
    securityConfiguration.setCipherKey("AcEG7RqLxcP6enoSBJKNjA==");
    assertArrayEquals(Base64.decode("AcEG7RqLxcP6enoSBJKNjA=="),
        securityConfiguration.getCipherKeyBytes());
  }
}
