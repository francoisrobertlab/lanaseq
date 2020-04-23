package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import com.google.common.collect.Range;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Filters protocols.
 */
public class ProtocolFilter implements Predicate<Protocol> {
  public String nameContains;
  public Range<LocalDate> dateRange;
  public String ownerContains;

  @Override
  public boolean test(Protocol protocol) {
    boolean test = true;
    if (nameContains != null) {
      test &= comparable(replaceNull(protocol.getName())).contains(comparable(nameContains));
    }
    if (dateRange != null) {
      test &= dateRange.contains(protocol.getDate().toLocalDate());
    }
    if (ownerContains != null) {
      test &= comparable(replaceNull(protocol.getOwner().getEmail()))
          .contains(comparable(ownerContains))
          || comparable(replaceNull(protocol.getOwner().getName()))
              .contains(comparable(ownerContains));
    }
    return test;
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
