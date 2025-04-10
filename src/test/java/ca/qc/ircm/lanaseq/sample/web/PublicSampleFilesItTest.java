package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.PublicSampleFiles.REST_MAPPING;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFileRepository;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchBrowser;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;

/**
 * Integration tests for {@link PublicSampleFiles}.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class PublicSampleFilesItTest extends AbstractTestBenchBrowser {

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

  @BrowserTest
  public void publicFile() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);

    openView(
        REST_MAPPING + "/" + sample.getName() + "/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw");

    Assertions.assertEquals(Files.readString(file1).replaceAll("\n", " "),
        $("body").waitForFirst().getText());
  }
}
