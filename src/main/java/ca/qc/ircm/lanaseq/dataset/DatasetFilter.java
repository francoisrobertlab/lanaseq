package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import com.google.common.collect.Range;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Filters datasets.
 */
public class DatasetFilter implements Predicate<Dataset> {
  public String nameContains;
  public String projectContains;
  public String protocolContains;
  public Range<LocalDate> dateRange;
  public String ownerContains;

  @Override
  public boolean test(Dataset dataset) {
    boolean test = true;
    if (nameContains != null) {
      test &= comparable(replaceNull(dataset.getName())).contains(comparable(nameContains));
    }
    if (projectContains != null) {
      test &=
          comparable(replaceNull(dataset.getProject())).contains(comparable(projectContains));
    }
    if (protocolContains != null) {
      test &= comparable(replaceNull(dataset.getProtocol().getName()))
          .contains(comparable(protocolContains));
    }
    if (dateRange != null) {
      test &= dateRange.contains(dataset.getDate().toLocalDate());
    }
    if (ownerContains != null) {
      test &= comparable(replaceNull(dataset.getOwner().getEmail()))
          .contains(comparable(ownerContains))
          || comparable(replaceNull(dataset.getOwner().getName()))
              .contains(comparable(ownerContains));
    }
    return test;
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
