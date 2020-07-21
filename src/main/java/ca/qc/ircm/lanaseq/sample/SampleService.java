package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toLocalDateTime;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
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
import java.util.stream.Collectors;
import javax.transaction.Transactional;
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
  @PostAuthorize("returnObject == null || hasPermission(returnObject, 'read')")
  public Sample get(Long id) {
    if (id == null) {
      return null;
    }

    return repository.findById(id).orElse(null);
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
    try {
      return Files.list(folder).collect(Collectors.toCollection(ArrayList::new));
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
    User user = authorizationService.getCurrentUser();
    if (sample.getId() == null) {
      sample.setOwner(user);
      sample.setCreationDate(now);
      sample.setEditable(true);
    }
    Path oldFolder = null;
    if (sample.getName() != null) {
      oldFolder = configuration.folder(sample);
    }
    sample.generateName();
    repository.save(sample);
    Path folder = configuration.folder(sample);
    move(oldFolder, folder);
    renameDatasets(sample);
  }

  private void renameDatasets(Sample sample) {
    List<Dataset> datasets = datasetRepository.findBySamples(sample);
    for (Dataset dataset : datasets) {
      Path oldFolder = configuration.folder(dataset);
      dataset.generateName();
      datasetRepository.save(dataset);
      Path folder = configuration.folder(dataset);
      move(oldFolder, folder);
    }
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
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
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
