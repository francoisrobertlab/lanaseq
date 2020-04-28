package ca.qc.ircm.lanaseq.dataset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.dataset.Experiment;
import ca.qc.ircm.lanaseq.dataset.ExperimentFilter;
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
public class ExperimentFilterTest {
  private ExperimentFilter filter = new ExperimentFilter();

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

  private Experiment name(String name) {
    Experiment experiment = new Experiment();
    experiment.setName(name);
    return experiment;
  }

  private Experiment project(String project) {
    Experiment experiment = new Experiment();
    experiment.setProject(project);
    return experiment;
  }

  private Experiment date(LocalDateTime date) {
    Experiment experiment = new Experiment();
    experiment.setDate(date);
    return experiment;
  }

  private Experiment protocol(String name) {
    Experiment experiment = new Experiment();
    Protocol protocol = new Protocol();
    protocol.setName(name);
    experiment.setProtocol(protocol);
    return experiment;
  }

  private Experiment owner(String email, String name) {
    Experiment experiment = new Experiment();
    User user = new User();
    user.setEmail(email);
    user.setName(name);
    experiment.setOwner(user);
    return experiment;
  }

  private Experiment nameOwner(String name, String email) {
    Experiment experiment = new Experiment();
    experiment.setName(name);
    User user = new User();
    user.setEmail(email);
    experiment.setOwner(user);
    return experiment;
  }
}
