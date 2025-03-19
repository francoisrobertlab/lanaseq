package ca.qc.ircm.lanaseq.sample.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFile;
import ca.qc.ircm.lanaseq.sample.SamplePublicFileRepository;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link PublicSampleFiles}.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class PublicSampleFilesItTest extends AbstractTestBenchTestCase {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private SamplePublicFileRepository samplePublicFileRepository;
  @Autowired
  private AppConfiguration configuration;

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
    setArchive(Files.createDirectory(temporaryFolder.resolve("archives")));
    setUpload(Files.createDirectory(temporaryFolder.resolve("upload")));
  }

  @Test
  public void publicFile() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(sample);
    samplePublicFile.setPath("R1.fastq");
    samplePublicFile.setExpiryDate(LocalDate.now().plusDays(1));
    TestTransaction.flagForCommit();
    samplePublicFileRepository.save(samplePublicFile);
    TestTransaction.end();

    openView("sample-file/" + sample.getName() + "/R1.fastq");

    assertEquals(Files.readString(file1).replaceAll("\n", " "), $("body").waitForFirst().getText());
  }
}
