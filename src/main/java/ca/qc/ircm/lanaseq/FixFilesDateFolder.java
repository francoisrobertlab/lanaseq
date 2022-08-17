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
@Profile({ "!test & !integration-test" })
public class FixFilesDateFolder {
  private static final Logger logger = LoggerFactory.getLogger(FixFilesDateFolder.class);
  private AppConfiguration configuration;
  private DatasetRepository datasetRepository;
  private SampleRepository sampleRepository;

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
      if (dataset.getCreationDate().getYear() != dataset.getDate().getYear()) {
        Path folder = configuration.getHome().folder(dataset);
        if (!validateParent(folder, dataset.getDate().getYear())) {
          continue;
        }
        Path oldFolder = Optional.of(folder).map(Path::getParent)
            .map(f -> f.resolveSibling(String.valueOf(dataset.getCreationDate().getYear())))
            .map(f -> f.resolve(folder.getFileName())).orElseThrow(
                () -> new IllegalStateException("parent folder of " + folder + " does not exists"));
        if (Files.exists(oldFolder)) {
          logger.info("moving folder {} to {} for dataset {}", oldFolder, folder, dataset);
          try {
            moveFolder(oldFolder, folder);
          } catch (IOException e) {
            logger.error("could not move all old files from {} to {}", oldFolder, folder);
          }
        }
      }
    }
    List<Sample> samples = sampleRepository.findAll();
    for (Sample sample : samples) {
      if (sample.getCreationDate().getYear() != sample.getDate().getYear()) {
        Path folder = configuration.getHome().folder(sample);
        if (!validateParent(folder, sample.getDate().getYear())) {
          continue;
        }
        Path oldFolder = Optional.of(folder).map(Path::getParent)
            .map(f -> f.resolveSibling(String.valueOf(sample.getCreationDate().getYear())))
            .map(f -> f.resolve(folder.getFileName())).orElseThrow(
                () -> new IllegalStateException("parent folder of " + folder + " does not exists"));
        if (Files.exists(oldFolder)) {
          logger.info("moving folder {} to {} for sample {}", oldFolder, folder, sample);
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
      Files.walk(oldFolder).sorted(Collections.reverseOrder()).forEach(oldFile -> {
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
    } catch (IllegalStateException e) {
      throw new IOException(e.getMessage(), e);
    }
  }
}
