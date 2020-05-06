package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.dataset.Dataset;
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

  @Autowired
  protected SampleService(SampleRepository repository) {
    this.repository = repository;
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
   * Returns sample with id.
   *
   * @param id
   *          sample's id
   * @return sample with id
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public boolean exists(String name, Dataset dataset) {
    if (name == null || dataset == null) {
      return false;
    }

    return repository.existsByNameAndDataset(name, dataset);
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
   * Saves sample in database.
   *
   * @param sample
   *          sample
   */
  @PreAuthorize("hasPermission(#sample, 'write')")
  public void save(Sample sample) {
    if (sample.getId() == null) {
      sample.setDate(LocalDateTime.now());
    }
    repository.save(sample);
  }
}
