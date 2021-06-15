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

import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration tests for {@link AddDatasetFilesDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddDatasetFilesDialogItTest extends AbstractTestBenchTestCase {
  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;
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
      throws IOException, URISyntaxException, NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Path folder = configuration.upload(dataset);
    Files.createDirectories(folder);
    file1 = folder.resolve("R1.fastq");
    file2 = folder.resolve("R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file1);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file2);
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(0);
    DatasetFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    AddDatasetFilesDialogElement dialog = filesDialog.addFilesDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.message()).isPresent());
    assertTrue(optional(() -> dialog.files()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
  }

  @Test
  public void refresh_Files() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(3);
    DatasetFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    AddDatasetFilesDialogElement dialog = filesDialog.addFilesDialog();
    Dataset dataset = repository.findById(2L).get();
    assertEquals(0, dialog.files().getRowCount());
    copyFiles(dataset);
    Thread.sleep(2500);
    assertEquals(2, dialog.files().getRowCount());
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        configuration.upload(dataset).resolve("other.fastq"));
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        configuration.getUpload().resolve("prefix_" + dataset.getName() + "_R1"));
    Thread.sleep(2500);
    assertEquals(4, dialog.files().getRowCount());
  }

  @Test
  public void save() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(DatasetsView.ID);
    view.datasets().controlClick(3);
    DatasetFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.addLargeFiles().click();
    final AddDatasetFilesDialogElement dialog = filesDialog.addFilesDialog();
    Dataset dataset = repository.findById(2L).get();
    copyFiles(dataset);
    String filenameInRoot = "prefix_" + dataset.getName() + "_R1";
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        configuration.getUpload().resolve(filenameInRoot));

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(AddDatasetFilesDialog.class);
    assertEquals(resources.message(SAVED, 3, dataset.getName()), notification.getText());
    Path folder = configuration.folder(dataset);
    Path upload = configuration.upload(dataset);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertTrue(Files.exists(folder.resolve(file2.getFileName())));
    assertFalse(Files.exists(upload.getParent().resolve(filenameInRoot)));
    assertFalse(Files.exists(upload.resolve(file1.getFileName())));
    assertFalse(Files.exists(upload.resolve(file2.getFileName())));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve(file2.getFileName())));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve(filenameInRoot)));
  }
}
