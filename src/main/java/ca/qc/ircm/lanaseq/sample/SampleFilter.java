package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.sample.QSample.sample;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ID;
import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Filters samples.
 */
public class SampleFilter implements Predicate<Sample> {

  public String nameContains;
  public String keywordsContains;
  public String protocolContains;
  public Range<LocalDate> dateRange;
  public String ownerContains;
  public Sort sort = Sort.by(Direction.ASC, ID);
  public int page = 0;
  public int size = Integer.MAX_VALUE;

  @Override
  public boolean test(Sample sample) {
    boolean test = true;
    if (nameContains != null) {
      test &= comparable(replaceNull(sample.getName())).contains(comparable(nameContains));
    }
    if (keywordsContains != null) {
      test &= sample.getKeywords().stream()
          .anyMatch(keyword -> comparable(keyword).contains(comparable(keywordsContains)));
    }
    if (protocolContains != null) {
      test &= comparable(replaceNull(sample.getProtocol().getName())).contains(
          comparable(protocolContains));
    }
    if (dateRange != null) {
      test &= dateRange.contains(sample.getDate(), Comparator.naturalOrder());
    }
    if (ownerContains != null) {
      test &=
          comparable(replaceNull(sample.getOwner().getEmail())).contains(comparable(ownerContains))
              || comparable(replaceNull(sample.getOwner().getName())).contains(
              comparable(ownerContains));
    }
    return test;
  }

  /**
   * Returns QueryDSL predicate matching filter.
   *
   * @return QueryDSL predicate matching filter
   */
  public com.querydsl.core.types.Predicate predicate() {
    BooleanBuilder predicate = new BooleanBuilder();
    if (nameContains != null) {
      predicate.and(sample.name.contains(nameContains));
    }
    if (keywordsContains != null) {
      predicate.and(sample.keywords.any().contains(keywordsContains));
    }
    if (protocolContains != null) {
      predicate.and(sample.protocol.name.contains(protocolContains));
    }
    if (dateRange != null) {
      if (dateRange.getLowerBound().isBounded()) {
        LocalDate date = dateRange.getLowerBound().getValue().orElseThrow();
        if (dateRange.getLowerBound().isInclusive()) {
          date = date.minusDays(1);
        }
        predicate.and(sample.date.after(date));
      }
      if (dateRange.getUpperBound().isBounded()) {
        LocalDate date = dateRange.getUpperBound().getValue().orElseThrow();
        if (dateRange.getUpperBound().isInclusive()) {
          date = date.plusDays(1);
        }
        predicate.and(sample.date.before(date));
      }
    }
    if (ownerContains != null) {
      predicate.and(
          sample.owner.email.contains(ownerContains).or(sample.owner.name.contains(ownerContains)));
    }
    return predicate.getValue() != null ? predicate.getValue()
        : Expressions.asBoolean(true).isTrue();
  }

  public Pageable pageable() {
    return PageRequest.of(page, size, sort != null ? sort : Sort.unsorted());
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
