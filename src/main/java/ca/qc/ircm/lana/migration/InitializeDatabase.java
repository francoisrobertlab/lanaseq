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

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRole;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Initializes database.
 */
@ChangeLog(order = "00001")
public class InitializeDatabase {
  private static final Logger logger = LoggerFactory.getLogger(InitializeDatabase.class);

  /**
   * Adds admin user.
   *
   * @param mongoTemplate
   *          Mongo template
   * @param environment
   *          environment
   */
  @ChangeSet(id = "20181106113301-addAdminUser", order = "01", author = "poitrac")
  public void addAdminUser(MongoTemplate mongoTemplate, Environment environment) {
    String username = environment.getProperty(ADMIN_USERNAME_PROPERTY);
    String password = environment.getProperty(ADMIN_HASHED_PASSWORD_PROPERTY);
    User user = new User();
    user.setId(ADMIN_ID);
    user.setName(username);
    user.setEmail(username);
    user.setRole(UserRole.ADMIN);
    user.setHashedPassword(password);
    user.setActive(true);
    user.setPreferences(new HashMap<>());
    mongoTemplate.save(user);
    logger.debug("created admin with email: {}", username);
  }
}
