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

import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.NETWORK;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.OVERWRITE_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.GeneratedVaadinDialog.OpenedChangeEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.server.Command;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class AddDatasetFilesDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private AddDatasetFilesDialogPresenter presenter;
  @Mock
  private AddDatasetFilesDialog dialog;
  @MockBean
  private DatasetService service;
  @MockBean
  private AppConfiguration configuration;
  @Autowired
  private DatasetRepository repository;
  @Mock
  private OpenedChangeEvent<Dialog> openedChangeEvent;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<OpenedChangeEvent<Dialog>>> openedChangeListenerCaptor;
  @Captor
  private ArgumentCaptor<Command> commandCaptor;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AddDatasetFilesDialog.class, locale);
  private Path folder;
  private List<File> files = new ArrayList<>();
  private String uploadLabelLinux = "lanaseq/upload";
  private String uploadLabelWindows = "lanaseq\\upload";
  private String uploadNetworkLinux = "smb://lanaseq01/lanaseq";
  private String uploadNetworkWindows = "\\\\lanaseq01\\lanaseq";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    ui.setLocale(locale);
    dialog.header = new H3();
    dialog.message = new Div();
    dialog.network = new Div();
    dialog.files = new Grid<>();
    dialog.error = new Div();
    dialog.save = new Button();
    files.add(new File("dataset_R1.fastq"));
    files.add(new File("dataset_R2.fastq"));
    files.add(new File("dataset.bw"));
    files.add(new File("dataset.png"));
    folder = temporaryFolder.getRoot().toPath().resolve("dataset");
    when(dialog.getUI()).thenReturn(Optional.of(ui));
    when(dialog.overwrite(any())).thenReturn(new Checkbox("test", false));
    when(configuration.upload(any(Dataset.class))).thenReturn(folder);
    when(configuration.uploadLabel(any(Dataset.class), anyBoolean())).then(i -> {
      Dataset dataset = i.getArgument(0);
      boolean linux = i.getArgument(1);
      return (linux ? uploadLabelLinux : uploadLabelWindows) + "/" + dataset.getName();
    });
    when(configuration.uploadNetwork(anyBoolean())).then(i -> {
      boolean linux = i.getArgument(0);
      return (linux ? uploadNetworkLinux : uploadNetworkWindows);
    });
    when(service.files(any())).thenReturn(
        files.subList(0, 2).stream().map(file -> file.toPath()).collect(Collectors.toList()));
    doAnswer(i -> {
      Collection<Path> files = i.getArgument(1);
      for (Path file : files) {
        Files.delete(file);
      }
      return null;
    }).when(service).saveFiles(any(), any());
    presenter.init(dialog);
    presenter.localeChange(locale);
  }

  /**
   * After test.
   */
  @After
  public void afterTest() {
    Thread thread = presenter.updateFilesThread();
    if (thread != null) {
      thread.interrupt();
    }
  }

  private Path uploadFolder(Dataset dataset) {
    return configuration.upload(dataset);
  }

  @Test
  public void createUploadFolderOnOpen() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertFalse(Files.exists(uploadFolder(dataset)));
    when(openedChangeEvent.isOpened()).thenReturn(true);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(Files.isDirectory(uploadFolder(dataset)));
  }

  @Test
  public void keepUploadFolderOnClose() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertFalse(Files.exists(uploadFolder(dataset)));
    when(openedChangeEvent.isOpened()).thenReturn(true);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(Files.isDirectory(uploadFolder(dataset)));
    Files.createFile(uploadFolder(dataset).resolve("test.txt"));
    when(openedChangeEvent.isOpened()).thenReturn(false);
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(Files.exists(uploadFolder(dataset)));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(dataset, false)),
        dialog.message.getText());
    assertEquals(resources.message(NETWORK, configuration.uploadNetwork(false)),
        dialog.network.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void labels_Linux() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(dataset, true)),
        dialog.message.getText());
    assertEquals(resources.message(NETWORK, configuration.uploadNetwork(true)),
        dialog.network.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void labels_Mac() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(dataset, true)),
        dialog.message.getText());
    assertEquals(resources.message(NETWORK, configuration.uploadNetwork(true)),
        dialog.network.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Empty() {
    when(configuration.uploadNetwork(anyBoolean())).thenReturn("");
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(dataset, true)),
        dialog.message.getText());
    assertFalse(dialog.network.isVisible());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Null() {
    when(configuration.uploadNetwork(anyBoolean())).thenReturn(null);
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(dataset, true)),
        dialog.message.getText());
    assertFalse(dialog.network.isVisible());
  }

  @Test
  public void getDataset() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(dataset, presenter.getDataset());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setDataset_NewDataset() {
    presenter.setDataset(new Dataset());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    verify(configuration, atLeastOnce()).upload(dataset);
    List<File> files = items(dialog.files);
    assertTrue(files.isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void setDataset_Null() {
    presenter.setDataset(null);
  }

  @Test
  public void updateFiles() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    Files.createDirectories(uploadFolder(dataset));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(1).toPath()));
    Files.createDirectory(uploadFolder(dataset).resolve("dir"));
    Files.createFile(uploadFolder(dataset).resolve("dir").resolve("test.txt"));
    Path hiddenFile = uploadFolder(dataset).resolve(".hidden.txt");
    Files.createFile(hiddenFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(hiddenFile, "dos:hidden", true);
    }

    presenter.updateFiles();

    List<File> files = items(dialog.files);
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(1).toPath()).toFile()));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(2).toPath()));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(3).toPath()));

    presenter.updateFiles();

    files = items(dialog.files);
    assertEquals(4, files.size());
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(1).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(2).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(3).toPath()).toFile()));
  }

  @Test
  public void updateFiles_StopThreadOnClose() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    when(openedChangeEvent.isOpened()).thenReturn(true);
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(presenter.updateFilesThread().isDaemon());
    Thread.sleep(500);
    assertTrue(presenter.updateFilesThread().isAlive());
    when(openedChangeEvent.isOpened()).thenReturn(false);
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    Thread.sleep(500);
    assertFalse(presenter.updateFilesThread().isAlive());
  }

  @Test
  public void updateFiles_StopThreadOnInterrupt() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    when(openedChangeEvent.isOpened()).thenReturn(true);
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(presenter.updateFilesThread().isDaemon());
    Thread.sleep(500);
    assertTrue(presenter.updateFilesThread().isAlive());
    presenter.updateFilesThread().interrupt();
    Thread.sleep(500);
    assertFalse(presenter.updateFilesThread().isAlive());
  }

  @Test
  public void exists_False() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertFalse(presenter.exists(files.get(2)));
  }

  @Test
  public void exists_True() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertTrue(presenter.exists(files.get(1)));
  }

  @Test
  public void exists_NoDataset() {
    assertFalse(presenter.exists(files.get(1)));
  }

  @Test
  public void save_OverwriteNotAllowed() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    Files.createDirectories(uploadFolder(dataset));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(1).toPath()));

    presenter.save();

    assertTrue(dialog.error.isVisible());
    assertEquals(resources.message(OVERWRITE_ERROR), dialog.error.getText());
    verify(service, never()).saveFiles(any(), any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).close();
  }

  @Test
  public void save_OverwriteAllowed() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    when(dialog.overwrite(any())).thenReturn(new Checkbox("test", true));
    Files.createDirectories(uploadFolder(dataset));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(1).toPath()));

    presenter.save();

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(0).toPath())));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(1).toPath())));
    assertFalse(dialog.error.isVisible());
    verify(dialog).showNotification(resources.message(SAVED, 2, dataset.getName()));
    verify(dialog).fireSavedEvent();
    verify(dialog).close();
  }

  @Test
  public void save() throws Throwable {
    when(service.files(any())).thenReturn(new ArrayList<>());
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    Files.createDirectories(uploadFolder(dataset));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(dataset).resolve(this.files.get(1).toPath()));

    presenter.save();

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(0).toPath())));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(1).toPath())));
    verify(dialog).showNotification(resources.message(SAVED, 2, dataset.getName()));
    verify(dialog).fireSavedEvent();
    verify(dialog).close();
  }

  @Test
  public void save_NoFiles() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);

    presenter.save();

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(0, files.size());
    assertFalse(Files.exists(uploadFolder(dataset)));
    verify(dialog).showNotification(resources.message(SAVED, 0, dataset.getName()));
    verify(dialog).fireSavedEvent();
    verify(dialog).close();
  }
}
