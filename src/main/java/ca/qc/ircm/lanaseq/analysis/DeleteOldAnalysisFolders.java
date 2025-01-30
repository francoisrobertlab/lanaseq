package ca.qc.ircm.lanaseq.analysis;

import ca.qc.ircm.lanaseq.AppConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

/**
 * Deletes old analysis folders.
 */
@Component
public class DeleteOldAnalysisFolders {

  private static final Logger logger = LoggerFactory.getLogger(DeleteOldAnalysisFolders.class);

  private AppConfiguration configuration;

  @Autowired
  protected DeleteOldAnalysisFolders(AppConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Deletes old analysis folders.
   *
   * <br>
   * This method is executed every hour with a initial delay of 2 minutes to let the application
   * start.
   */
  @Scheduled(fixedRateString = "PT1H", initialDelayString = "PT2M")
  public void deleteOldAnalysisFolders() {
    Path analysis = configuration.getAnalysis().getFolder();
    logger.debug("deleting old folders in analysis {}", analysis);
    Duration deleteAge = configuration.getAnalysisDeleteAge();
    Instant now = Instant.now();
    try (Stream<Path> files = Files.list(analysis)) {
      files.filter(file -> {
        try {
          FileTime modifiedTime = Files.getLastModifiedTime(file);
          Duration age = Duration.between(modifiedTime.toInstant(), now);
          return deleteAge.compareTo(age) < 0;
        } catch (IOException e) {
          return false;
        }
      }).forEach(file -> {
        try {
          logger.debug("deleting old analysis folder {}", file);
          if (!FileSystemUtils.deleteRecursively(file)) {
            logger.warn("could not delete folder {}", file);
          }
        } catch (IOException e) {
          logger.warn("could not delete folder {}", file);
        }
      });
    } catch (IOException e) {
      logger.warn("could not list files and folders in folder {}", analysis);
    }
  }
}
