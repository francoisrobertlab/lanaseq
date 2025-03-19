package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.PublicDatasetFiles.REST_MAPPING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFile;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFileRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
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
 * Integration tests for {@link PublicDatasetFiles}.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class PublicDatasetFilesItTest extends AbstractTestBenchTestCase {

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
    setArchive(Files.createDirectory(temporaryFolder.resolve("archives")));
    setUpload(Files.createDirectory(temporaryFolder.resolve("upload")));
  }

  @Test
  public void publicFile() throws Throwable {
    Dataset dataset = repository.findById(6L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    DatasetPublicFile datasetPublicFile = new DatasetPublicFile();
    datasetPublicFile.setDataset(dataset);
    datasetPublicFile.setPath("R1.fastq");
    datasetPublicFile.setExpiryDate(LocalDate.now().plusDays(1));
    TestTransaction.flagForCommit();
    datasetPublicFileRepository.save(datasetPublicFile);
    TestTransaction.end();

    openView(REST_MAPPING + "/" + dataset.getName() + "/R1.fastq");

    assertEquals(Files.readString(file1).replaceAll("\n", " "), $("body").waitForFirst().getText());
  }
}
