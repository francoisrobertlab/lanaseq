package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link AddSampleFilesDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddSampleFilesDialogItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(AddSampleFilesDialog.class);
  @TempDir
  Path temporaryFolder;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  private Path file1;
  private Path file2;

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
    setUpload(Files.createDirectory(temporaryFolder.resolve("upload")));
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void copyFiles(Sample sample)
      throws IOException, URISyntaxException, SecurityException, IllegalArgumentException {
    Path folder = configuration.getUpload().folder(sample);
    Files.createDirectories(folder);
    file1 = folder.resolve("R1.fastq");
    file2 = folder.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        file2);
  }

  @Test
  public void fieldsExistence() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(0);
    SampleFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    AddSampleFilesDialogElement dialog = filesDialog.addFilesDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.message()).isPresent());
    assertTrue(optional(() -> dialog.files()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
  }

  @Test
  public void refresh_Files() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    AddSampleFilesDialogElement dialog = filesDialog.addFilesDialog();
    Sample sample = repository.findById(10L).orElseThrow();
    assertEquals(0, dialog.files().getRowCount());
    copyFiles(sample);
    Thread.sleep(2500);
    assertEquals(2, dialog.files().getRowCount());
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        configuration.getUpload().folder(sample).resolve("other.fastq"));
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve("prefix_" + sample.getName() + "_R1"));
    Thread.sleep(2500);
    assertEquals(4, dialog.files().getRowCount());
  }

  @Test
  public void save() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    final AddSampleFilesDialogElement dialog = filesDialog.addFilesDialog();
    Sample sample = repository.findById(10L).orElseThrow();
    copyFiles(sample);
    String filenameInRoot = "prefix_" + sample.getName() + "_R1";
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve(filenameInRoot));

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED,
        new Object[] { 3, sample.getName() }, currentLocale()), notification.getText());
    Path folder = configuration.getHome().folder(sample);
    Path upload = configuration.getUpload().folder(sample);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertTrue(Files.exists(folder.resolve(file2.getFileName())));
    assertFalse(Files.exists(upload.getParent().resolve(filenameInRoot)));
    assertFalse(Files.exists(upload.resolve(file1.getFileName())));
    assertFalse(Files.exists(upload.resolve(file2.getFileName())));
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file2.getFileName())));
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(filenameInRoot)));
  }
}
