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

package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toLocalDateTime;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.file.Renamer;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

/**
 * Service for {@link Sample}.
 */
@Service
@Transactional
public class SampleService {
  private static final Logger logger = LoggerFactory.getLogger(SampleService.class);
  private SampleRepository repository;
  private DatasetRepository datasetRepository;
  private AppConfiguration configuration;
  private AuthorizationService authorizationService;

  @Autowired
  protected SampleService(SampleRepository repository, DatasetRepository datasetRepository,
      AppConfiguration configuration, AuthorizationService authorizationService) {
    this.repository = repository;
    this.datasetRepository = datasetRepository;
    this.configuration = configuration;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns sample with id.
   *
   * @param id
   *          sample's id
   * @return sample with id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<Sample> get(Long id) {
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
   * Returns all samples.
   *
   * @return all samples
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Sample> all() {
    return repository.findAll();
  }

  /**
   * Returns all samples passing filter.
   *
   * @param filter
   *          filter
   * @return all samples passing filter
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Sample> all(SampleFilter filter) {
    if (filter == null) {
      filter = new SampleFilter();
    }

    return new ArrayList<>(repository.findAll(filter.predicate(), filter.pageable()).getContent());
  }

  /**
   * Returns number of samples passing filter.
   *
   * @param filter
   *          filter
   * @return number of samples passing filter
   */
  public long count(SampleFilter filter) {
    if (filter == null) {
      filter = new SampleFilter();
    }

    return repository.count(filter.predicate());
  }

  /**
   * Returns all sample's files.
   *
   * @param sample
   *          sample
   * @return all sample's files
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public List<Path> files(Sample sample) {
    if (sample == null || sample.getId() == null) {
      return new ArrayList<>();
    }
    Path folder = configuration.folder(sample);
    try (Stream<Path> files = Files.list(folder)) {
      return files.filter(file -> !DELETED_FILENAME.equals(file.getFileName().toString()))
          .filter(file -> !file.toFile().isHidden())
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  /**
   * Returns all sample's upload files.
   *
   * @param sample
   *          sample
   * @return all sample's upload files
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public List<Path> uploadFiles(Sample sample) {
    if (sample == null || sample.getId() == null) {
      return new ArrayList<>();
    }
    Path upload = configuration.getUpload();
    Path sampleUpload = configuration.upload(sample);
    try {
      List<Path> files = new ArrayList<>();
      if (Files.exists(upload)) {
        try (Stream<Path> uploadFiles = Files.list(upload)) {
          uploadFiles.filter(file -> file.toFile().isFile()).filter(file -> {
            String filename = Optional.ofNullable(file.getFileName().toString()).orElse("");
            return filename.contains(sample.getName());
          }).filter(file -> !file.toFile().isHidden()).forEach(file -> files.add(file));
        }
      }
      if (Files.exists(sampleUpload)) {
        try (Stream<Path> uploadFiles = Files.list(sampleUpload)) {
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
   * Returns true if sample can be deleted, false otherwise.
   *
   * @param sample
   *          sample
   * @return true if sample can be deleted, false otherwise
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public boolean isDeletable(Sample sample) {
    if (sample == null || sample.getId() == null) {
      return false;
    }
    return sample.isEditable() && !datasetRepository.existsBySamples(sample);
  }

  /**
   * Returns true if samples can be merged, false otherwise.
   *
   * @param samples
   *          samples
   * @return true if samples can be merged, false otherwise
   */
  @PreAuthorize("hasRole('USER')")
  public boolean isMergable(Collection<Sample> samples) {
    if (samples == null || samples.isEmpty()) {
      return false;
    }
    Sample first = samples.stream().findAny().orElse(new Sample());
    boolean mergable = true;
    for (Sample sample : samples) {
      mergable &= first.getProtocol() != null && sample.getProtocol() != null
          ? first.getProtocol().getId().equals(sample.getProtocol().getId())
          : first.getProtocol() == sample.getProtocol();
      mergable &= first.getAssay() != null ? first.getAssay().equals(sample.getAssay())
          : sample.getAssay() == null;
      mergable &= first.getType() != null ? first.getType().equals(sample.getType())
          : sample.getType() == null;
      mergable &= first.getTarget() != null ? first.getTarget().equals(sample.getTarget())
          : sample.getTarget() == null;
      mergable &= first.getStrain() != null ? first.getStrain().equals(sample.getStrain())
          : sample.getStrain() == null;
      mergable &= first.getStrainDescription() != null
          ? first.getStrainDescription().equals(sample.getStrainDescription())
          : sample.getStrainDescription() == null;
      mergable &= first.getTreatment() != null ? first.getTreatment().equals(sample.getTreatment())
          : sample.getTreatment() == null;
    }
    return mergable;
  }

  /**
   * Saves sample into database.
   *
   * @param sample
   *          sample
   */
  @PreAuthorize("hasPermission(#sample, 'write')")
  public void save(Sample sample) {
    if (sample.getId() != null && !sample.isEditable()) {
      throw new IllegalArgumentException("sample " + sample + " cannot be edited");
    }
    LocalDateTime now = LocalDateTime.now();
    User user = authorizationService.getCurrentUser().orElse(null);
    if (sample.getId() == null) {
      sample.setOwner(user);
      sample.setCreationDate(now);
      sample.setEditable(true);
    }
    sample.generateName();
    Sample old = old(sample).orElse(null);
    final String oldName = old != null ? old.getName() : null;
    final Path oldFolder = old != null ? configuration.folder(old) : null;
    repository.save(sample);
    Path folder = configuration.folder(sample);
    Renamer.moveFolder(oldFolder, folder);
    Renamer.renameFiles(oldName, sample.getName(), folder);
  }

  @Transactional(TxType.REQUIRES_NEW)
  protected Optional<Sample> old(Sample sample) {
    if (sample.getId() != null) {
      return repository.findById(sample.getId());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Save files to sample folder.
   *
   * @param sample
   *          sample
   * @param files
   *          files to save
   */
  @PreAuthorize("hasPermission(#sample, 'write')")
  public void saveFiles(Sample sample, Collection<Path> files) {
    Path folder = configuration.folder(sample);
    try {
      Files.createDirectories(folder);
    } catch (IOException e) {
      throw new IllegalStateException("could not create folder " + folder, e);
    }
    for (Path file : files) {
      Path target = folder.resolve(file.getFileName());
      try {
        logger.debug("moving file {} to {} for sample {}", file, target, sample);
        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        Files.delete(file);
      } catch (IOException e) {
        throw new IllegalArgumentException("could not move file " + file + " to " + target, e);
      }
    }
  }

  /**
   * Deletes sample from database.
   *
   * @param sample
   *          sample
   */
  @PreAuthorize("hasPermission(#sample, 'write')")
  public void delete(Sample sample) {
    if (!isDeletable(sample)) {
      throw new IllegalArgumentException("sample cannot be deleted");
    }
    repository.delete(sample);
    Path folder = configuration.folder(sample);
    try {
      FileSystemUtils.deleteRecursively(folder);
    } catch (IOException e) {
      logger.error("could not delete folder {}", folder);
    }
  }

  /**
   * Deletes sample file.
   *
   * @param sample
   *          sample
   * @param file
   *          file to delete
   */
  public void deleteFile(Sample sample, Path file) {
    Path filename = file.getFileName();
    if (filename == null) {
      throw new IllegalArgumentException("file " + file + " is empty");
    }
    Path folder = configuration.folder(sample);
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
