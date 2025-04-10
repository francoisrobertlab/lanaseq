package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchBrowser;
import ca.qc.ircm.lanaseq.test.config.Download;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link PublicFilesView}.
 */
@TestBenchTestAnnotations
@ActiveProfiles({"integration-test", "context-path"})
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PublicFilesViewContextPathItTest extends AbstractTestBenchBrowser {

  @Value("${download-home}")
  protected Path downloadHome;

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
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
      Assertions.assertEquals(4, lines.size());
      Assertions.assertEquals(homeUrl()
              + "sample-file/JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211/JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw",
          lines.get(0));
      Assertions.assertEquals(homeUrl()
              + "sample-file/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw",
          lines.get(1));
      Assertions.assertEquals(homeUrl()
              + "dataset-file/ChIPseq_Spt16_yFR101_G24D_JS3_20181211/ChIPseq_Spt16_yFR101_G24D_JS3_20181211.bw",
          lines.get(2));
      Assertions.assertEquals(homeUrl()
              + "dataset-file/ChIPseq_Spt16_yFR101_G24D_JS1_20181208/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw",
          lines.get(3));
    } finally {
      Files.delete(downloaded);
    }
  }

  @BrowserTest
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
      Assertions.assertEquals(2, lines.size());
      Assertions.assertEquals(homeUrl()
              + "sample-file/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw",
          lines.get(0));
      Assertions.assertEquals(homeUrl()
              + "dataset-file/ChIPseq_Spt16_yFR101_G24D_JS1_20181208/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw",
          lines.get(1));
    } finally {
      Files.delete(downloaded);
    }
  }
}
