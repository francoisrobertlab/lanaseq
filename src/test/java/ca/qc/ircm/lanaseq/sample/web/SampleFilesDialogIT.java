package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFile;
import ca.qc.ircm.lanaseq.sample.SamplePublicFileRepository;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link SampleFilesDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleFilesDialogIT extends AbstractBrowserTestCase {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private SamplePublicFileRepository samplePublicFileRepository;
  @Autowired
  private AppConfiguration configuration;

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
    setArchive(Files.createDirectory(temporaryFolder.resolve("archives")));
  }

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void fieldsExistence() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(0);
    SampleFilesDialogElement dialog = view.filesDialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::message).isPresent());
    assertTrue(optional(dialog::folders).isPresent());
    assertTrue(optional(dialog::files).isPresent());
    assertTrue(optional(dialog::refresh).isPresent());
    assertTrue(optional(dialog::upload).isPresent());
    assertTrue(optional(dialog::addLargeFiles).isPresent());
  }

  @BrowserTest
  public void files() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Path archive = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(archive);
    Path file2 = archive.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        file2);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();
    Assertions.assertEquals(2, dialog.folders().labels().size());
    Assertions.assertEquals(configuration.getHome().label(sample, !SystemUtils.IS_OS_WINDOWS),
        dialog.folders().labels().get(0).getText());
    Assertions.assertEquals(
        configuration.getArchives().get(0).label(sample, !SystemUtils.IS_OS_WINDOWS),
        dialog.folders().labels().get(1).getText());
    Assertions.assertEquals(2, dialog.files().getRowCount());
    Assertions.assertEquals(file1.getFileName().toString(), dialog.files().filename(0));
    Assertions.assertEquals(file2.getFileName().toString(), dialog.files().filename(1));
  }

  @BrowserTest
  public void rename() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();

    dialog.files().getRow(0).doubleClick();
    dialog.filenameEdit().setValue(sample.getName() + "_R1.fastq");
    dialog.filenameEdit().sendKeys(Keys.ENTER);
    Thread.sleep(1000); // Allow time to apply changes to files.

    assertTrue(Files.exists(file.resolveSibling(sample.getName() + "_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(file.resolveSibling(sample.getName() + "_R1.fastq")));
    assertFalse(Files.exists(file));
  }

  @BrowserTest
  public void publicFile() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();
    assertFalse(dialog.files().publicFileCheckbox(0).isChecked());

    TestTransaction.flagForCommit();
    dialog.files().publicFileCheckbox(0).click();
    Thread.sleep(1000); // Wait for insert to complete.
    TestTransaction.end();

    assertTrue(dialog.files().publicFileCheckbox(0).isChecked());
    Optional<SamplePublicFile> optionalSamplePublicFile = samplePublicFileRepository.findBySampleAndPath(
        sample, "R1.fastq");
    assertTrue(optionalSamplePublicFile.isPresent());
    SamplePublicFile samplePublicFile = optionalSamplePublicFile.orElseThrow();
    Assertions.assertEquals(LocalDate.now().plus(configuration.getPublicFilePeriod()),
        samplePublicFile.getExpiryDate());
  }

  @BrowserTest
  public void delete() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();

    dialog.files().delete(0).click();
    Thread.sleep(1000); // Allow time to apply changes to files.

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
  public void refresh() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
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
  public void addLargeFiles() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(0);
    SampleFilesDialogElement dialog = view.filesDialog();

    dialog.addLargeFiles().click();

    assertTrue(dialog.addFilesDialog().isOpen());
  }
}
