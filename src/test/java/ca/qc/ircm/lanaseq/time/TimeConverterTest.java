package ca.qc.ircm.lanaseq.time;

import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toLocalDate;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toLocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TimeConverter}.
 */
public class TimeConverterTest {

  @Test
  public void toInstant_LocalDateTime() {
    LocalDateTime dateTime1 = LocalDateTime.now();
    LocalDateTime dateTime2 = LocalDateTime.now().minusMinutes(10);

    assertEquals(dateTime1.atZone(ZoneId.systemDefault()).toInstant(), toInstant(dateTime1));
    assertEquals(dateTime2.atZone(ZoneId.systemDefault()).toInstant(), toInstant(dateTime2));
  }

  @Test
  public void toInstant_LocalDate() {
    LocalDate date1 = LocalDate.now();
    LocalDate date2 = LocalDate.now().minusDays(2);

    assertEquals(date1.atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant(), toInstant(date1));
    assertEquals(date2.atTime(0, 0).atZone(ZoneId.systemDefault()).toInstant(), toInstant(date2));
  }

  @Test
  public void toLocalDateTime_Instant() {
    Instant instant1 = Instant.now();
    Instant instant2 = Instant.now().minus(10, ChronoUnit.MINUTES);

    assertEquals(LocalDateTime.ofInstant(instant1, ZoneId.systemDefault()),
        toLocalDateTime(instant1));
    assertEquals(LocalDateTime.ofInstant(instant2, ZoneId.systemDefault()),
        toLocalDateTime(instant2));
  }

  @Test
  public void toLocalDate_Instant() {
    Instant instant1 = Instant.now();
    Instant instant2 = Instant.now().minus(10, ChronoUnit.MINUTES);

    assertEquals(LocalDateTime.ofInstant(instant1, ZoneId.systemDefault()).toLocalDate(),
        toLocalDate(instant1));
    assertEquals(LocalDateTime.ofInstant(instant2, ZoneId.systemDefault()).toLocalDate(),
        toLocalDate(instant2));
  }
}
