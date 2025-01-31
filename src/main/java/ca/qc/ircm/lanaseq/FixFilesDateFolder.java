package ca.qc.ircm.lanaseq;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Fixes the location of samples and dataset files.
 */
@Component
@Profile({"!test & !integration-test"})
public class FixFilesDateFolder {

  private static final Logger logger = LoggerFactory.getLogger(FixFilesDateFolder.class);
  private final AppConfiguration configuration;
  private final DatasetRepository datasetRepository;
  private final SampleRepository sampleRepository;

  @Autowired
  protected FixFilesDateFolder(AppConfiguration configuration, DatasetRepository datasetRepository,
      SampleRepository sampleRepository) {
    this.configuration = configuration;
    this.datasetRepository = datasetRepository;
    this.sampleRepository = sampleRepository;
  }

  /**
   * Fixes the location of samples and dataset files.
   *
   * <p>
   * Files were stored inside a year folder that is based on getCreationDate rather than getDate.
   * This scripts fixes the location of the folders.
   * </p>
   */
  @EventListener(ApplicationReadyEvent.class)
  public void fixFilesDateFolder() {
    List<Dataset> datasets = datasetRepository.findAll();
    for (Dataset dataset : datasets) {
      moveFolderIfNecessary(dataset);
    }
    List<Sample> samples = sampleRepository.findAll();
    for (Sample sample : samples) {
      moveFolderIfNecessary(sample);
    }
  }

  private void moveFolderIfNecessary(DataWithFiles data) {
    if (data.getCreationDate().getYear() != data.getDate().getYear()) {
      Path folder = configuration.getHome().folder(data);
      if (validateParent(folder, data.getDate().getYear())) {
        Path oldFolder = Optional.of(folder).map(Path::getParent)
            .map(f -> f.resolveSibling(String.valueOf(data.getCreationDate().getYear())))
            .map(f -> f.resolve(folder.getFileName())).orElseThrow(
                () -> new IllegalStateException("parent folder of " + folder + " does not exists"));
        if (Files.exists(oldFolder)) {
          logger.info("moving folder {} to {} for dataset {}", oldFolder, folder, data);
          try {
            moveFolder(oldFolder, folder);
          } catch (IOException e) {
            logger.error("could not move all old files from {} to {}", oldFolder, folder);
          }
        }
      }
    }
  }

  private boolean validateParent(Path folder, int year) {
    Path parent = Optional.of(folder).map(Path::getParent).orElseThrow(
        () -> new IllegalStateException("parent folder of " + folder + " cannot be null"));
    String parentFilename = Objects.toString(parent.getFileName(), "");
    if (!parentFilename.equals(String.valueOf(year))) {
      logger.warn("parent folder of {} does not match year {}", folder, year);
      return false;
    }
    return true;
  }

  private void moveFolder(Path oldFolder, Path folder) throws IOException {
    try {
      Files.createDirectories(folder);
      try (Stream<Path> walkFiles = Files.walk(oldFolder).sorted(Collections.reverseOrder())) {
        walkFiles.forEach(oldFile -> {
          Path file = folder.resolve(oldFolder.relativize(oldFile));
          try {
            if (Files.isDirectory(oldFile)) {
              Files.delete(oldFile);
            } else {
              Files.createDirectories(file.getParent());
              Files.move(oldFile, file);
            }
          } catch (IOException e) {
            logger.error("could not move old file from {} to {}", oldFile, file);
            throw new IllegalStateException("could not move old file " + oldFile + " to " + file);
          }
        });
      }
    } catch (IllegalStateException e) {
      throw new IOException(e.getMessage(), e);
    }
  }
}
