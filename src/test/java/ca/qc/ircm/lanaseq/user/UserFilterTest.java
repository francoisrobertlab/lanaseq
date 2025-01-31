package ca.qc.ircm.lanaseq.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UserFilter}.
 */
@NonTransactionalTestAnnotations
public class UserFilterTest {

  private UserFilter filter = new UserFilter();

  @Test
  public void test_EmailContains() {
    filter.emailContains = "test";

    assertTrue(filter.test(email("My.test@abc.com")));
    assertTrue(filter.test(email("Test.my@abc.com")));
    assertTrue(filter.test(email("My.test.my@abc.com")));
    assertTrue(filter.test(email("My.TEST.my@abc.com")));
    assertTrue(filter.test(email("abc@test.com")));
    assertTrue(filter.test(email("abc@a.test")));
    assertTrue(filter.test(email("test@test.com")));
    assertFalse(filter.test(email("")));
    assertFalse(filter.test(email("christian@abc.com")));
  }

  @Test
  public void test_EmailContainsNull() {
    filter.emailContains = null;

    assertTrue(filter.test(email("My.test@abc.com")));
    assertTrue(filter.test(email("Test.my@abc.com")));
    assertTrue(filter.test(email("My.test.my@abc.com")));
    assertTrue(filter.test(email("My.TEST.my@abc.com")));
    assertTrue(filter.test(email("abc@test.com")));
    assertTrue(filter.test(email("abc@a.test")));
    assertTrue(filter.test(email("test@test.com")));
    assertTrue(filter.test(email("")));
    assertTrue(filter.test(email("christian@abc.com")));
  }

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
  public void test_ActiveFalse() {
    filter.active = false;

    assertTrue(filter.test(active(false)));
    assertFalse(filter.test(active(true)));
  }

  @Test
  public void test_ActiveTrue() {
    filter.active = true;

    assertFalse(filter.test(active(false)));
    assertTrue(filter.test(active(true)));
  }

  @Test
  public void test_ActiveNull() {
    filter.active = null;

    assertTrue(filter.test(active(false)));
    assertTrue(filter.test(active(true)));
  }

  @Test
  public void test_EmailAndNameContains() {
    filter.emailContains = "test";
    filter.nameContains = "test";

    assertTrue(filter.test(emailName("My.test@abc.com", "My test")));
    assertTrue(filter.test(emailName("Test.my@abc.com", "Test my")));
    assertTrue(filter.test(emailName("My.test.my@abc.com", "My test my")));
    assertTrue(filter.test(emailName("My.TEST.my@abc.com", "My TEST my")));
    assertTrue(filter.test(emailName("abc@test.com", "My test")));
    assertTrue(filter.test(emailName("abc@a.test", "My test")));
    assertTrue(filter.test(emailName("test@test.com", "My test")));
    assertFalse(filter.test(emailName("My.test@abc.com", "christian")));
    assertFalse(filter.test(emailName("Test.my@abc.com", "christian")));
    assertFalse(filter.test(emailName("My.test.my@abc.com", "christian")));
    assertFalse(filter.test(emailName("My.TEST.my@abc.com", "christian")));
    assertFalse(filter.test(emailName("abc@test.com", "christian")));
    assertFalse(filter.test(emailName("abc@a.test", "christian")));
    assertFalse(filter.test(emailName("test@test.com", "christian")));
    assertFalse(filter.test(emailName("christian@abc.com", "My test")));
    assertFalse(filter.test(emailName("christian@abc.com", "Test my")));
    assertFalse(filter.test(emailName("christian@abc.com", "My test my")));
    assertFalse(filter.test(emailName("christian@abc.com", "My TEST my")));
    assertFalse(filter.test(emailName("", "")));
    assertFalse(filter.test(emailName("", "My test")));
    assertFalse(filter.test(emailName("my.test@abc.com", "")));
  }

  private User email(String email) {
    User user = new User();
    user.setEmail(email);
    return user;
  }

  private User name(String name) {
    User user = new User();
    user.setName(name);
    return user;
  }

  private User active(boolean active) {
    User user = new User();
    user.setActive(active);
    return user;
  }

  private User emailName(String email, String name) {
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    return user;
  }
}
