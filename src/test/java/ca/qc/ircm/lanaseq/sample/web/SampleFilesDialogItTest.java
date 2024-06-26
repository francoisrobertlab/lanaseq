/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES_SUCCESS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.Download;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SampleFilesDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleFilesDialogItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(SampleFilesDialog.class);
  @TempDir
  Path temporaryFolder;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  @Value("${download-home}")
  protected Path downloadHome;
  private Path file1;

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
    setArchive(Files.createDirectory(temporaryFolder.resolve("archives")));
    setUpload(Files.createDirectory(temporaryFolder.resolve("upload")));
    file1 = Paths.get(getClass().getResource("/sample/R1.fastq").toURI());
  }

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(0);
    SampleFilesDialogElement dialog = view.filesDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.message()).isPresent());
    assertTrue(optional(() -> dialog.folders()).isPresent());
    assertTrue(optional(() -> dialog.files()).isPresent());
    assertTrue(optional(() -> dialog.upload()).isPresent());
    assertTrue(optional(() -> dialog.addLargeFiles()).isPresent());
  }

  @Test
  public void files() throws Throwable {
    Sample sample = repository.findById(10L).get();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file1);
    Path archive = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(archive);
    Path file2 = archive.resolve("R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file2);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();
    assertEquals(2, dialog.folders().labels().size());
    assertEquals(configuration.getHome().label(sample, !SystemUtils.IS_OS_WINDOWS),
        dialog.folders().labels().get(0).getText());
    assertEquals(configuration.getArchives().get(0).label(sample, !SystemUtils.IS_OS_WINDOWS),
        dialog.folders().labels().get(1).getText());
    assertEquals(2, dialog.files().getRowCount());
    assertEquals(file1.getFileName().toString(), dialog.files().filename(0));
    assertEquals(file2.getFileName().toString(), dialog.files().filename(1));
  }

  @Test
  public void rename() throws Throwable {
    Sample sample = repository.findById(10L).get();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();

    dialog.files().getRow(0).doubleClick();
    dialog.filenameEdit().setValue(sample.getName() + "_R1.fastq");
    dialog.filenameEdit().sendKeys(Keys.ENTER);
    Thread.sleep(1000); // Allow time to apply changes to files.

    assertTrue(Files.exists(file.resolveSibling(sample.getName() + "_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(file.resolveSibling(sample.getName() + "_R1.fastq")));
    assertFalse(Files.exists(file));
  }

  @Test
  @Download
  public void download() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("R1.fastq");
    Files.deleteIfExists(downloaded);
    Sample sample = repository.findById(10L).get();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();

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

  @Test
  public void delete() throws Throwable {
    Sample sample = repository.findById(10L).get();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file);
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
    assertEquals(3, deletedFileColumns.length);
    assertEquals("R1.fastq", deletedFileColumns[0]);
    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    assertEquals(modifiedTime, LocalDateTime.from(formatter.parse(deletedFileColumns[1])));
    LocalDateTime deletedTime = LocalDateTime.from(formatter.parse(deletedFileColumns[2]));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(deletedTime));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(deletedTime));
  }

  @Test
  public void upload() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(1);
    SampleFilesDialogElement dialog = view.filesDialog();
    Sample sample = repository.findById(10L).get();

    dialog.upload().upload(file1.toFile());

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FILES_SUCCESS,
        new Object[] { file1.getFileName() }, currentLocale()), notification.getText());
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
  }

  @Test
  public void addLargeFiles() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(0);
    SampleFilesDialogElement dialog = view.filesDialog();

    dialog.addLargeFiles().click();

    assertTrue(dialog.addFilesDialog().isOpen());
  }
}
