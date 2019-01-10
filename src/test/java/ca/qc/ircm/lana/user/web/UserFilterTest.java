package ca.qc.ircm.lana.user.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
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
    assertFalse(filter.test(email(null)));
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
    assertTrue(filter.test(email(null)));
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
  public void test_LaboratoryNameContains() {
    filter.laboratoryNameContains = "test";

    assertTrue(filter.test(laboratoryName("My test")));
    assertTrue(filter.test(laboratoryName("Test my")));
    assertTrue(filter.test(laboratoryName("My test my")));
    assertTrue(filter.test(laboratoryName("My TEST my")));
    assertFalse(filter.test(laboratoryName(null)));
    assertFalse(filter.test(laboratoryName("")));
    assertFalse(filter.test(laboratoryName("christian")));
  }

  @Test
  public void test_LaboratoryNameContainsNull() {
    filter.laboratoryNameContains = null;

    assertTrue(filter.test(laboratoryName("My test")));
    assertTrue(filter.test(laboratoryName("Test my")));
    assertTrue(filter.test(laboratoryName("My test my")));
    assertTrue(filter.test(laboratoryName("My TEST my")));
    assertTrue(filter.test(laboratoryName(null)));
    assertTrue(filter.test(laboratoryName("")));
    assertTrue(filter.test(laboratoryName("christian")));
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
    assertFalse(filter.test(emailName(null, null)));
    assertFalse(filter.test(emailName("", "")));
    assertFalse(filter.test(emailName(null, "My test")));
    assertFalse(filter.test(emailName("", "My test")));
    assertFalse(filter.test(emailName("my.test@abc.com", null)));
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

  private User laboratoryName(String name) {
    Laboratory laboratory = new Laboratory();
    laboratory.setName(name);
    User user = new User();
    user.setLaboratory(laboratory);
    return user;
  }

  private User emailName(String email, String name) {
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    return user;
  }
}
