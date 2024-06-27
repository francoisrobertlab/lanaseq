package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.sample.QSample.sample;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ID;
import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Filters samples.
 */
public class SampleFilter implements Predicate<Sample> {
  public String nameContains;
  public String tagsContains;
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
    if (tagsContains != null) {
      test &= sample.getTags().stream()
          .filter(tag -> comparable(tag).contains(comparable(tagsContains))).findAny().isPresent();
    }
    if (protocolContains != null) {
      test &= comparable(replaceNull(sample.getProtocol().getName()))
          .contains(comparable(protocolContains));
    }
    if (dateRange != null) {
      test &= dateRange.contains(sample.getDate());
    }
    if (ownerContains != null) {
      test &=
          comparable(replaceNull(sample.getOwner().getEmail())).contains(comparable(ownerContains))
              || comparable(replaceNull(sample.getOwner().getName()))
                  .contains(comparable(ownerContains));
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
    if (tagsContains != null) {
      predicate.and(sample.tags.any().contains(tagsContains));
    }
    if (protocolContains != null) {
      predicate.and(sample.protocol.name.contains(protocolContains));
    }
    if (dateRange != null) {
      if (dateRange.hasLowerBound()) {
        LocalDate date = dateRange.lowerEndpoint();
        if (dateRange.lowerBoundType() == BoundType.OPEN) {
          date = date.plusDays(1);
        }
        predicate.and(sample.date.goe(date));
      }
      if (dateRange.hasUpperBound()) {
        LocalDate date = dateRange.upperEndpoint();
        if (dateRange.upperBoundType() == BoundType.CLOSED) {
          date = date.plusDays(1);
        }
        predicate.and(sample.date.before(date));
      }
    }
    if (ownerContains != null) {
      predicate.and(
          sample.owner.email.contains(ownerContains).or(sample.owner.name.contains(ownerContains)));
    }
    return predicate.hasValue() ? predicate.getValue() : Expressions.asBoolean(true).isTrue();
  }

  public Pageable pageable() {
    return PageRequest.of(page, size, sort != null ? sort : Sort.unsorted());
  }

  private String replaceNull(String value) {
    return Objects.toString(value, "");
  }
}
