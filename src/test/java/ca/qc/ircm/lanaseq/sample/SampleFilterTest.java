package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.sample.QSample.sample;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ID;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.google.common.collect.Range;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

/**
 * Tests for {@link SampleFilter}.
 */
@NonTransactionalTestAnnotations
public class SampleFilterTest {
  private SampleFilter filter = new SampleFilter();

  @Test
  public void test_NameContains() {
    filter.nameContains = "test";

    assertTrue(filter.test(name("My test")));
    assertTrue(filter.test(name("Test my")));
    assertTrue(filter.test(name("My test my")));
    assertTrue(filter.test(name("My TEST my")));
    assertFalse(filter.test(name("")));
    assertFalse(filter.test(name("christian")));
  }

  @Test
  public void test_NameContainsNull() {
    filter.nameContains = null;

    assertTrue(filter.test(name("My test")));
    assertTrue(filter.test(name("Test my")));
    assertTrue(filter.test(name("My test my")));
    assertTrue(filter.test(name("My TEST my")));
    assertTrue(filter.test(name("")));
    assertTrue(filter.test(name("christian")));
  }

  @Test
  public void test_KeywordsContains() {
    filter.keywordsContains = "test";

    assertTrue(filter.test(keywords("My test")));
    assertTrue(filter.test(keywords("Test my")));
    assertTrue(filter.test(keywords("My test my")));
    assertTrue(filter.test(keywords("My TEST my")));
    assertFalse(filter.test(keywords()));
    assertFalse(filter.test(keywords("")));
    assertFalse(filter.test(keywords("christian")));
    assertTrue(filter.test(keywords("My test", "keyword1")));
    assertTrue(filter.test(keywords("Test my", "keyword1")));
    assertTrue(filter.test(keywords("My test my", "keyword1")));
    assertTrue(filter.test(keywords("My TEST my", "keyword1")));
    assertFalse(filter.test(keywords("", "keyword1")));
    assertFalse(filter.test(keywords("christian", "keyword1")));
    assertTrue(filter.test(keywords("keyword1", "My test")));
    assertTrue(filter.test(keywords("keyword1", "Test my")));
    assertTrue(filter.test(keywords("keyword1", "My test my")));
    assertTrue(filter.test(keywords("keyword1", "My TEST my")));
    assertFalse(filter.test(keywords("keyword1", "")));
    assertFalse(filter.test(keywords("keyword1", "christian")));
  }

  @Test
  public void test_KeywordsContainsNull() {
    filter.keywordsContains = null;

    assertTrue(filter.test(keywords("My test")));
    assertTrue(filter.test(keywords("Test my")));
    assertTrue(filter.test(keywords("My test my")));
    assertTrue(filter.test(keywords("My TEST my")));
    assertTrue(filter.test(keywords()));
    assertTrue(filter.test(keywords("")));
    assertTrue(filter.test(keywords("christian")));
    assertTrue(filter.test(keywords("My test", "keyword1")));
    assertTrue(filter.test(keywords("Test my", "keyword1")));
    assertTrue(filter.test(keywords("My test my", "keyword1")));
    assertTrue(filter.test(keywords("My TEST my", "keyword1")));
    assertTrue(filter.test(keywords("", "keyword1")));
    assertTrue(filter.test(keywords("christian", "keyword1")));
    assertTrue(filter.test(keywords("keyword1", "My test")));
    assertTrue(filter.test(keywords("keyword1", "Test my")));
    assertTrue(filter.test(keywords("keyword1", "My test my")));
    assertTrue(filter.test(keywords("keyword1", "My TEST my")));
    assertTrue(filter.test(keywords("keyword1", "")));
    assertTrue(filter.test(keywords("keyword1", "christian")));
  }

  @Test
  public void test_ProtocolContains() {
    filter.protocolContains = "test";

    assertTrue(filter.test(protocol("My test")));
    assertTrue(filter.test(protocol("Test my")));
    assertTrue(filter.test(protocol("My test my")));
    assertTrue(filter.test(protocol("My TEST my")));
    assertFalse(filter.test(protocol("Christian")));
    assertFalse(filter.test(protocol("")));
  }

  @Test
  public void test_ProtocolContainsNull() {
    filter.protocolContains = null;

    assertTrue(filter.test(protocol("My test")));
    assertTrue(filter.test(protocol("Test my")));
    assertTrue(filter.test(protocol("My test my")));
    assertTrue(filter.test(protocol("My TEST my")));
    assertTrue(filter.test(protocol("Christian")));
    assertTrue(filter.test(protocol("")));
  }

  @Test
  public void test_DateRange() {
    LocalDate from = LocalDate.of(2011, 1, 2);
    LocalDate to = LocalDate.of(2011, 10, 9);
    filter.dateRange = Range.closed(from, to);

    assertFalse(filter.test(date(LocalDate.of(2011, 1, 1))));
    assertTrue(filter.test(date(LocalDate.of(2011, 1, 2))));
    assertTrue(filter.test(date(LocalDate.of(2011, 10, 8))));
    assertTrue(filter.test(date(LocalDate.of(2011, 10, 9))));
    assertFalse(filter.test(date(LocalDate.of(2011, 12, 1))));
    assertFalse(filter.test(date(LocalDate.of(2011, 1, 1))));
  }

  @Test
  public void test_DateRange_Null() {
    filter.dateRange = null;

    assertTrue(filter.test(date(LocalDate.of(2011, 1, 1))));
    assertTrue(filter.test(date(LocalDate.of(2011, 1, 2))));
    assertTrue(filter.test(date(LocalDate.of(2011, 10, 8))));
    assertTrue(filter.test(date(LocalDate.of(2011, 10, 9))));
    assertTrue(filter.test(date(LocalDate.of(2011, 12, 1))));
    assertTrue(filter.test(date(LocalDate.of(2011, 1, 1))));
  }

  @Test
  public void test_OwnerContains() {
    filter.ownerContains = "test";

    assertTrue(filter.test(owner("My.test@abc.com", "Christian")));
    assertTrue(filter.test(owner("Test.my@abc.com", "Christian")));
    assertTrue(filter.test(owner("My.test.my@abc.com", "Christian")));
    assertTrue(filter.test(owner("My.TEST.my@abc.com", "Christian")));
    assertTrue(filter.test(owner("abc@test.com", "Christian")));
    assertTrue(filter.test(owner("abc@a.test", "Christian")));
    assertTrue(filter.test(owner("test@test.com", "Christian")));
    assertTrue(filter.test(owner("christian@abc.com", "My test")));
    assertTrue(filter.test(owner("christian@abc.com", "Test my")));
    assertTrue(filter.test(owner("christian@abc.com", "My test my")));
    assertTrue(filter.test(owner("christian@abc.com", "My TEST my")));
    assertFalse(filter.test(owner("", "Christian")));
    assertFalse(filter.test(owner("christian@abc.com", "")));
    assertFalse(filter.test(owner("christian@abc.com", "Christian")));
  }

  @Test
  public void test_OwnerContainsNull() {
    filter.ownerContains = null;

    assertTrue(filter.test(owner("My.test@abc.com", "Christian")));
    assertTrue(filter.test(owner("Test.my@abc.com", "Christian")));
    assertTrue(filter.test(owner("My.test.my@abc.com", "Christian")));
    assertTrue(filter.test(owner("My.TEST.my@abc.com", "Christian")));
    assertTrue(filter.test(owner("abc@test.com", "Christian")));
    assertTrue(filter.test(owner("abc@a.test", "Christian")));
    assertTrue(filter.test(owner("test@test.com", "Christian")));
    assertTrue(filter.test(owner("christian@abc.com", "My test")));
    assertTrue(filter.test(owner("christian@abc.com", "Test my")));
    assertTrue(filter.test(owner("christian@abc.com", "My test my")));
    assertTrue(filter.test(owner("christian@abc.com", "My TEST my")));
    assertTrue(filter.test(owner("", "Christian")));
    assertTrue(filter.test(owner("christian@abc.com", "")));
    assertTrue(filter.test(owner("christian@abc.com", "Christian")));
  }

  @Test
  public void test_ProtocolAndOwnerContains() {
    filter.protocolContains = "test";
    filter.ownerContains = "test";

    assertTrue(filter.test(protocolOwner("My test", "My.test@abc.com")));
    assertTrue(filter.test(protocolOwner("Test my", "Test.my@abc.com")));
    assertTrue(filter.test(protocolOwner("My test my", "My.test.my@abc.com")));
    assertTrue(filter.test(protocolOwner("My TEST my", "My.TEST.my@abc.com")));
    assertTrue(filter.test(protocolOwner("My test", "abc@test.com")));
    assertTrue(filter.test(protocolOwner("My test", "abc@a.test")));
    assertTrue(filter.test(protocolOwner("My test", "test@test.com")));
    assertFalse(filter.test(protocolOwner("christian", "My.test@abc.com")));
    assertFalse(filter.test(protocolOwner("christian", "Test.my@abc.com")));
    assertFalse(filter.test(protocolOwner("christian", "My.test.my@abc.com")));
    assertFalse(filter.test(protocolOwner("christian", "My.TEST.my@abc.com")));
    assertFalse(filter.test(protocolOwner("christian", "abc@test.com")));
    assertFalse(filter.test(protocolOwner("christian", "abc@a.test")));
    assertFalse(filter.test(protocolOwner("christian", "test@test.com")));
    assertFalse(filter.test(protocolOwner("My test", "christian@abc.com")));
    assertFalse(filter.test(protocolOwner("Test my", "christian@abc.com")));
    assertFalse(filter.test(protocolOwner("My test my", "christian@abc.com")));
    assertFalse(filter.test(protocolOwner("My TEST my", "christian@abc.com")));
    assertFalse(filter.test(protocolOwner("", "")));
    assertFalse(filter.test(protocolOwner("My test", "")));
    assertFalse(filter.test(protocolOwner("", "my.test@abc.com")));
  }

  private Sample name(String name) {
    Sample sample = new Sample();
    sample.setName(name);
    return sample;
  }

  private Sample keywords(String... keywords) {
    Sample sample = new Sample();
    sample.setKeywords(Stream.of(keywords).collect(Collectors.toSet()));
    return sample;
  }

  private Sample date(LocalDate date) {
    Sample sample = new Sample();
    sample.setDate(date);
    return sample;
  }

  private Sample protocol(String name) {
    Sample sample = new Sample();
    Protocol protocol = new Protocol();
    protocol.setName(name);
    sample.setProtocol(protocol);
    return sample;
  }

  private Sample owner(String email, String name) {
    Sample sample = new Sample();
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    sample.setOwner(user);
    return sample;
  }

  private Sample protocolOwner(String protocol, String email) {
    Sample sample = new Sample();
    sample.setProtocol(new Protocol(protocol));
    User user = new User();
    user.setEmail(email);
    sample.setOwner(user);
    return sample;
  }

  @Test
  public void predicate_Default() {
    Predicate predicate = filter.predicate();

    assertEquals(Expressions.asBoolean(true).isTrue(), predicate);
  }

  @Test
  public void predicate_NameContains() {
    filter.nameContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(sample.name.contains("test"), predicate);
  }

  @Test
  public void predicate_KeywordsContains() {
    filter.keywordsContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(sample.keywords.any().contains("test"), predicate);
  }

  @Test
  public void predicate_ProtocolContains() {
    filter.protocolContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(sample.protocol.name.contains("test"), predicate);
  }

  @Test
  public void predicate_Date_OpenRange() {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.open(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.goe(start.plusDays(1)).and(sample.date.before(end)), predicate);
  }

  @Test
  public void predicate_Date_ClosedRange() {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.closed(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.goe(start).and(sample.date.before(end.plusDays(1))), predicate);
  }

  @Test
  public void predicate_Date_OpenClosedRange() {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.openClosed(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.goe(start.plusDays(1)).and(sample.date.before(end.plusDays(1))),
        predicate);
  }

  @Test
  public void predicate_Date_ClosedOpenRange() {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.closedOpen(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.goe(start).and(sample.date.before(end)), predicate);
  }

  @Test
  public void predicate_Date_AtLeast() {
    LocalDate start = LocalDate.now().minusDays(10);
    filter.dateRange = Range.atLeast(start);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.goe(start), predicate);
  }

  @Test
  public void predicate_Date_GreaterThan() {
    LocalDate start = LocalDate.now().minusDays(10);
    filter.dateRange = Range.greaterThan(start);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.goe(start.plusDays(1)), predicate);
  }

  @Test
  public void predicate_Date_AtMost() {
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.atMost(end);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.before(end.plusDays(1)), predicate);
  }

  @Test
  public void predicate_Date_LessThan() {
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.lessThan(end);

    Predicate predicate = filter.predicate();

    assertEquals(sample.date.before(end), predicate);
  }

  @Test
  public void predicate_OwnerContains() {
    filter.ownerContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(sample.owner.email.contains("test").or(sample.owner.name.contains("test")),
        predicate);
  }

  @Test
  public void predicate_NameAndOwnerContains() {
    filter.nameContains = "test1";
    filter.ownerContains = "test2";

    Predicate predicate = filter.predicate();

    assertEquals(
        sample.name.contains("test1")
            .and(sample.owner.email.contains("test2").or(sample.owner.name.contains("test2"))),
        predicate);
  }

  @Test
  public void pageable_Default() {
    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Direction.ASC, ID)), pageable);
  }

  @Test
  public void pageable_Page() {
    filter.page = 2;
    filter.size = 5;

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(2, 5, Sort.by(Direction.ASC, ID)), pageable);
  }

  @Test
  public void pageable_Sort() {
    filter.sort = Sort.by(Order.asc(NAME), Order.desc(DATE));

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc(NAME), Order.desc(DATE))),
        pageable);
  }

  @Test
  public void pageable_NullSort() {
    filter.sort = null;

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()), pageable);
  }

  @Test
  public void pageable_PageAndSort() {
    filter.page = 2;
    filter.size = 5;
    filter.sort = Sort.by(Order.asc(NAME), Order.desc(DATE));

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(2, 5, Sort.by(Order.asc(NAME), Order.desc(DATE))), pageable);
  }
}
