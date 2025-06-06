package ca.qc.ircm.lanaseq.test.config;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.UsedBy;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Initialized test database.
 */
@Order(InitializeDatabaseExecutionListener.ORDER)
public class InitializeDatabaseExecutionListener implements TestExecutionListener,
    InjectDependencies {

  public static final int ORDER = 5001;
  /**
   * Matches pass1.
   */
  @SuppressWarnings("checkstyle:linelength")
  public static final String PASSWORD_PASS1 = "$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq";
  /**
   * Matches pass2.
   */
  @SuppressWarnings("checkstyle:linelength")
  public static final String PASSWORD_PASS2 = "$2a$10$JU0aj7Cc/7sWVkFXoHbWTuvVWEAwXFT1EhCX4S6Aa9JfSsKqLP8Tu";
  private static final Logger logger = LoggerFactory.getLogger(
      InitializeDatabaseExecutionListener.class);
  private UserRepository userRepository;

  @Override
  public void beforeTestClass(TestContext testContext) {
    injectDependencies(testContext.getApplicationContext());
  }

  @Override
  @SuppressWarnings("checkstyle:linelength")
  public void beforeTestMethod(@NotNull TestContext testContext) {
    logger.debug("Initializes database");

    User user = userRepository.findById(1L).orElseThrow();
    user.setLastSignAttempt(LocalDateTime.now().minusDays(4));
    userRepository.save(user);
    user = userRepository.findById(2L).orElseThrow();
    user.setLastSignAttempt(LocalDateTime.now().minusHours(2));
    userRepository.save(user);
    user = userRepository.findById(3L).orElseThrow();
    user.setLastSignAttempt(LocalDateTime.now().minusDays(10));
    userRepository.save(user);
    user = userRepository.findById(4L).orElseThrow();
    user.setLastSignAttempt(LocalDateTime.now().minusDays(21));
    userRepository.save(user);
    user = userRepository.findById(5L).orElseThrow();
    user.setLastSignAttempt(LocalDateTime.now().minusMinutes(20));
    userRepository.save(user);
    user = userRepository.findById(6L).orElseThrow();
    user.setLastSignAttempt(LocalDateTime.now().minusMinutes(20));
    userRepository.save(user);
  }

  @Autowired
  @UsedBy(SPRING)
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
}
