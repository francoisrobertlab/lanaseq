package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFileRepository;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.Download;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link PublicFilesView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PublicFilesViewItTest extends AbstractTestBenchTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(PublicFilesView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Value("${download-home}")
  protected Path downloadHome;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private SamplePublicFileRepository samplePublicFileRepository;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    open();

    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  public void title() {
    open();

    String applicationName = messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null,
        currentLocale());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[]{applicationName},
        currentLocale()), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() {
    open();
    PublicFilesViewElement view = $(PublicFilesViewElement.class).waitForFirst();
    assertTrue(optional(view::files).isPresent());
    assertTrue(optional(() -> view.files().filenameFilter()).isPresent());
    assertTrue(optional(() -> view.files().expiryDateFilter()).isPresent());
    assertTrue(optional(() -> view.files().sampleNameFilter()).isPresent());
    assertTrue(optional(() -> view.files().ownerFilter()).isPresent());
    assertTrue(optional(view::downloadLinks).isPresent());
  }

  @Test
  public void delete() {
    open();
    Sample sample = sampleRepository.findById(11L).orElseThrow();
    assertTrue(samplePublicFileRepository.findBySampleAndPath(sample,
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw").isPresent());
    PublicFilesViewElement view = $(PublicFilesViewElement.class).waitForFirst();
    assertEquals(4, view.files().getRowCount());

    view.files().delete(0).click();

    assertEquals(3, view.files().getRowCount());
    assertFalse(samplePublicFileRepository.findBySampleAndPath(sample,
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw").isPresent());
  }

  @Test
  @Download
  public void downloadLinks() throws IOException, InterruptedException {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("links.txt");
    Files.deleteIfExists(downloaded);
    open();
    PublicFilesViewElement view = $(PublicFilesViewElement.class).waitForFirst();

    view.downloadLinks().click();

    // Wait for file to download.
    Thread.sleep(2000);
    assertTrue(Files.exists(downloaded));
    try {
      List<String> lines = Files.readAllLines(downloaded);
      assertEquals(4, lines.size());
      assertEquals(baseUrl
              + "/sample-file/JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211/JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw",
          lines.get(0));
      assertEquals(baseUrl
              + "/sample-file/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw",
          lines.get(1));
      assertEquals(baseUrl
              + "/dataset-file/ChIPseq_Spt16_yFR101_G24D_JS3_20181211/ChIPseq_Spt16_yFR101_G24D_JS3_20181211.bw",
          lines.get(2));
      assertEquals(baseUrl
              + "/dataset-file/ChIPseq_Spt16_yFR101_G24D_JS1_20181208/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw",
          lines.get(3));
    } finally {
      Files.delete(downloaded);
    }
  }

  @Test
  @Download
  public void downloadLinks_FilterFilename() throws IOException, InterruptedException {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("links.txt");
    Files.deleteIfExists(downloaded);
    open();
    PublicFilesViewElement view = $(PublicFilesViewElement.class).waitForFirst();
    view.files().filenameFilter().setValue("JS1");

    view.downloadLinks().click();

    // Wait for file to download.
    Thread.sleep(2000);
    assertTrue(Files.exists(downloaded));
    try {
      List<String> lines = Files.readAllLines(downloaded);
      assertEquals(2, lines.size());
      assertEquals(baseUrl
              + "/sample-file/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw",
          lines.get(0));
      assertEquals(baseUrl
              + "/dataset-file/ChIPseq_Spt16_yFR101_G24D_JS1_20181208/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw",
          lines.get(1));
    } finally {
      Files.delete(downloaded);
    }
  }
}
