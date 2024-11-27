package ca.qc.ircm.lanaseq.protocol;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.google.common.collect.Range;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ProtocolFilter}.
 */
@NonTransactionalTestAnnotations
public class ProtocolFilterTest {
  private ProtocolFilter filter = new ProtocolFilter();

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
    assertFalse(filter.test(nameOwner("", "")));
    assertFalse(filter.test(nameOwner("My test", "")));
    assertFalse(filter.test(nameOwner("", "my.test@abc.com")));
  }

  private Protocol name(String name) {
    Protocol protocol = new Protocol();
    protocol.setName(name);
    return protocol;
  }

  private Protocol date(LocalDateTime date) {
    Protocol protocol = new Protocol();
    protocol.setCreationDate(date);
    return protocol;
  }

  private Protocol owner(String email, String name) {
    Protocol protocol = new Protocol();
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    protocol.setOwner(user);
    return protocol;
  }

  private Protocol nameOwner(String name, String email) {
    Protocol protocol = new Protocol();
    protocol.setName(name);
    User user = new User();
    user.setEmail(email);
    protocol.setOwner(user);
    return protocol;
  }
}
