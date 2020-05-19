package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Sample}.
 */
public interface SampleRepository extends JpaRepository<Sample, Long> {
  public List<Sample> findByOwner(User owner);
}
