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

package ca.qc.ircm.lana.test.config;

import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestContext;

/**
 * Initialized Mongo database.
 */
public class InitializeMongoExecutionListener extends InjectIntoTestExecutionListener {
  private static final Logger logger =
      LoggerFactory.getLogger(InitializeMongoExecutionListener.class);
  @SuppressWarnings("checkstyle:linelength")
  public static final String PASSWORD_PASS1 =
      "$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq";
  @SuppressWarnings("checkstyle:linelength")
  public static final String PASSWORD_PASS2 =
      "$2a$10$JU0aj7Cc/7sWVkFXoHbWTuvVWEAwXFT1EhCX4S6Aa9JfSsKqLP8Tu";
  @Inject
  private MongoTemplate mongoTemplate;

  @Override
  @SuppressWarnings("checkstyle:linelength")
  public void beforeTestMethod(TestContext testContext) throws Exception {
    logger.debug("Initializes database");

    User user = new User();
    user.setId("1");
    user.setName("Lana Administrator");
    user.setEmail("lana@ircm.qc.ca");
    user.setRole(UserRole.ADMIN);
    user.setHashedPassword(PASSWORD_PASS2);
    user.setSignAttempts(1);
    user.setLastSignAttempt(Instant.now().minus(4, ChronoUnit.DAYS));
    user.setActive(true);
    user.setPreferences(new HashMap<>());
    mongoTemplate.save(user);
    Laboratory lab = new Laboratory();
    lab.setId("1");
    lab.setName("Chromatin and Genomic Expression");
    mongoTemplate.save(lab);
    user = new User();
    user.setId("2");
    user.setName("Francois Robert");
    user.setEmail("francois.robert@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(0);
    user.setLastSignAttempt(Instant.now().minus(2, ChronoUnit.HOURS));
    user.setActive(true);
    user.setManager(true);
    user.setLaboratory(lab);
    Map<String, String> preferences = new HashMap<>();
    preferences.put(User.LOCALE, "en");
    user.setPreferences(preferences);
    mongoTemplate.save(user);
    user = new User();
    user.setId("3");
    user.setName("Jonh Smith");
    user.setEmail("jonh.smith@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(2);
    user.setLastSignAttempt(Instant.now().minus(10, ChronoUnit.DAYS));
    user.setActive(true);
    user.setLaboratory(lab);
    user.setPreferences(new HashMap<>());
    mongoTemplate.save(user);
    lab = new Laboratory();
    lab.setId("2");
    lab.setName("Translational Proteomics Research Unit");
    mongoTemplate.save(lab);
    user = new User();
    user.setId("4");
    user.setName("Benoit Coulombe");
    user.setEmail("benoit.coulombe@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setActive(true);
    user.setManager(true);
    user.setSignAttempts(0);
    user.setLastSignAttempt(Instant.now().minus(21, ChronoUnit.DAYS));
    user.setLaboratory(lab);
    user.setPreferences(new HashMap<>());
    mongoTemplate.save(user);
    user = new User();
    user.setId("4");
    user.setName("Christian Poitras");
    user.setEmail("christian.poitras@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(3);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.MINUTES));
    user.setActive(true);
    user.setLaboratory(lab);
    user.setPreferences(new HashMap<>());
    mongoTemplate.save(user);
    user = new User();
    user.setId("5");
    user.setName("Inactive User");
    user.setEmail("inactive.user@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(3);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.MINUTES));
    user.setActive(false);
    user.setLaboratory(lab);
    user.setPreferences(new HashMap<>());
    mongoTemplate.save(user);
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    mongoTemplate.dropCollection(User.class);
    mongoTemplate.dropCollection(Laboratory.class);
  }
}
