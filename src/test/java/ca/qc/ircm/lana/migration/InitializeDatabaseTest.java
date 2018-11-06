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

package ca.qc.ircm.lana.migration;

import static ca.qc.ircm.lana.security.AdminUserConfiguration.ADMIN_HASHED_PASSWORD_PROPERTY;
import static ca.qc.ircm.lana.security.AdminUserConfiguration.ADMIN_ID;
import static ca.qc.ircm.lana.security.AdminUserConfiguration.ADMIN_USERNAME_PROPERTY;
import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class InitializeDatabaseTest {
  private InitializeDatabase initializeDatabase = new InitializeDatabase();
  @Mock
  private MongoTemplate mongoTemplate;
  @Mock
  private Environment environment;
  @Captor
  private ArgumentCaptor<User> userCaptor;
  private String username = "admin_test@ircm.qc.ca";
  private String hashedPassword = "my_hashed_password";

  @Before
  public void beforeTest() {
    when(environment.getProperty(ADMIN_USERNAME_PROPERTY)).thenReturn(username);
    when(environment.getProperty(ADMIN_HASHED_PASSWORD_PROPERTY)).thenReturn(hashedPassword);
  }

  @Test
  public void addAdminUser() {
    initializeDatabase.addAdminUser(mongoTemplate, environment);

    verify(mongoTemplate).save(userCaptor.capture());
    User user = userCaptor.getValue();
    assertEquals(ADMIN_ID, user.getId());
    assertEquals(username, user.getName());
    assertEquals(username, user.getEmail());
    assertEquals(ADMIN, user.getRole());
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertTrue(user.isActive());
    assertFalse(user.isManager());
    assertNull(user.getLaboratory());
    assertNotNull(user.getPreferences());
    assertTrue(user.getPreferences().isEmpty());
  }
}
