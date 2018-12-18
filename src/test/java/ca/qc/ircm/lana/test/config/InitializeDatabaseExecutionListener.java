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

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestContext;

/**
 * Initialized test database.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
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

  @Override
  @SuppressWarnings("checkstyle:linelength")
  public void beforeTestMethod(TestContext testContext) throws Exception {
    logger.debug("Initializes database");

    User user = userRepository.findById(1L).orElse(null);
    user.setLastSignAttempt(Instant.now().minus(4, ChronoUnit.DAYS));
    userRepository.save(user);
    user = userRepository.findById(2L).orElse(null);
    user.setLastSignAttempt(Instant.now().minus(2, ChronoUnit.HOURS));
    userRepository.save(user);
    user = userRepository.findById(3L).orElse(null);
    user.setLastSignAttempt(Instant.now().minus(10, ChronoUnit.DAYS));
    userRepository.save(user);
    user = userRepository.findById(4L).orElse(null);
    user.setLastSignAttempt(Instant.now().minus(21, ChronoUnit.DAYS));
    userRepository.save(user);
    user = userRepository.findById(5L).orElse(null);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.MINUTES));
    userRepository.save(user);
    user = userRepository.findById(6L).orElse(null);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.MINUTES));
    userRepository.save(user);
  }
}
