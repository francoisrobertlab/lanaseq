package ca.qc.ircm.lanaseq.sample;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Sample}.
 */
public interface SampleRepository extends JpaRepository<Sample, Long> {
}
