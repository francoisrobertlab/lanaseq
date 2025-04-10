package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.SAVED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchBrowser;
import ca.qc.ircm.lanaseq.test.config.Download;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link ProtocolDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogItTest extends AbstractTestBenchBrowser {

  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolDialog.class);
  @Value("${download-home}")
  protected Path downloadHome;
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Autowired
  private MessageSource messageSource;
  private final String name = "test protocol";
  private final String note = "test note\nsecond line";
  private Path file1;
  private Path file2;

  @BeforeEach
  public void beforeTest() throws Throwable {
    file1 = Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI());
    file2 = Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx"))
            .toURI());
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void setFields(ProtocolDialogElement dialog) {
    dialog.name().setValue(name);
    dialog.note().setValue(note);
    dialog.upload().upload(file1.toFile());
    dialog.upload().upload(file2.toFile());
  }

  @BrowserTest
  public void fieldsExistence() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(0);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::name).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::upload).isPresent());
    assertTrue(optional(dialog::files).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertFalse(optional(dialog::delete).isPresent());
    assertTrue(optional(dialog::confirm).isPresent());
  }

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void fieldsExistence_Deletable() {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(3);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::name).isPresent());
    assertTrue(optional(dialog::note).isPresent());
    assertTrue(optional(dialog::upload).isPresent());
    assertTrue(optional(dialog::files).isPresent());
    assertTrue(optional(dialog::save).isPresent());
    assertTrue(optional(dialog::cancel).isPresent());
    assertTrue(optional(dialog::delete).isPresent());
    assertTrue(optional(dialog::confirm).isPresent());
  }

  @BrowserTest
  public void save_New() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.add().click();
    ProtocolDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name}, currentLocale()),
        notification.getText());
    Protocol protocol = repository.findByName(name).orElseThrow();
    assertNotEquals(0, protocol.getId());
    Assertions.assertEquals(name, protocol.getName());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(protocol.getCreationDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(protocol.getCreationDate()));
    Assertions.assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    Assertions.assertEquals(2, files.size());
    ProtocolFile file = files.get(0);
    Assertions.assertEquals("FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file1), file.getContent());
    file = files.get(1);
    Assertions.assertEquals("Histone_FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file2), file.getContent());
  }

  @BrowserTest
  public void save_Update() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(0);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name}, currentLocale()),
        notification.getText());
    Protocol protocol = repository.findById(1L).orElseThrow();
    Assertions.assertEquals(name, protocol.getName());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    Assertions.assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    Assertions.assertEquals(3, files.size());
    ProtocolFile file = files.get(0);
    Assertions.assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(Paths.get(
            Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI())),
        file.getContent());
    file = files.get(1);
    Assertions.assertEquals("FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file1), file.getContent());
    file = files.get(2);
    Assertions.assertEquals("Histone_FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file2), file.getContent());
  }

  @BrowserTest
  public void cancel() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(0);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Protocol protocol = repository.findById(1L).orElseThrow();
    Assertions.assertEquals("FLAG", protocol.getName());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    Assertions.assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    Assertions.assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    Assertions.assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(Paths.get(
            Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI())),
        file.getContent());
  }

  @BrowserTest
  @Download
  public void downloadFile() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("FLAG Protocol.docx");
    Files.deleteIfExists(downloaded);
    Path source = Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI());
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(0);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
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

  @BrowserTest
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void delete() {
    open();
    Protocol protocol = repository.findById(4L).get();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).waitForFirst();
    view.protocols().select(3);
    view.edit().click();
    ProtocolDialogElement dialog = view.dialog();
    final String name = protocol.getName();

    TestTransaction.flagForCommit();
    dialog.delete().click();
    dialog.confirm().getConfirmButton().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[]{name}, currentLocale()),
        notification.getText());
    assertFalse(repository.findById(4L).isPresent());
  }
}
