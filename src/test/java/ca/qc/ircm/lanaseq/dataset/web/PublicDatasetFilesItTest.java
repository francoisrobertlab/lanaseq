package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.PublicDatasetFiles.REST_MAPPING;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFileRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
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
 * Integration tests for {@link PublicDatasetFiles}.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class PublicDatasetFilesItTest extends AbstractTestBenchBrowser {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private DatasetPublicFileRepository datasetPublicFileRepository;
  @Autowired
  private AppConfiguration configuration;

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
  }

  @BrowserTest
  public void publicFile() throws Throwable {
    Dataset dataset = repository.findById(6L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);

    openView(REST_MAPPING + "/" + dataset.getName() + "/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");

    Assertions.assertEquals(Files.readString(file1).replaceAll("\n", " "),
        $("body").waitForFirst().getText());
  }
}
