package ca.qc.ircm.lanaseq;

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
 * Deletes old upload folders.
 */
@Component
public class DeleteOldUploadFolders {
  private static final Logger logger = LoggerFactory.getLogger(DeleteOldUploadFolders.class);

  private final AppConfiguration configuration;

  @Autowired
  protected DeleteOldUploadFolders(AppConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Deletes old upload folders.
   *
   * <br>
   * This method is executed every hour with an initial delay of 2 minutes to let the application
   * start.
   */
  @Scheduled(fixedRateString = "PT1H", initialDelayString = "PT2M")
  public void deleteOldUploadFolders() {
    Path upload = configuration.getUpload().getFolder();
    if (upload == null) {
      return;
    }
    logger.debug("deleting old folders in upload {}", upload);
    Duration deleteAge = configuration.getUploadDeleteAge();
    Instant now = Instant.now();
    try (Stream<Path> files = Files.list(upload)) {
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
          logger.debug("deleting old upload folder {}", file);
          FileSystemUtils.deleteRecursively(file);
        } catch (IOException e) {
          logger.warn("could not delete folder {}", file);
        }
      });
    } catch (IOException e) {
      logger.warn("could not list files and folders in folder {}", upload);
    }
  }
}
