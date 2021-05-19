/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.dataset.QDataset.dataset;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ID;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.google.common.collect.Range;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

/**
 * Tests for {@link DatasetFilter}.
 */
@NonTransactionalTestAnnotations
public class DatasetFilterTest {
  private DatasetFilter filter = new DatasetFilter();

  @Test
  public void test_NameContains() {
    filter.nameContains = "test";

    assertTrue(filter.test(name("My test")));
    assertTrue(filter.test(name("Test my")));
    assertTrue(filter.test(name("My test my")));
    assertTrue(filter.test(name("My TEST my")));
    assertFalse(filter.test(name(null)));
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
    assertTrue(filter.test(name(null)));
    assertTrue(filter.test(name("")));
    assertTrue(filter.test(name("christian")));
  }

  @Test
  public void test_TagsContains() {
    filter.tagsContains = "test";

    assertTrue(filter.test(tags("My test")));
    assertTrue(filter.test(tags("Test my")));
    assertTrue(filter.test(tags("My test my")));
    assertTrue(filter.test(tags("My TEST my")));
    assertFalse(filter.test(tags()));
    assertFalse(filter.test(tags("")));
    assertFalse(filter.test(tags("christian")));
    assertTrue(filter.test(tags("My test", "tag1")));
    assertTrue(filter.test(tags("Test my", "tag1")));
    assertTrue(filter.test(tags("My test my", "tag1")));
    assertTrue(filter.test(tags("My TEST my", "tag1")));
    assertFalse(filter.test(tags("", "tag1")));
    assertFalse(filter.test(tags("christian", "tag1")));
    assertTrue(filter.test(tags("tag1", "My test")));
    assertTrue(filter.test(tags("tag1", "Test my")));
    assertTrue(filter.test(tags("tag1", "My test my")));
    assertTrue(filter.test(tags("tag1", "My TEST my")));
    assertFalse(filter.test(tags("tag1", "")));
    assertFalse(filter.test(tags("tag1", "christian")));
  }

  @Test
  public void test_TagsContainsNull() {
    filter.tagsContains = null;

    assertTrue(filter.test(tags("My test")));
    assertTrue(filter.test(tags("Test my")));
    assertTrue(filter.test(tags("My test my")));
    assertTrue(filter.test(tags("My TEST my")));
    assertTrue(filter.test(tags()));
    assertTrue(filter.test(tags("")));
    assertTrue(filter.test(tags("christian")));
    assertTrue(filter.test(tags("My test", "tag1")));
    assertTrue(filter.test(tags("Test my", "tag1")));
    assertTrue(filter.test(tags("My test my", "tag1")));
    assertTrue(filter.test(tags("My TEST my", "tag1")));
    assertTrue(filter.test(tags("", "tag1")));
    assertTrue(filter.test(tags("christian", "tag1")));
    assertTrue(filter.test(tags("tag1", "My test")));
    assertTrue(filter.test(tags("tag1", "Test my")));
    assertTrue(filter.test(tags("tag1", "My test my")));
    assertTrue(filter.test(tags("tag1", "My TEST my")));
    assertTrue(filter.test(tags("tag1", "")));
    assertTrue(filter.test(tags("tag1", "christian")));
  }

  @Test
  public void test_ProtocolContains() {
    filter.protocolContains = "test";

    assertTrue(filter.test(protocol("My test")));
    assertTrue(filter.test(protocol("Test my")));
    assertTrue(filter.test(protocol("My test my")));
    assertTrue(filter.test(protocol("My TEST my")));
    assertFalse(filter.test(protocol("Christian")));
    assertFalse(filter.test(protocol(null)));
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
    assertTrue(filter.test(protocol(null)));
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
    assertFalse(filter.test(owner(null, "Christian")));
    assertFalse(filter.test(owner("", "Christian")));
    assertFalse(filter.test(owner("christian@abc.com", null)));
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
    assertTrue(filter.test(owner(null, "Christian")));
    assertTrue(filter.test(owner("", "Christian")));
    assertTrue(filter.test(owner("christian@abc.com", null)));
    assertTrue(filter.test(owner("christian@abc.com", "")));
    assertTrue(filter.test(owner("christian@abc.com", "Christian")));
  }

  @Test
  public void test_NameAndOwnerContains() {
    filter.nameContains = "test";
    filter.ownerContains = "test";

    assertTrue(filter.test(nameOwner("My test", "My.test@abc.com")));
    assertTrue(filter.test(nameOwner("Test my", "Test.my@abc.com")));
    assertTrue(filter.test(nameOwner("My test my", "My.test.my@abc.com")));
    assertTrue(filter.test(nameOwner("My TEST my", "My.TEST.my@abc.com")));
    assertTrue(filter.test(nameOwner("My test", "abc@test.com")));
    assertTrue(filter.test(nameOwner("My test", "abc@a.test")));
    assertTrue(filter.test(nameOwner("My test", "test@test.com")));
    assertFalse(filter.test(nameOwner("christian", "My.test@abc.com")));
    assertFalse(filter.test(nameOwner("christian", "Test.my@abc.com")));
    assertFalse(filter.test(nameOwner("christian", "My.test.my@abc.com")));
    assertFalse(filter.test(nameOwner("christian", "My.TEST.my@abc.com")));
    assertFalse(filter.test(nameOwner("christian", "abc@test.com")));
    assertFalse(filter.test(nameOwner("christian", "abc@a.test")));
    assertFalse(filter.test(nameOwner("christian", "test@test.com")));
    assertFalse(filter.test(nameOwner("My test", "christian@abc.com")));
    assertFalse(filter.test(nameOwner("Test my", "christian@abc.com")));
    assertFalse(filter.test(nameOwner("My test my", "christian@abc.com")));
    assertFalse(filter.test(nameOwner("My TEST my", "christian@abc.com")));
    assertFalse(filter.test(nameOwner(null, null)));
    assertFalse(filter.test(nameOwner("", "")));
    assertFalse(filter.test(nameOwner("My test", null)));
    assertFalse(filter.test(nameOwner("My test", "")));
    assertFalse(filter.test(nameOwner(null, "my.test@abc.com")));
    assertFalse(filter.test(nameOwner("", "my.test@abc.com")));
  }

  private Dataset name(String name) {
    Dataset dataset = new Dataset();
    dataset.setName(name);
    return dataset;
  }

  private Dataset tags(String... tags) {
    Dataset dataset = new Dataset();
    dataset.setTags(Stream.of(tags).collect(Collectors.toSet()));
    return dataset;
  }

  private Dataset date(LocalDate date) {
    Dataset dataset = new Dataset();
    dataset.setDate(date);
    return dataset;
  }

  private Dataset protocol(String name) {
    Dataset dataset = new Dataset();
    Sample sample = new Sample();
    Protocol protocol = new Protocol();
    protocol.setName(name);
    sample.setProtocol(protocol);
    dataset.setSamples(new ArrayList<>());
    dataset.getSamples().add(sample);
    return dataset;
  }

  private Dataset owner(String email, String name) {
    Dataset dataset = new Dataset();
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    dataset.setOwner(user);
    return dataset;
  }

  private Dataset nameOwner(String name, String email) {
    Dataset dataset = new Dataset();
    dataset.setName(name);
    User user = new User();
    user.setEmail(email);
    dataset.setOwner(user);
    return dataset;
  }

  @Test
  public void predicate_Default() throws Exception {
    Predicate predicate = filter.predicate();

    assertEquals(Expressions.asBoolean(true).isTrue(), predicate);
  }

  @Test
  public void predicate_NameContains() throws Exception {
    filter.nameContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(dataset.name.contains("test"), predicate);
  }

  @Test
  public void predicate_TagsContains() throws Exception {
    filter.tagsContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(dataset.tags.any().contains("test"), predicate);
  }

  @Test
  public void predicate_ProtocolContains() throws Exception {
    filter.protocolContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(dataset.samples.any().protocol.name.contains("test"), predicate);
  }

  @Test
  public void predicate_Date_OpenRange() throws Exception {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.open(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.goe(start.plusDays(1)).and(dataset.date.before(end)), predicate);
  }

  @Test
  public void predicate_Date_ClosedRange() throws Exception {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.closed(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.goe(start).and(dataset.date.before(end.plusDays(1))), predicate);
  }

  @Test
  public void predicate_Date_OpenClosedRange() throws Exception {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.openClosed(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.goe(start.plusDays(1)).and(dataset.date.before(end.plusDays(1))),
        predicate);
  }

  @Test
  public void predicate_Date_ClosedOpenRange() throws Exception {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.closedOpen(start, end);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.goe(start).and(dataset.date.before(end)), predicate);
  }

  @Test
  public void predicate_Date_AtLeast() throws Exception {
    LocalDate start = LocalDate.now().minusDays(10);
    filter.dateRange = Range.atLeast(start);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.goe(start), predicate);
  }

  @Test
  public void predicate_Date_GreaterThan() throws Exception {
    LocalDate start = LocalDate.now().minusDays(10);
    filter.dateRange = Range.greaterThan(start);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.goe(start.plusDays(1)), predicate);
  }

  @Test
  public void predicate_Date_AtMost() throws Exception {
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.atMost(end);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.before(end.plusDays(1)), predicate);
  }

  @Test
  public void predicate_Date_LessThan() throws Exception {
    LocalDate end = LocalDate.now();
    filter.dateRange = Range.lessThan(end);

    Predicate predicate = filter.predicate();

    assertEquals(dataset.date.before(end), predicate);
  }

  @Test
  public void predicate_OwnerContains() throws Exception {
    filter.ownerContains = "test";

    Predicate predicate = filter.predicate();

    assertEquals(dataset.owner.email.contains("test").or(dataset.owner.name.contains("test")),
        predicate);
  }

  @Test
  public void predicate_NameAndOwnerContains() throws Exception {
    filter.nameContains = "test1";
    filter.ownerContains = "test2";

    Predicate predicate = filter.predicate();

    assertEquals(
        dataset.name.contains("test1")
            .and(dataset.owner.email.contains("test2").or(dataset.owner.name.contains("test2"))),
        predicate);
  }

  @Test
  public void pageable_Default() throws Exception {
    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Direction.ASC, ID)), pageable);
  }

  @Test
  public void pageable_Page() throws Exception {
    filter.page = 2;
    filter.size = 5;

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(2, 5, Sort.by(Direction.ASC, ID)), pageable);
  }

  @Test
  public void pageable_Sort() throws Exception {
    filter.sort = Sort.by(Order.asc(NAME), Order.desc(DATE));

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc(NAME), Order.desc(DATE))),
        pageable);
  }

  @Test
  public void pageable_NullSort() throws Exception {
    filter.sort = null;

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()), pageable);
  }

  @Test
  public void pageable_PageAndSort() throws Exception {
    filter.page = 2;
    filter.size = 5;
    filter.sort = Sort.by(Order.asc(NAME), Order.desc(DATE));

    Pageable pageable = filter.pageable();

    assertEquals(PageRequest.of(2, 5, Sort.by(Order.asc(NAME), Order.desc(DATE))), pageable);
  }
}
