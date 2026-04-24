package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.PublicSampleFiles.REST_MAPPING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.SeleniumTestAnnotations;
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
 * Integration tests for {@link PublicSampleFiles} using Selenium.
 */
@SeleniumTestAnnotations
@WithAnonymousUser
public class PublicSampleFilesIT extends AbstractSeleniumTestCase {

  @Autowired
  private SampleRepository repository;
  @Autowired
  private AppConfiguration configuration;

  @Test
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

    WebElement body = waitUntil(d -> d.findElement(By.cssSelector("body")));
    assertEquals(Files.readString(file1).replaceAll("\\n", " "), body.getText());
  }
}
