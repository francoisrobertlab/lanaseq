package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.ID;
import static ca.qc.ircm.lanaseq.dataset.QDataset.dataset;
import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Filters datasets.
 */
public class DatasetFilter implements Predicate<Dataset> {
  public String nameContains;
  public String keywordsContains;
  public String protocolContains;
  public Range<LocalDate> dateRange;
  public String ownerContains;
  public Sort sort = Sort.by(Direction.ASC, ID);
  public Integer page = 0;
  public Integer size = Integer.MAX_VALUE;

  @Override
  public boolean test(Dataset dataset) {
    boolean test = true;
    if (nameContains != null) {
      test &= comparable(replaceNull(dataset.getName())).contains(comparable(nameContains));
    }
    if (keywordsContains != null) {
      test &= dataset.getKeywords().stream()
          .anyMatch(keyword -> comparable(keyword).contains(comparable(keywordsContains)));
    }
    if (protocolContains != null) {
      String protocol = !dataset.getSamples().isEmpty() ? Optional.of(dataset.getSamples().get(0))
          .map(Sample::getProtocol).map(Protocol::getName).orElse("") : "";
      test &= comparable(protocol).contains(comparable(protocolContains));
    }
    if (dateRange != null) {
      test &= dateRange.contains(dataset.getDate());
    }
    if (ownerContains != null) {
      test &=
          comparable(replaceNull(dataset.getOwner().getEmail())).contains(comparable(ownerContains))
              || comparable(replaceNull(dataset.getOwner().getName()))
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
      predicate.and(dataset.name.contains(nameContains));
    }
    if (keywordsContains != null) {
      predicate.and(dataset.keywords.any().contains(keywordsContains));
    }
    if (protocolContains != null) {
      predicate.and(dataset.samples.any().protocol.name.contains(protocolContains));
    }
    if (dateRange != null) {
      if (dateRange.hasLowerBound()) {
        LocalDate date = dateRange.lowerEndpoint();
        if (dateRange.lowerBoundType() == BoundType.OPEN) {
          date = date.plusDays(1);
        }
        predicate.and(dataset.date.goe(date));
      }
      if (dateRange.hasUpperBound()) {
        LocalDate date = dateRange.upperEndpoint();
        if (dateRange.upperBoundType() == BoundType.CLOSED) {
          date = date.plusDays(1);
        }
        predicate.and(dataset.date.before(date));
      }
    }
    if (ownerContains != null) {
      predicate.and(dataset.owner.email.contains(ownerContains)
          .or(dataset.owner.name.contains(ownerContains)));
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
