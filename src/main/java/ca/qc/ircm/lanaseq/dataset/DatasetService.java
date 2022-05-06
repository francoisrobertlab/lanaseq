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

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toLocalDateTime;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.file.Renamer;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

/**
 * Services for {@link Dataset}.
 */
@Service
@Transactional
public class DatasetService {
  private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);
  private DatasetRepository repository;
  private AppConfiguration configuration;
  private AuthorizationService authorizationService;
  private JPAQueryFactory queryFactory;

  protected DatasetService() {
  }

  @Autowired
  protected DatasetService(DatasetRepository repository, AppConfiguration configuration,
      AuthorizationService authorizationService, JPAQueryFactory queryFactory) {
    this.repository = repository;
    this.configuration = configuration;
    this.authorizationService = authorizationService;
    this.queryFactory = queryFactory;
  }

  /**
   * Returns dataset having specified id.
   *
   * @param id
   *          dataset's id
   * @return dataset having specified id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<Dataset> get(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    return repository.findById(id);
  }

  /**
   * Returns true if a dataset with specified name exists, false otherwise.
   *
   * @param name
   *          name
   * @return true if a dataset with specified name exists, false otherwise
   */
  public boolean exists(String name) {
    if (name == null) {
      return false;
    }
    return repository.existsByName(name);
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
   * Returns all datasets passing filter.
   *
   * @param filter
   *          filter
   * @return all datasets passing filter
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Dataset> all(DatasetFilter filter) {
    if (filter == null) {
      filter = new DatasetFilter();
    }

    return new ArrayList<>(repository.findAll(filter.predicate(), filter.pageable()).getContent());
  }

  /**
   * Returns number of datasets passing filter.
   *
   * @param filter
   *          filter
   * @return number of datasets passing filter
   */
  public long count(DatasetFilter filter) {
    if (filter == null) {
      filter = new DatasetFilter();
    }

    return repository.count(filter.predicate());
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
    try (Stream<Path> files = Files.list(folder)) {
      return files.filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
          .filter(file -> !file.toFile().isHidden())
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  /**
   * Returns all dataset's upload files.
   *
   * @param dataset
   *          dataset
   * @return all dataset's upload files
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public List<Path> uploadFiles(Dataset dataset) {
    if (dataset == null || dataset.getId() == null) {
      return new ArrayList<>();
    }
    Path upload = configuration.getUpload();
    Path datasetUpload = configuration.upload(dataset);
    try {
      List<Path> files = new ArrayList<>();
      if (Files.exists(upload)) {
        try (Stream<Path> uploadFiles = Files.list(upload)) {
          uploadFiles.filter(file -> file.toFile().isFile()).filter(file -> {
            String filename = Optional.ofNullable(file.getFileName().toString()).orElse("");
            return filename.contains(dataset.getName());
          }).filter(file -> !file.toFile().isHidden()).forEach(file -> files.add(file));
        }
      }
      if (Files.exists(datasetUpload)) {
        try (Stream<Path> uploadFiles = Files.list(datasetUpload)) {
          uploadFiles.filter(file -> file.toFile().isFile())
              .filter(file -> !file.toFile().isHidden()).forEach(file -> files.add(file));
        }
      }
      return files;
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
    return dataset.isEditable();
  }

  /**
   * Saves dataset into database.
   *
   * @param dataset
   *          dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void save(Dataset dataset) {
    if (dataset.getId() != null && !dataset.isEditable()) {
      throw new IllegalArgumentException("dataset " + dataset + " cannot be edited");
    }
    if (dataset.getName() == null) {
      throw new NullPointerException("dataset's name cannot be null");
    }
    if (dataset.getSamples() != null && dataset.getSamples().stream()
        .filter(sample -> sample.getId() == null).findAny().isPresent()) {
      throw new IllegalArgumentException("all dataset's samples must already be in database");
    }
    LocalDateTime now = LocalDateTime.now();
    User user = authorizationService.getCurrentUser().orElse(null);
    if (dataset.getId() == null) {
      dataset.setOwner(user);
      dataset.setCreationDate(now);
      dataset.setEditable(true);
    }
    Dataset old = old(dataset).orElse(null);
    final String oldName = old != null ? old.getName() : null;
    Path oldFolder = old != null ? configuration.folder(old) : null;
    repository.save(dataset);
    Path folder = configuration.folder(dataset);
    Renamer.moveFolder(oldFolder, folder);
    Renamer.renameFiles(oldName, dataset.getName(), folder);
  }

  @Transactional(TxType.REQUIRES_NEW)
  protected Optional<Dataset> old(Dataset dataset) {
    if (dataset.getId() != null) {
      return repository.findById(dataset.getId());
    } else {
      return Optional.empty();
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
        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        Files.delete(file);
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

  /**
   * Deletes dataset file.
   *
   * @param dataset
   *          dataset
   * @param file
   *          file to delete
   */
  public void deleteFile(Dataset dataset, Path file) {
    Path filename = file.getFileName();
    if (filename == null) {
      throw new IllegalArgumentException("file " + file + " is empty");
    }
    Path folder = configuration.folder(dataset);
    Path toDelete = folder.resolve(file);
    Path toDeleteParent = toDelete.getParent();
    if (toDeleteParent == null || !toDeleteParent.equals(folder)) {
      throw new IllegalArgumentException("file " + file + " not in folder " + folder);
    }
    Path deleted = folder.resolve(DELETED_FILENAME);
    try (Writer writer =
        Files.newBufferedWriter(deleted, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
      writer.write(filename.toString());
      writer.write("\t");
      DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
      writer.write(
          formatter.format(toLocalDateTime(Files.getLastModifiedTime(toDelete).toInstant())));
      writer.write("\t");
      writer.write(formatter.format(LocalDateTime.now()));
      writer.write("\n");
      Files.delete(toDelete);
    } catch (IOException e) {
      throw new IllegalArgumentException("could not delete file " + file + " from folder " + folder,
          e);
    }
  }
}
