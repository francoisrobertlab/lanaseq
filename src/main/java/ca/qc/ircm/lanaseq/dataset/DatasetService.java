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

package ca.qc.ircm.lanaseq.dataset;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

/**
 * Services for {@link Dataset}.
 */
@Service
@Transactional
public class DatasetService {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);
  private DatasetRepository repository;
  private SampleService sampleService;
  private AppConfiguration configuration;
  private AuthorizationService authorizationService;

  protected DatasetService() {
  }

  @Autowired
  protected DatasetService(DatasetRepository repository, SampleService sampleService,
      AppConfiguration configuration, AuthorizationService authorizationService) {
    this.repository = repository;
    this.sampleService = sampleService;
    this.configuration = configuration;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns dataset having specified id.
   *
   * @param id
   *          dataset's id
   * @return dataset having specified id
   */
  @PostAuthorize("returnObject == null || hasPermission(returnObject, 'read')")
  public Dataset get(Long id) {
    if (id == null) {
      return null;
    }

    return repository.findById(id).orElse(null);
  }

  /**
   * Returns all datasets.
   *
   * @return all datasets
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Dataset> all() {
    return repository.findAll();
  }

  /**
   * Returns all dataset's files.
   *
   * @param dataset
   *          dataset
   * @return all dataset's files
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public List<Path> files(Dataset dataset) {
    if (dataset == null || dataset.getId() == null) {
      return new ArrayList<>();
    }
    Path folder = configuration.folder(dataset);
    try {
      return Files.list(folder).collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  /**
   * Returns most recent tags.
   *
   * @param limit
   *          maximum number of tags to return
   * @return most recent tags
   */
  public List<String> topTags(int limit) {
    Set<String> tags = new LinkedHashSet<>();
    int page = 0;
    while (tags.size() < limit) {
      Page<Dataset> datasets = repository.findAllByOrderByIdDesc(PageRequest.of(page++, 50));
      if (datasets.isEmpty()) {
        break; // No more datasets.
      }
      tags.addAll(datasets.flatMap(d -> d.getTags().stream()).toList());
    }
    return tags.stream().limit(limit).collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Returns true if dataset can be deleted, false otherwise.
   *
   * @param dataset
   *          dataset
   * @return true if dataset can be deleted, false otherwise
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public boolean isDeletable(Dataset dataset) {
    if (dataset == null || dataset.getId() == null) {
      return false;
    }
    return true;
  }

  /**
   * Saves dataset into database.
   *
   * @param dataset
   *          dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void save(Dataset dataset) {
    LocalDateTime now = LocalDateTime.now();
    User user = authorizationService.getCurrentUser();
    if (dataset.getId() == null) {
      dataset.setOwner(user);
      dataset.setDate(now);
    }
    if (dataset.getSamples() != null) {
      for (Sample sample : dataset.getSamples()) {
        if (sample.isEditable()) {
          sampleService.save(sample);
        }
      }
    }
    Path oldFolder = null;
    if (dataset.getName() != null) {
      oldFolder = configuration.folder(dataset);
    }
    dataset.generateName();
    repository.save(dataset);
    Path folder = configuration.folder(dataset);
    move(oldFolder, folder);
  }

  private void move(Path oldFolder, Path folder) {
    if (oldFolder != null && Files.exists(oldFolder) && !oldFolder.equals(folder)) {
      try {
        logger.debug("moving folder {} to {} for dataset {}", oldFolder, folder);
        Files.move(oldFolder, folder);
      } catch (IOException e) {
        throw new IllegalStateException("could not move folder " + oldFolder + " to " + folder, e);
      }
    }
  }

  /**
   * Save files to dataset folder.
   *
   * @param dataset
   *          dataset
   * @param files
   *          files to save
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void saveFiles(Dataset dataset, Collection<Path> files) {
    Path folder = configuration.folder(dataset);
    try {
      Files.createDirectories(folder);
    } catch (IOException e) {
      throw new IllegalStateException("could not create folder " + folder, e);
    }
    for (Path file : files) {
      Path target = folder.resolve(file.getFileName());
      try {
        logger.debug("moving file {} to {} for dataset {}", file, target, dataset);
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new IllegalArgumentException("could not move file " + file + " to " + target, e);
      }
    }
  }

  /**
   * Deletes dataset from database.
   *
   * @param dataset
   *          dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void delete(Dataset dataset) {
    if (!isDeletable(dataset)) {
      throw new IllegalArgumentException("dataset cannot be deleted");
    }
    repository.delete(dataset);
    Path folder = configuration.folder(dataset);
    try {
      FileSystemUtils.deleteRecursively(folder);
    } catch (IOException e) {
      logger.error("could not delete folder {}", folder);
    }
  }
}
