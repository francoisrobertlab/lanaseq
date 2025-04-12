package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link AddDatasetFilesDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddDatasetFilesDialogItTest extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(AddDatasetFilesDialog.class);
  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
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

  private void copyFiles(Dataset dataset)
      throws IOException, URISyntaxException, SecurityException, IllegalArgumentException {
    Path folder = configuration.getUpload().folder(dataset);
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

  @BrowserTest
  public void fieldsExistence() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(0);
    DatasetFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    AddDatasetFilesDialogElement dialog = filesDialog.addFilesDialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::message).isPresent());
    assertTrue(optional(dialog::files).isPresent());
    assertTrue(optional(dialog::refresh).isPresent());
    assertTrue(optional(dialog::save).isPresent());
  }

  @BrowserTest
  public void refresh() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    AddDatasetFilesDialogElement dialog = filesDialog.addFilesDialog();
    Dataset dataset = repository.findById(2L).orElseThrow();
    Assertions.assertEquals(0, dialog.files().getRowCount());
    copyFiles(dataset);

    dialog.refresh().click();

    Assertions.assertEquals(2, dialog.files().getRowCount());
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        configuration.getUpload().folder(dataset).resolve("other.fastq"));
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve("prefix_" + dataset.getName() + "_R1"));

    dialog.refresh().click();

    Assertions.assertEquals(4, dialog.files().getRowCount());
  }

  @BrowserTest
  public void save() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    final AddDatasetFilesDialogElement dialog = filesDialog.addFilesDialog();
    Dataset dataset = repository.findById(2L).orElseThrow();
    copyFiles(dataset);
    String filenameInRoot = "prefix_" + dataset.getName() + "_R1";
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve(filenameInRoot));

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{3, dataset.getName()},
            currentLocale()), notification.getText());
    Path folder = configuration.getHome().folder(dataset);
    Path upload = configuration.getUpload().folder(dataset);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertTrue(Files.exists(folder.resolve(file2.getFileName())));
    assertFalse(Files.exists(upload.getParent().resolve(filenameInRoot)));
    assertFalse(Files.exists(upload.resolve(file1.getFileName())));
    assertFalse(Files.exists(upload.resolve(file2.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file2.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(filenameInRoot)));
  }
}
