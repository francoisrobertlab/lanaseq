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
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;

/**
 * Initialized test database.
 */
public class InitializeDatabaseExecutionListener extends InjectIntoTestExecutionListener {
  private static final Logger logger =
      LoggerFactory.getLogger(InitializeDatabaseExecutionListener.class);
  /**
   * Matches pass1.
   */
  @SuppressWarnings("checkstyle:linelength")
  public static final String PASSWORD_PASS1 =
      "$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq";
  /**
   * Matches pass2.
   */
  @SuppressWarnings("checkstyle:linelength")
  public static final String PASSWORD_PASS2 =
      "$2a$10$JU0aj7Cc/7sWVkFXoHbWTuvVWEAwXFT1EhCX4S6Aa9JfSsKqLP8Tu";
  @Inject
  private UserRepository userRepository;
  @Inject
  private LaboratoryRepository laboratoryRepository;

  @Override
  @SuppressWarnings("checkstyle:linelength")
  public void beforeTestMethod(TestContext testContext) throws Exception {
    logger.debug("Initializes database");

    User user = new User();
    user.setId(1L);
    user.setName("Lana Administrator");
    user.setEmail("lana@ircm.qc.ca");
    user.setRole(UserRole.ADMIN);
    user.setHashedPassword(PASSWORD_PASS2);
    user.setSignAttempts(1);
    user.setLastSignAttempt(Instant.now().minus(4, ChronoUnit.DAYS));
    user.setActive(true);
    userRepository.save(user);
    Laboratory lab = new Laboratory();
    lab.setId(1L);
    lab.setName("Chromatin and Genomic Expression");
    laboratoryRepository.save(lab);
    user = new User();
    user.setId(2L);
    user.setName("Francois Robert");
    user.setEmail("francois.robert@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(0);
    user.setLastSignAttempt(Instant.now().minus(2, ChronoUnit.HOURS));
    user.setActive(true);
    user.setManager(true);
    user.setLaboratory(lab);
    user.setLocale(Locale.ENGLISH);
    userRepository.save(user);
    user = new User();
    user.setId(3L);
    user.setName("Jonh Smith");
    user.setEmail("jonh.smith@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(2);
    user.setLastSignAttempt(Instant.now().minus(10, ChronoUnit.DAYS));
    user.setActive(true);
    user.setLaboratory(lab);
    userRepository.save(user);
    lab = new Laboratory();
    lab.setId(2L);
    lab.setName("Translational Proteomics Research Unit");
    laboratoryRepository.save(lab);
    user = new User();
    user.setId(4L);
    user.setName("Benoit Coulombe");
    user.setEmail("benoit.coulombe@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setActive(true);
    user.setManager(true);
    user.setSignAttempts(0);
    user.setLastSignAttempt(Instant.now().minus(21, ChronoUnit.DAYS));
    user.setLaboratory(lab);
    userRepository.save(user);
    user = new User();
    user.setId(4L);
    user.setName("Christian Poitras");
    user.setEmail("christian.poitras@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(3);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.MINUTES));
    user.setActive(true);
    user.setLaboratory(lab);
    userRepository.save(user);
    user = new User();
    user.setId(5L);
    user.setName("Inactive User");
    user.setEmail("inactive.user@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setHashedPassword(PASSWORD_PASS1);
    user.setSignAttempts(3);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.MINUTES));
    user.setActive(false);
    user.setLaboratory(lab);
    userRepository.save(user);
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    userRepository.deleteAll();
    laboratoryRepository.deleteAll();
  }
}
