package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Service for {@link Sample}.
 */
@Service
@Transactional
public class SampleService {
  private SampleRepository repository;
  private AuthorizationService authorizationService;

  @Autowired
  protected SampleService(SampleRepository repository, AuthorizationService authorizationService) {
    this.repository = repository;
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
   * Returns all samples of a dataset.
   *
   * @param dataset
   *          dataset
   * @return all samples of a dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public List<Sample> all(Dataset dataset) {
    if (dataset == null) {
      return new ArrayList<>();
    }

    return repository.findAllByDataset(dataset);
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
    repository.save(sample);
  }
}
