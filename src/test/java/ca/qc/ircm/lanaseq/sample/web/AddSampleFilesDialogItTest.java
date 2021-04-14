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

import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
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

@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddSampleFilesDialogItTest extends AbstractTestBenchTestCase {
  @TempDir
  Path temporaryFolder;
  @Autowired
  private SampleRepository repository;
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

  private void copyFiles(Sample sample)
      throws IOException, URISyntaxException, NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Path folder = configuration.upload(sample);
    Files.createDirectories(folder);
    file1 = folder.resolve("R1.fastq");
    file2 = folder.resolve("R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file1);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file2);
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().controlClick(0);
    SampleFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.add().click();
    AddSampleFilesDialogElement dialog = filesDialog.addFilesDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.message()).isPresent());
    assertTrue(optional(() -> dialog.files()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
  }

  @Test
  public void refresh_Files() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().controlClick(1);
    SampleFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.add().click();
    AddSampleFilesDialogElement dialog = filesDialog.addFilesDialog();
    Sample sample = repository.findById(10L).get();
    assertEquals(0, dialog.files().getRowCount());
    copyFiles(sample);
    Thread.sleep(2500);
    assertEquals(2, dialog.files().getRowCount());
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        configuration.upload(sample).resolve("other.fastq"));
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        configuration.getUpload().resolve("prefix_" + sample.getName() + "_R1"));
    Thread.sleep(2500);
    assertEquals(4, dialog.files().getRowCount());
  }

  @Test
  public void save() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(SamplesView.ID);
    view.samples().controlClick(1);
    SampleFilesDialogElement filesDialog = view.filesDialog();
    filesDialog.add().click();
    final AddSampleFilesDialogElement dialog = filesDialog.addFilesDialog();
    Sample sample = repository.findById(10L).get();
    copyFiles(sample);
    String filenameInRoot = "prefix_" + sample.getName() + "_R1";
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        configuration.getUpload().resolve(filenameInRoot));

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(AddSampleFilesDialog.class);
    assertEquals(resources.message(SAVED, 3, sample.getName()), notification.getText());
    Path folder = configuration.folder(sample);
    Path upload = configuration.upload(sample);
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
