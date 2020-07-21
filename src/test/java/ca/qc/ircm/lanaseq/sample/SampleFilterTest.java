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

package ca.qc.ircm.lanaseq.sample;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
public class SampleFilterTest {
  private SampleFilter filter = new SampleFilter();

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
    assertFalse(filter.test(protocolOwner(null, null)));
    assertFalse(filter.test(protocolOwner("", "")));
    assertFalse(filter.test(protocolOwner("My test", null)));
    assertFalse(filter.test(protocolOwner("My test", "")));
    assertFalse(filter.test(protocolOwner(null, "my.test@abc.com")));
    assertFalse(filter.test(protocolOwner("", "my.test@abc.com")));
  }

  private Sample name(String name) {
    Sample sample = new Sample();
    sample.setName(name);
    return sample;
  }

  private Sample date(LocalDateTime date) {
    Sample sample = new Sample();
    sample.setCreationDate(date);
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
}
