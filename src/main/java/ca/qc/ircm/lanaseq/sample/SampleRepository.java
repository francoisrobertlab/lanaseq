package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Sample}.
 */
public interface SampleRepository extends JpaRepository<Sample, Long> {
  public boolean existsByNameAndDataset(String name, Dataset dataset);

  public List<Sample> findAllByDataset(Dataset dataset);
}
