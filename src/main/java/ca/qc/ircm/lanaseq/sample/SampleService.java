package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
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
   * @return true if sample can be deleted, false otherwise
   */
  @PreAuthorize("hasPermission(#sample, 'read')")
  public boolean isDeletable(Sample sample) {
    if (sample == null || sample.getId() == null) {
      return false;
    }
    return !datasetRepository.existsBySamples(sample);
  }

  /**
   * Returns true if samples can be merged, false otherwise.
   *
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
    LocalDateTime now = LocalDateTime.now();
    User user = authorizationService.getCurrentUser();
    if (sample.getId() == null) {
      sample.setOwner(user);
      sample.setDate(now);
    }
    Path oldFolder = null;
    if (sample.getName() != null) {
      oldFolder = configuration.folder(sample);
    }
    sample.generateName();
    repository.save(sample);
    Path folder = configuration.folder(sample);
    if (oldFolder != null && Files.exists(oldFolder) && !oldFolder.equals(folder)) {
      try {
        logger.debug("moving folder {} to {} for sample {}", oldFolder, folder, sample);
        Files.move(oldFolder, folder);
      } catch (IOException e) {
        throw new IllegalArgumentException("could not move file " + oldFolder + " to " + folder, e);
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
  }
}
