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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetFilesDialogItTest extends AbstractTestBenchTestCase {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;

  @Before
  public void beforeTest() throws Throwable {
    setHome(temporaryFolder.newFolder("home").toPath());
  }

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(0);
    DatasetFilesDialogElement dialog = view.filesDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.files()).isPresent());
    assertTrue(optional(() -> dialog.samples()).isPresent());
    assertTrue(optional(() -> dialog.add()).isPresent());
    assertTrue(optional(() -> dialog.addFilesDialog()).isPresent());
    assertTrue(optional(() -> dialog.sampleFilesDialog()).isPresent());
  }

  @Test
  public void rename() throws Throwable {
    Dataset dataset = repository.findById(2L).get();
    Path folder = configuration.folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file);
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.files().getRow(0).doubleClick();
    dialog.filenameEdit().setValue(dataset.getName() + "_R1.fastq");
    dialog.filenameEdit().sendKeys(Keys.ENTER);
    Thread.sleep(1000);

    assertTrue(Files.exists(file.resolveSibling(dataset.getName() + "_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(file.resolveSibling(dataset.getName() + "_R1.fastq")));
    assertFalse(Files.exists(file));
  }

  @Test
  public void delete() throws Throwable {
    Dataset dataset = repository.findById(2L).get();
    Path folder = configuration.folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.delete(0).click();
    Thread.sleep(1000);

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
  public void viewFiles_Sample() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();
    dialog.samples().getCell(0, 0).doubleClick();

    assertTrue(dialog.sampleFilesDialog().isOpen());
  }

  @Test
  public void add() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.add().click();

    assertTrue(dialog.addFilesDialog().isOpen());
  }
}
