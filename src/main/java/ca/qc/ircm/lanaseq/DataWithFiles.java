package ca.qc.ircm.lanaseq;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data with files.
 */
public interface DataWithFiles {

  LocalDate getDate();

  LocalDateTime getCreationDate();

  String getName();
}
