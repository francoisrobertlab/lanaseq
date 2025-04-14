package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.AbstractLocalBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.testbench.BrowserTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ProtocolHistoryDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("francois.robert@ircm.qc.ca")
public class ProtocolHistoryDialogLocalItTest extends AbstractLocalBrowserTestCase {

  @Value("${download-home}")
  protected Path downloadHome;

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void downloadFile() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("Histone FLAG Protocol.docx");
    Files.deleteIfExists(downloaded);
    Path source = Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx"))
            .toURI());
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(2);
    view.history().click();
    ProtocolHistoryDialogElement dialog = view.historyDialog();
    AnchorElement filename = dialog.files().filename(0);
    filename.click();

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
