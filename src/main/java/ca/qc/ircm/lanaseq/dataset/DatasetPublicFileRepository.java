package ca.qc.ircm.lanaseq.dataset;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Public dataset file repository.
 */
public interface DatasetPublicFileRepository extends JpaRepository<DatasetPublicFile, Long> {

  /**
   * Returns public file with specified dataset and path.
   *
   * @param dataset file's dataset
   * @param path    file's path
   * @return public file with specified dataset and path
   */
  Optional<DatasetPublicFile> findByDatasetAndPath(Dataset dataset, String path);

  /**
   * Returns public files with an expiry date equals or after specified date.
   *
   * @param date date
   * @return public files with an expiry date equals or after specified date
   */
  List<DatasetPublicFile> findByExpiryDateGreaterThanEqual(LocalDate date);
}
