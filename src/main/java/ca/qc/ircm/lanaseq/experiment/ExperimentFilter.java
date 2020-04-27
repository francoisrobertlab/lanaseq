package ca.qc.ircm.lanaseq.experiment;

import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import com.google.common.collect.Range;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Filters experiments.
 */
public class ExperimentFilter implements Predicate<Experiment> {
  public String nameContains;
  public Range<LocalDate> dateRange;
  public String protocolContains;
  public String ownerContains;

  @Override
  public boolean test(Experiment experiment) {
    boolean test = true;
    if (nameContains != null) {
      test &= comparable(replaceNull(experiment.getName())).contains(comparable(nameContains));
    }
    if (dateRange != null) {
      test &= dateRange.contains(experiment.getDate().toLocalDate());
    }
    if (protocolContains != null) {
      test &= comparable(replaceNull(experiment.getProtocol().getName()))
          .contains(comparable(protocolContains));
    }
    if (ownerContains != null) {
      test &= comparable(replaceNull(experiment.getOwner().getEmail()))
          .contains(comparable(ownerContains))
          || comparable(replaceNull(experiment.getOwner().getName()))
              .contains(comparable(ownerContains));
    }
    return test;
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
