package ca.qc.ircm.lanaseq.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Converts time instance to other time instance.
 */
public class TimeConverter {
  public static Instant toInstant(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant();
  }

  public static Instant toInstant(LocalDate date) {
    return date.atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant();
  }

  public static LocalDateTime toLocalDateTime(Instant instant) {
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
  }

  public static LocalDate toLocalDate(Instant instant) {
    return toLocalDateTime(instant).toLocalDate();
  }
}
