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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.google.common.collect.Range;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class DatasetFilterTest {
  private DatasetFilter filter = new DatasetFilter();

  @Test
  public void test_FilenameContains() {
    filter.filenameContains = "test";

    assertTrue(filter.test(filename("My test")));
    assertTrue(filter.test(filename("Test my")));
    assertTrue(filter.test(filename("My test my")));
    assertTrue(filter.test(filename("My TEST my")));
    assertFalse(filter.test(filename(null)));
    assertFalse(filter.test(filename("")));
    assertFalse(filter.test(filename("christian")));
  }

  @Test
  public void test_FilenameContainsNull() {
    filter.filenameContains = null;

    assertTrue(filter.test(filename("My test")));
    assertTrue(filter.test(filename("Test my")));
    assertTrue(filter.test(filename("My test my")));
    assertTrue(filter.test(filename("My TEST my")));
    assertTrue(filter.test(filename(null)));
    assertTrue(filter.test(filename("")));
    assertTrue(filter.test(filename("christian")));
  }

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
  public void test_ProjectContains() {
    filter.projectContains = "test";

    assertTrue(filter.test(project("My test")));
    assertTrue(filter.test(project("Test my")));
    assertTrue(filter.test(project("My test my")));
    assertTrue(filter.test(project("My TEST my")));
    assertFalse(filter.test(project(null)));
    assertFalse(filter.test(project("")));
    assertFalse(filter.test(project("christian")));
  }

  @Test
  public void test_ProjectContainsNull() {
    filter.projectContains = null;

    assertTrue(filter.test(project("My test")));
    assertTrue(filter.test(project("Test my")));
    assertTrue(filter.test(project("My test my")));
    assertTrue(filter.test(project("My TEST my")));
    assertTrue(filter.test(project(null)));
    assertTrue(filter.test(project("")));
    assertTrue(filter.test(project("christian")));
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
  public void test_dateRange() {
    LocalDate from = LocalDate.of(2011, 1, 2);
    LocalDate to = LocalDate.of(2011, 10, 9);
    filter.dateRange = Range.closed(from, to);

    assertFalse(filter.test(date(LocalDateTime.of(2011, 1, 1, 9, 40))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 1, 2, 9, 40))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 10, 8, 23, 40))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 10, 9, 23, 40))));
    assertFalse(filter.test(date(LocalDateTime.of(2011, 12, 1, 0, 0))));
    assertFalse(filter.test(date(LocalDateTime.of(2011, 1, 1, 0, 0))));
  }

  @Test
  public void test_dateRange_Null() {
    filter.dateRange = null;

    assertTrue(filter.test(date(LocalDateTime.of(2011, 1, 1, 9, 40))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 1, 2, 9, 40))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 10, 8, 23, 40))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 10, 9, 23, 40))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 12, 1, 0, 0))));
    assertTrue(filter.test(date(LocalDateTime.of(2011, 1, 1, 0, 0))));
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

  private Dataset filename(String filename) {
    Dataset dataset = mock(Dataset.class);
    when(dataset.getFilename()).thenReturn(filename);
    return dataset;
  }

  private Dataset name(String name) {
    Dataset dataset = new Dataset();
    dataset.setName(name);
    return dataset;
  }

  private Dataset project(String project) {
    Dataset dataset = new Dataset();
    dataset.setProject(project);
    return dataset;
  }

  private Dataset date(LocalDateTime date) {
    Dataset dataset = new Dataset();
    dataset.setDate(date);
    return dataset;
  }

  private Dataset protocol(String name) {
    Dataset dataset = new Dataset();
    Protocol protocol = new Protocol();
    protocol.setName(name);
    dataset.setProtocol(protocol);
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
}
