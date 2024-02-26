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

import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.OVERWRITE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.OVERWRITE_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SIZE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.github.mvysny.kaributesting.v10.GridKt;
import com.github.mvysny.kaributesting.v10.LocatorJ;
import com.github.mvysny.kaributesting.v10.NotificationsKt;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.SortDirection;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link AddSampleFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddSampleFilesDialogTest extends AbstractKaribuTestCase {
  @TempDir
  Path temporaryFolder;
  private AddSampleFilesDialog dialog;
  @MockBean
  private SampleService service;
  @MockBean
  private AppConfiguration configuration;
  @Mock
  private ComponentEventListener<SavedEvent<AddSampleFilesDialog>> savedListener;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Autowired
  private SampleRepository repository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private Path folder;
  private List<File> files = new ArrayList<>();
  private Random random = new Random();
  private String uploadLabelLinux = "lanaseq/upload";
  private String uploadLabelWindows = "lanaseq\\upload";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Throwable {
    files.add(temporaryFolder.resolve("sample_R1.fastq").toFile());
    files.add(temporaryFolder.resolve("sample_R2.fastq").toFile());
    files.add(temporaryFolder.resolve("sample.bw").toFile());
    files.add(temporaryFolder.resolve("sample.png").toFile());
    for (File file : files) {
      writeFile(file.toPath(), random.nextInt(10) * 1024 ^ 2);
    }
    folder = temporaryFolder.resolve("sample");
    when(configuration.getHome()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getHome().folder(any(Sample.class)))
        .thenReturn(temporaryFolder.resolve("home"));
    when(configuration.getUpload()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getUpload().folder(any(Sample.class))).thenReturn(folder);
    when(configuration.getUpload().label(any(Sample.class), anyBoolean())).then(i -> {
      Sample sample = i.getArgument(0);
      boolean linux = i.getArgument(1);
      return (linux ? uploadLabelLinux : uploadLabelWindows) + "/" + sample.getName();
    });
    when(service.uploadFiles(any())).thenReturn(
        files.stream().map(file -> folder.resolve(file.toPath())).collect(Collectors.toList()));
    when(service.files(any())).thenReturn(
        files.subList(0, 2).stream().map(file -> file.toPath()).collect(Collectors.toList()));
    ui.setLocale(locale);
    SamplesView view = ui.navigate(SamplesView.class).get();
    view.samples.setItems(repository.findAll());
    GridKt._clickItem(view.samples, 1, 1, true, false, false, false);
    SampleFilesDialog filesDialog = LocatorJ._find(SampleFilesDialog.class).get(0);
    filesDialog.addLargeFiles.click();
    dialog = LocatorJ._find(AddSampleFilesDialog.class).get(0);
  }

  private void writeFile(Path file, long size) throws IOException {
    try (OutputStream output =
        new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.CREATE))) {
      long written = 0;
      byte[] buff = new byte[1024];
      while (written < size) {
        random.nextBytes(buff);
        output.write(buff);
        written += buff.length;
      }
    }
  }

  private File filename(String filename) {
    return new File(filename);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(MESSAGE), dialog.message.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
  }

  @Test
  public void labels() {
    Sample sample = dialog.getSample();
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
    assertEquals(resources.message(MESSAGE, configuration.getUpload().label(sample, true)),
        dialog.message.getText());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(resources.message(FILENAME), headerRow.getCell(dialog.filename).getText());
    assertEquals(resources.message(SIZE), headerRow.getCell(dialog.size).getText());
    assertEquals(resources.message(OVERWRITE), headerRow.getCell(dialog.overwrite).getText());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    Sample sample = dialog.getSample();
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
    assertEquals(resources.message(MESSAGE, configuration.getUpload().label(sample, true)),
        dialog.message.getText());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(resources.message(FILENAME), headerRow.getCell(dialog.filename).getText());
    assertEquals(resources.message(SIZE), headerRow.getCell(dialog.size).getText());
    assertEquals(resources.message(OVERWRITE), headerRow.getCell(dialog.overwrite).getText());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
  }

  @Test
  public void files() {
    assertEquals(3, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(SIZE));
    assertNotNull(dialog.files.getColumnByKey(OVERWRITE));
  }

  @Test
  public void files_ColumnsValueProvider() throws Throwable {
    when(service.uploadFiles(any())).thenReturn(files.stream().map(File::toPath).toList());
    when(service.files(any())).thenReturn(Collections.nCopies(1, files.get(0).toPath()));
    dialog.updateFiles();
    for (int i = 0; i < files.size(); i++) {
      File file = files.get(i);
      Span span = (Span) GridKt._getCellComponent(dialog.files, i, dialog.filename.getKey());
      assertEquals(file.getName(), span.getText());
      assertEquals(i == 0, span.hasClassName(ERROR_TEXT));
      assertEquals(resources.message(AddSampleFilesDialog.SIZE_VALUE, file.length() / 1048576),
          GridKt.getPresentationValue(dialog.size, file));
      assertTrue(
          GridKt._getCellComponent(dialog.files, i, dialog.overwrite.getKey()) instanceof Checkbox);
    }
  }

  @Test
  public void files_FilenameColumnComparator() {
    Comparator<File> comparator = dialog.filename.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(filename("éê"), filename("ee")));
    assertTrue(comparator.compare(filename("a"), filename("e")) < 0);
    assertTrue(comparator.compare(filename("a"), filename("é")) < 0);
    assertTrue(comparator.compare(filename("e"), filename("a")) > 0);
    assertTrue(comparator.compare(filename("é"), filename("a")) > 0);
  }

  @Test
  public void overwriteAll_True() {
    for (File file : files) {
      dialog.overwrite(file);
    }
    dialog.overwriteAll.setValue(true);
    ComponentValueChangeEvent<Checkbox, Boolean> event =
        new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, false, true);
    fireEvent(dialog.overwriteAll, event);
    for (File file : files) {
      Checkbox checkbox = dialog.overwrite(file);
      assertTrue(checkbox.getValue());
    }
  }

  @Test
  public void overwriteAll_False() {
    for (File file : files) {
      Checkbox checkbox = dialog.overwrite(file);
      checkbox.setValue(true);
    }
    dialog.overwriteAll.setValue(true);
    ComponentValueChangeEvent<Checkbox, Boolean> event =
        new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, false, true);
    fireEvent(dialog.overwriteAll, event);
    dialog.overwriteAll.setValue(false);
    event = new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, true, true);
    fireEvent(dialog.overwriteAll, event);
    for (File file : files) {
      Checkbox checkbox = dialog.overwrite(file);
      assertFalse(checkbox.getValue());
    }
  }

  @Test
  public void overwriteAll_UncheckIfOneOverwiteUnchecked() {
    for (File file : files) {
      dialog.overwrite(file);
    }
    dialog.overwriteAll.setValue(true);
    ComponentValueChangeEvent<Checkbox, Boolean> event =
        new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, false, true);
    fireEvent(dialog.overwriteAll, event);
    dialog.overwrite(files.get(0)).setValue(false);
    assertFalse(dialog.overwriteAll.getValue());
  }

  @Test
  public void savedListener() {
    dialog.addSavedListener(savedListener);
    dialog.fireSavedEvent();
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void savedListener_Remove() {
    dialog.addSavedListener(savedListener).remove();
    dialog.fireSavedEvent();
    verify(savedListener, never()).onComponentEvent(any());
  }

  private Path uploadFolder(Sample sample) {
    return configuration.getUpload().folder(sample);
  }

  @Test
  public void createUploadFolder() {
    assertTrue(Files.exists(uploadFolder(dialog.getSample())));
  }

  @Test
  public void keepUploadFolderOnClose() throws Throwable {
    Sample sample = dialog.getSample();
    assertTrue(Files.exists(uploadFolder(sample)));
    Files.createFile(uploadFolder(sample).resolve("test.txt"));
    dialog.close();
    assertTrue(Files.exists(uploadFolder(sample)));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void message_Windows() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.getUpload().label(sample, false)),
        dialog.message.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void message_Linux() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.getUpload().label(sample, true)),
        dialog.message.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void message_Mac() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.getUpload().label(sample, true)),
        dialog.message.getText());
  }

  @Test
  public void getSample() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    assertEquals(sample, dialog.getSample());
  }

  @Test
  public void setSample_NewSample() {
    assertThrows(IllegalArgumentException.class, () -> {
      dialog.setSample(new Sample());
    });
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(2L).get();

    dialog.setSample(sample);

    verify(service, atLeastOnce()).uploadFiles(sample);
    List<File> files = items(dialog.files);
    assertEquals(4, files.size());
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
  }

  @Test
  public void setSample_Null() {
    assertThrows(NullPointerException.class, () -> {
      dialog.setSample(null);
    });
  }

  @Test
  public void updateFiles() {
    when(service.uploadFiles(any())).thenReturn(
        files.subList(0, 2).stream().map(file -> folder.resolve(file.toPath()))
            .collect(Collectors.toList()),
        files.stream().map(file -> folder.resolve(file.toPath())).collect(Collectors.toList()));
    Sample sample = dialog.getSample();

    dialog.updateFiles();

    verify(service, times(2)).uploadFiles(sample);
    List<File> files = items(dialog.files);
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(1).toPath()).toFile()));

    dialog.updateFiles();

    verify(service, times(3)).uploadFiles(sample);
    files = items(dialog.files);
    assertEquals(4, files.size());
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(1).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(2).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(3).toPath()).toFile()));
  }

  @Test
  public void updateFiles_StopThreadOnClose() throws Throwable {
    assertTrue(dialog.updateFilesThread().isDaemon());
    Thread.sleep(500);
    assertTrue(dialog.updateFilesThread().isAlive());
    dialog.close();
    Thread.sleep(500);
    assertFalse(dialog.updateFilesThread().isAlive());
  }

  @Test
  public void updateFiles_StopThreadOnInterrupt() throws Throwable {
    assertTrue(dialog.updateFilesThread().isDaemon());
    Thread.sleep(500);
    assertTrue(dialog.updateFilesThread().isAlive());
    dialog.updateFilesThread().interrupt();
    Thread.sleep(500);
    assertFalse(dialog.updateFilesThread().isAlive());
  }

  @Test
  public void exists_False() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    assertFalse(dialog.exists(files.get(2)));
  }

  @Test
  public void exists_True() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    assertTrue(dialog.exists(files.get(1)));
  }

  @Test
  public void save_OverwriteNotAllowed() {
    dialog.addSavedListener(savedListener);

    dialog.save();

    assertTrue(dialog.error.isVisible());
    assertEquals(resources.message(OVERWRITE_ERROR), dialog.error.getText());
    verify(service, never()).saveFiles(any(), any());
    NotificationsKt.expectNoNotifications();
    verify(savedListener, never()).onComponentEvent(any());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void save_OverwriteAllowed() {
    dialog.addSavedListener(savedListener);
    dialog.files.getListDataView().getItems().forEach(f -> dialog.overwrite(f));
    Sample sample = dialog.getSample();
    dialog.overwriteAll.setValue(true);

    dialog.save();

    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(4, files.size());
    for (Path file : files) {
      assertTrue(files.contains(uploadFolder(sample).resolve(file)));
    }
    assertFalse(dialog.error.isVisible());
    NotificationsKt.expectNotifications(resources.message(SAVED, 4, sample.getName()));
    verify(savedListener).onComponentEvent(any());
    assertFalse(dialog.isOpened());
  }

  @Test
  public void save() throws Throwable {
    when(service.files(any())).thenReturn(new ArrayList<>());
    when(service.uploadFiles(any())).thenReturn(files.subList(0, 2).stream()
        .map(file -> folder.resolve(file.toPath())).collect(Collectors.toList()));
    dialog.addSavedListener(savedListener);
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);

    dialog.save();

    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(0).toPath())));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(1).toPath())));
    NotificationsKt.expectNotifications(resources.message(SAVED, 2, sample.getName()));
    verify(savedListener).onComponentEvent(any());
    assertFalse(dialog.isOpened());
  }

  @Test
  public void save_NoFiles() {
    when(service.uploadFiles(any())).thenReturn(new ArrayList<>());
    dialog.addSavedListener(savedListener);
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);

    dialog.save();

    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(0, files.size());
    assertTrue(Files.exists(uploadFolder(sample)));
    NotificationsKt.expectNotifications(resources.message(SAVED, 0, sample.getName()));
    verify(savedListener).onComponentEvent(any());
    assertFalse(dialog.isOpened());
  }
}
