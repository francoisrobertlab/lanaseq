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

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link SecurityConfiguration}.
 */
@NonTransactionalTestAnnotations
public class SecurityConfigurationTest {
  @Autowired
  private SecurityConfiguration securityConfiguration;

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals(5, securityConfiguration.getLockAttemps());
    assertEquals(Duration.ofMinutes(3), securityConfiguration.getLockDuration());
    assertEquals(20, securityConfiguration.getDisableSignAttemps());
    assertEquals("JfYMi0qUQVt8FObsZHW7", securityConfiguration.getRememberMeKey());
  }
}
