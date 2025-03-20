package ca.qc.ircm.lanaseq.sample;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Public sample file repository.
 */
public interface SamplePublicFileRepository extends JpaRepository<SamplePublicFile, Long> {

  /**
   * Returns public file with specified sample and path.
   *
   * @param sample file's sample
   * @param path   file's path
   * @return public file with specified sample and path
   */
  Optional<SamplePublicFile> findBySampleAndPath(Sample sample, String path);

  /**
   * Returns public files with an expiry date equals or after specified date.
   *
   * @param date date
   * @return public files with an expiry date equals or after specified date
   */
  List<SamplePublicFile> findByExpiryDateGreaterThanEqual(LocalDate date);
}
