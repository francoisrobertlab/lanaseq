package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.List;
import javax.transaction.Transactional;
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
  private SampleRepository repository;
  private DatasetRepository datasetRepository;
  private AuthorizationService authorizationService;

  @Autowired
  protected SampleService(SampleRepository repository, DatasetRepository datasetRepository,
      AuthorizationService authorizationService) {
    this.repository = repository;
    this.datasetRepository = datasetRepository;
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
    sample.generateName();
    repository.save(sample);
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
