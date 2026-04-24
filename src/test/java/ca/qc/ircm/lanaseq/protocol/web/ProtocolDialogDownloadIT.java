package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.AbstractSeleniumTestCase;
import ca.qc.ircm.lanaseq.test.config.SeleniumTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ProtocolDialog}.
 */
@SeleniumTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogDownloadIT extends AbstractSeleniumTestCase {

  @Value("${download-home}")
  protected Path downloadHome;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void downloadFile() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("FLAG Protocol.docx");
    Files.deleteIfExists(downloaded);
    Path source = Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI());
    open();
    ProtocolsViewPage view = waitUntil(ProtocolsViewPage.find());
    view.protocols().select(0);
    view.edit().click();
    ProtocolDialogComponent dialog = waitUntil(ProtocolDialogComponent.find());
    dialog.files().filename(0).click();

    // Wait for file to download.
    Thread.sleep(2000);
    assertTrue(Files.exists(downloaded));
    try {
      assertArrayEquals(Files.readAllBytes(source), Files.readAllBytes(downloaded));
    } finally {
      Files.delete(downloaded);
    }
  }
}
