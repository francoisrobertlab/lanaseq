package ca.qc.ircm.lanaseq.files.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFile;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Range;

/**
 * Tests for {@link PublicFileFilter}.
 */
@NonTransactionalTestAnnotations
public class PublicFileFilterTest {

  private final PublicFileFilter filter = new PublicFileFilter();

  @Test
  public void test_FilenameContains() {
    filter.filenameContains = "test";

    assertTrue(filter.test(filename("My_test.txt")));
    assertTrue(filter.test(filename("Test_my.txt")));
    assertTrue(filter.test(filename("My_test_my.pdf")));
    assertTrue(filter.test(filename("My_TEST_my.txt")));
    assertFalse(filter.test(filename("")));
    assertFalse(filter.test(filename("christian.txt")));
  }

  @Test
  public void test_FilenameContains_Null() {
    filter.filenameContains = null;

    assertTrue(filter.test(filename("My_test.txt")));
    assertTrue(filter.test(filename("Test_my.txt")));
    assertTrue(filter.test(filename("My_test_my.pdf")));
    assertTrue(filter.test(filename("My_TEST_my.txt")));
    assertTrue(filter.test(filename("")));
    assertTrue(filter.test(filename("christian.txt")));
  }

  @Test
  public void test_ExpiryDateRange() {
    LocalDate from = LocalDate.of(2011, 1, 2);
    LocalDate to = LocalDate.of(2011, 10, 9);
    filter.expiryDateRange = Range.closed(from, to);

    assertFalse(filter.test(expiryDate(LocalDate.of(2011, 1, 1))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 1, 2))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 10, 8))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 10, 9))));
    assertFalse(filter.test(expiryDate(LocalDate.of(2011, 12, 1))));
    assertFalse(filter.test(expiryDate(LocalDate.of(2011, 1, 1))));
  }

  @Test
  public void test_DateRange_Null() {
    filter.expiryDateRange = null;

    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 1, 1))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 1, 2))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 10, 8))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 10, 9))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 12, 1))));
    assertTrue(filter.test(expiryDate(LocalDate.of(2011, 1, 1))));
  }

  @Test
  public void test_SampleNameContains() {
    filter.sampleNameContains = "test";

    assertTrue(filter.test(sampleName("My test")));
    assertTrue(filter.test(sampleName("Test my")));
    assertTrue(filter.test(sampleName("My test my")));
    assertTrue(filter.test(sampleName("My TEST my")));
    assertFalse(filter.test(sampleName("")));
    assertFalse(filter.test(sampleName("christian")));
  }

  @Test
  public void test_SampleNameContains_Null() {
    filter.sampleNameContains = null;

    assertTrue(filter.test(sampleName("My test")));
    assertTrue(filter.test(sampleName("Test my")));
    assertTrue(filter.test(sampleName("My test my")));
    assertTrue(filter.test(sampleName("My TEST my")));
    assertTrue(filter.test(sampleName("")));
    assertTrue(filter.test(sampleName("christian")));
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
  public void test_OwnerContains_Null() {
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
  public void test_FilenameAndOwnerContains() {
    filter.filenameContains = "test";
    filter.ownerContains = "test";

    assertTrue(filter.test(filenameOwner("My test", "My.test@abc.com")));
    assertTrue(filter.test(filenameOwner("Test my", "Test.my@abc.com")));
    assertTrue(filter.test(filenameOwner("My test my", "My.test.my@abc.com")));
    assertTrue(filter.test(filenameOwner("My TEST my", "My.TEST.my@abc.com")));
    assertTrue(filter.test(filenameOwner("My test", "abc@test.com")));
    assertTrue(filter.test(filenameOwner("My test", "abc@a.test")));
    assertTrue(filter.test(filenameOwner("My test", "test@test.com")));
    assertFalse(filter.test(filenameOwner("christian", "My.test@abc.com")));
    assertFalse(filter.test(filenameOwner("christian", "Test.my@abc.com")));
    assertFalse(filter.test(filenameOwner("christian", "My.test.my@abc.com")));
    assertFalse(filter.test(filenameOwner("christian", "My.TEST.my@abc.com")));
    assertFalse(filter.test(filenameOwner("christian", "abc@test.com")));
    assertFalse(filter.test(filenameOwner("christian", "abc@a.test")));
    assertFalse(filter.test(filenameOwner("christian", "test@test.com")));
    assertFalse(filter.test(filenameOwner("My test", "christian@abc.com")));
    assertFalse(filter.test(filenameOwner("Test my", "christian@abc.com")));
    assertFalse(filter.test(filenameOwner("My test my", "christian@abc.com")));
    assertFalse(filter.test(filenameOwner("My TEST my", "christian@abc.com")));
    assertFalse(filter.test(filenameOwner("", "")));
    assertFalse(filter.test(filenameOwner("My test", "")));
    assertFalse(filter.test(filenameOwner("", "my.test@abc.com")));
  }

  private PublicFile filename(String filename) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.setExpiryDate(LocalDate.now());
    samplePublicFile.setPath(filename);
    return new PublicFile(samplePublicFile);
  }

  private PublicFile expiryDate(LocalDate expiryDate) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.setExpiryDate(expiryDate);
    samplePublicFile.setPath("");
    return new PublicFile(samplePublicFile);
  }

  private PublicFile sampleName(String name) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setName(name);
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.setExpiryDate(LocalDate.now());
    samplePublicFile.setPath("");
    return new PublicFile(samplePublicFile);
  }

  private PublicFile owner(String email, String name) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.getSample().getOwner().setEmail(email);
    samplePublicFile.getSample().getOwner().setName(name);
    samplePublicFile.setExpiryDate(LocalDate.now());
    samplePublicFile.setPath("");
    return new PublicFile(samplePublicFile);
  }

  private PublicFile filenameOwner(String filename, String email) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.getSample().getOwner().setEmail(email);
    samplePublicFile.setExpiryDate(LocalDate.now());
    samplePublicFile.setPath(filename);
    return new PublicFile(samplePublicFile);
  }
}
