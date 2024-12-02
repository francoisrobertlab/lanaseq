package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVERED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.Download;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link ProtocolHistoryDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("francois.robert@ircm.qc.ca")
public class ProtocolHistoryDialogItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolHistoryDialog.class);
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Autowired
  private MessageSource messageSource;
  @Value("${download-home}")
  protected Path downloadHome;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(2);
    view.history().click();
    ProtocolHistoryDialogElement dialog = view.historyDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.files()).isPresent());
  }

  @Test
  public void recover() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(2);
    view.history().click();
    ProtocolHistoryDialogElement dialog = view.historyDialog();

    TestTransaction.flagForCommit();
    dialog.files().recover(0).click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + RECOVERED,
            new Object[] { "Histone FLAG Protocol.docx" }, currentLocale()),
        notification.getText());
    ProtocolFile file = fileRepository.findById(3L).orElseThrow();
    assertEquals("Histone FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(Paths.get(Objects
        .requireNonNull(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx")).toURI())),
        file.getContent());
    assertFalse(file.isDeleted());
    assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getCreationDate());
  }

  @Test
  @Download
  public void downloadFile() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("Histone FLAG Protocol.docx");
    Files.deleteIfExists(downloaded);
    Path source = Paths.get(Objects
        .requireNonNull(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx")).toURI());
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
