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

import static ca.qc.ircm.lana.security.AdminUserConfiguration.ADMIN_HASHED_PASSWORD_PROPERTY;
import static ca.qc.ircm.lana.security.AdminUserConfiguration.ADMIN_USERNAME_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class AdminUserConfigurationTest {
  @Inject
  private AdminUserConfiguration configuration;
  @Inject
  private Environment environment;
  @Inject
  private PasswordEncoder passwordEncoder;

  @Test
  public void defaultProperties() {
    assertEquals("admin@ircm.qc.ca", configuration.getName());
    assertEquals("password", configuration.getPassword());
    assertNotNull(configuration.getHashedPassword());
    assertTrue(passwordEncoder.matches("password", configuration.getHashedPassword()));
    assertEquals("admin@ircm.qc.ca", environment.getProperty(ADMIN_USERNAME_PROPERTY));
    assertNotNull(environment.getProperty(ADMIN_HASHED_PASSWORD_PROPERTY));
    assertTrue(passwordEncoder.matches("password",
        environment.getProperty(ADMIN_HASHED_PASSWORD_PROPERTY)));
  }
}
