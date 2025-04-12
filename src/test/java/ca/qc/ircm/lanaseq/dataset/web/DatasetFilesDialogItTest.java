package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES_SUCCESS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFile;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFileRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.Download;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link DatasetFilesDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetFilesDialogItTest extends AbstractBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(DatasetFilesDialog.class);
  @Value("${download-home}")
  protected Path downloadHome;
  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private DatasetPublicFileRepository datasetPublicFileRepository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  private Path file1;

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
    setArchive(Files.createDirectory(temporaryFolder.resolve("archives")));
    setUpload(Files.createDirectory(temporaryFolder.resolve("upload")));
    file1 = Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI());
  }

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void fieldsExistence() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(0);
    DatasetFilesDialogElement dialog = view.filesDialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::message).isPresent());
    assertTrue(optional(dialog::folders).isPresent());
    assertTrue(optional(dialog::files).isPresent());
    assertTrue(optional(dialog::samples).isPresent());
    assertTrue(optional(dialog::refresh).isPresent());
    assertTrue(optional(dialog::upload).isPresent());
    assertTrue(optional(dialog::addLargeFiles).isPresent());
  }

  @BrowserTest
  public void files() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Path archive = configuration.getArchives().get(0).folder(dataset);
    Files.createDirectories(archive);
    Path file2 = archive.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        file2);
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();
    Assertions.assertEquals(2, dialog.folders().labels().size());
    Assertions.assertEquals(configuration.getHome().label(dataset, !SystemUtils.IS_OS_WINDOWS),
        dialog.folders().labels().get(0).getText());
    Assertions.assertEquals(
        configuration.getArchives().get(0).label(dataset, !SystemUtils.IS_OS_WINDOWS),
        dialog.folders().labels().get(1).getText());
    Assertions.assertEquals(2, dialog.files().getRowCount());
    Assertions.assertEquals(file1.getFileName().toString(), dialog.files().filename(0));
    Assertions.assertEquals(file2.getFileName().toString(), dialog.files().filename(1));
  }

  @BrowserTest
  public void rename() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.files().getRow(0).doubleClick();
    dialog.filenameEdit().setValue(dataset.getName() + "_R1.fastq");
    dialog.filenameEdit().sendKeys(Keys.ENTER);
    Thread.sleep(1000);

    assertTrue(Files.exists(file.resolveSibling(dataset.getName() + "_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(file.resolveSibling(dataset.getName() + "_R1.fastq")));
    assertFalse(Files.exists(file));
  }

  @BrowserTest
  @Download
  public void download() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("R1.fastq");
    Files.deleteIfExists(downloaded);
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.files().download(0).click();

    // Wait for file to download.
    Thread.sleep(2000);
    assertTrue(Files.exists(downloaded));
    try {
      assertArrayEquals(Files.readAllBytes(file), Files.readAllBytes(downloaded));
    } finally {
      Files.delete(downloaded);
    }
  }

  @BrowserTest
  public void publicFile() throws Throwable {
    Dataset dataset = repository.findById(6L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(1);
    DatasetFilesDialogElement dialog = view.filesDialog();

    TestTransaction.flagForCommit();
    dialog.files().publicFileCheckbox(0).setChecked(true);
    Thread.sleep(1000); // Wait for insert to complete.
    TestTransaction.end();

    Optional<DatasetPublicFile> optionalDatasetPublicFile = datasetPublicFileRepository.findByDatasetAndPath(
        dataset, "R1.fastq");
    assertTrue(optionalDatasetPublicFile.isPresent());
    DatasetPublicFile datasetPublicFile = optionalDatasetPublicFile.orElseThrow();
    Assertions.assertEquals(LocalDate.now().plus(configuration.getPublicFilePeriod()),
        datasetPublicFile.getExpiryDate());
  }

  @BrowserTest
  public void delete() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.files().delete(0).click();
    Thread.sleep(1000);

    assertFalse(Files.exists(file));
    Path deleted = folder.resolve(DELETED_FILENAME);
    List<String> deletedLines = Files.readAllLines(deleted);
    String[] deletedFileColumns = deletedLines.get(deletedLines.size() - 1).split("\t", -1);
    Assertions.assertEquals(3, deletedFileColumns.length);
    Assertions.assertEquals("R1.fastq", deletedFileColumns[0]);
    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    Assertions.assertEquals(modifiedTime,
        LocalDateTime.from(formatter.parse(deletedFileColumns[1])));
    LocalDateTime deletedTime = LocalDateTime.from(formatter.parse(deletedFileColumns[2]));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(deletedTime));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(deletedTime));
  }

  @BrowserTest
  public void viewFiles_Sample() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();
    dialog.samples().getCell(0, 0).doubleClick();

    assertTrue(dialog.sampleFilesDialog().isOpen());
  }

  @BrowserTest
  public void refresh() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Assertions.assertEquals(0, dialog.files().getRowCount());

    dialog.refresh().click();

    Assertions.assertEquals(1, dialog.files().getRowCount());
    Assertions.assertEquals(file1.getFileName().toString(), dialog.files().filename(0));
  }

  @BrowserTest
  public void upload() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();
    Dataset dataset = repository.findById(2L).orElseThrow();

    dialog.upload().upload(file1.toFile());

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + FILES_SUCCESS, new Object[]{file1.getFileName()},
            currentLocale()), notification.getText());
    Path folder = configuration.getHome().folder(dataset);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
  }

  @BrowserTest
  public void addLargeFiles() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.addLargeFiles().click();

    assertTrue(dialog.addFilesDialog().isOpen());
  }
}
