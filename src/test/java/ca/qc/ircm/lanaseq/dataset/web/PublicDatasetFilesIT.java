package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.PublicDatasetFiles.REST_MAPPING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;

/**
 * Integration tests for {@link PublicDatasetFiles} using Selenium.
 */
@TestBenchTestAnnotations
@WithAnonymousUser
public class PublicDatasetFilesIT extends AbstractSeleniumTestCase {

  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;

  @Test
  public void publicFile() throws Throwable {
    Dataset dataset = repository.findById(6L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);

    openView(REST_MAPPING + "/" + dataset.getName() + "/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");

    WebElement body = waitUntil(d -> d.findElement(By.cssSelector("body")));
    assertEquals(Files.readString(file1).replaceAll("\\n", " "), body.getText());
  }
}
