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

import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.NETWORK;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.OVERWRITE_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVED;
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
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
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
public class AddSampleFilesDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private AddSampleFilesDialogPresenter presenter;
  @Mock
  private AddSampleFilesDialog dialog;
  @MockBean
  private SampleService service;
  @MockBean
  private AppConfiguration configuration;
  @Autowired
  private SampleRepository repository;
  @Mock
  private OpenedChangeEvent<Dialog> openedChangeEvent;
  @Captor
  private ArgumentCaptor<ComponentEventListener<OpenedChangeEvent<Dialog>>> openedChangeListenerCaptor;
  @Captor
  private ArgumentCaptor<Command> commandCaptor;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
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
    files.add(new File("sample_R1.fastq"));
    files.add(new File("sample_R2.fastq"));
    files.add(new File("sample.bw"));
    files.add(new File("sample.png"));
    folder = temporaryFolder.getRoot().toPath().resolve("sample");
    when(dialog.getUI()).thenReturn(Optional.of(ui));
    when(dialog.overwrite(any())).thenReturn(new Checkbox("test", false));
    when(configuration.upload(any(Sample.class))).thenReturn(folder);
    when(configuration.uploadLabel(any(Sample.class), anyBoolean())).then(i -> {
      Sample sample = i.getArgument(0);
      boolean linux = i.getArgument(1);
      return (linux ? uploadLabelLinux : uploadLabelWindows) + "/" + sample.getName();
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

  @After
  public void afterTest() {
    Thread thread = presenter.updateFilesThread();
    if (thread != null) {
      thread.interrupt();
    }
  }

  private Path uploadFolder(Sample sample) {
    return configuration.upload(sample);
  }

  @Test
  public void createUploadFolderOnOpen() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertFalse(Files.exists(uploadFolder(sample)));
    when(openedChangeEvent.isOpened()).thenReturn(true);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(Files.isDirectory(uploadFolder(sample)));
  }

  @Test
  public void keepUploadFolderOnClose() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertFalse(Files.exists(uploadFolder(sample)));
    when(openedChangeEvent.isOpened()).thenReturn(true);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(Files.isDirectory(uploadFolder(sample)));
    Files.createFile(uploadFolder(sample).resolve("test.txt"));
    when(openedChangeEvent.isOpened()).thenReturn(false);
    openedChangeListenerCaptor.getAllValues()
        .forEach(listener -> listener.onComponentEvent(openedChangeEvent));
    assertTrue(Files.exists(uploadFolder(sample)));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(sample, false)),
        dialog.message.getText());
    assertEquals(resources.message(NETWORK, configuration.uploadNetwork(false)),
        dialog.network.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void labels_Linux() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(sample, true)),
        dialog.message.getText());
    assertEquals(resources.message(NETWORK, configuration.uploadNetwork(true)),
        dialog.network.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void labels_Mac() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(sample, true)),
        dialog.message.getText());
    assertEquals(resources.message(NETWORK, configuration.uploadNetwork(true)),
        dialog.network.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Empty() {
    Sample sample = repository.findById(1L).get();
    when(configuration.uploadNetwork(anyBoolean())).thenReturn("");
    presenter.setSample(sample, locale);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(sample, true)),
        dialog.message.getText());
    assertFalse(dialog.network.isVisible());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Null() {
    Sample sample = repository.findById(1L).get();
    when(configuration.uploadNetwork(anyBoolean())).thenReturn(null);
    presenter.setSample(sample, locale);
    assertEquals(resources.message(MESSAGE, configuration.uploadLabel(sample, true)),
        dialog.message.getText());
    assertFalse(dialog.network.isVisible());
  }

  @Test
  public void getSample() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertEquals(sample, presenter.getSample());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setSample_NewSample() {
    presenter.setSample(new Sample(), locale);
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample, locale);

    verify(configuration, atLeastOnce()).upload(sample);
    List<File> files = items(dialog.files);
    assertTrue(files.isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void setSample_Null() {
    presenter.setSample(null, locale);
  }

  @Test
  public void updateFiles() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    Files.createDirectories(uploadFolder(sample));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(1).toPath()));
    Files.createDirectory(uploadFolder(sample).resolve("dir"));
    Files.createFile(uploadFolder(sample).resolve("dir").resolve("test.txt"));
    Path hiddenFile = uploadFolder(sample).resolve(".hidden.txt");
    Files.createFile(hiddenFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(hiddenFile, "dos:hidden", true);
    }

    presenter.updateFiles();

    List<File> files = items(dialog.files);
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(1).toPath()).toFile()));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(2).toPath()));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(3).toPath()));

    presenter.updateFiles();

    files = items(dialog.files);
    assertEquals(4, files.size());
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(1).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(2).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(3).toPath()).toFile()));
  }

  @Test
  public void updateFiles_StopThreadOnClose() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    when(openedChangeEvent.isOpened()).thenReturn(true);
    openedChangeListenerCaptor.getValue().onComponentEvent(openedChangeEvent);
    assertTrue(presenter.updateFilesThread().isDaemon());
    Thread.sleep(500);
    assertTrue(presenter.updateFilesThread().isAlive());
    when(openedChangeEvent.isOpened()).thenReturn(false);
    openedChangeListenerCaptor.getValue().onComponentEvent(openedChangeEvent);
    Thread.sleep(500);
    assertFalse(presenter.updateFilesThread().isAlive());
  }

  @Test
  public void updateFiles_StopThreadOnInterrupt() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    verify(dialog, atLeastOnce()).addOpenedChangeListener(openedChangeListenerCaptor.capture());
    when(openedChangeEvent.isOpened()).thenReturn(true);
    openedChangeListenerCaptor.getValue().onComponentEvent(openedChangeEvent);
    assertTrue(presenter.updateFilesThread().isDaemon());
    Thread.sleep(500);
    assertTrue(presenter.updateFilesThread().isAlive());
    presenter.updateFilesThread().interrupt();
    Thread.sleep(500);
    assertFalse(presenter.updateFilesThread().isAlive());
  }

  @Test
  public void exists_False() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertFalse(presenter.exists(files.get(2)));
  }

  @Test
  public void exists_True() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    assertTrue(presenter.exists(files.get(1)));
  }

  @Test
  public void exists_NoSample() {
    assertFalse(presenter.exists(files.get(1)));
  }

  @Test
  public void save_OverwriteNotAllowed() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    Files.createDirectories(uploadFolder(sample));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(1).toPath()));

    presenter.save(locale);

    assertTrue(dialog.error.isVisible());
    assertEquals(resources.message(OVERWRITE_ERROR), dialog.error.getText());
    verify(service, never()).saveFiles(any(), any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).close();
  }

  @Test
  public void save_OverwriteAllowed() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    when(dialog.overwrite(any())).thenReturn(new Checkbox("test", true));
    Files.createDirectories(uploadFolder(sample));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(1).toPath()));

    presenter.save(locale);

    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(0).toPath())));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(1).toPath())));
    assertFalse(dialog.error.isVisible());
    verify(dialog).showNotification(resources.message(SAVED, 2, sample.getName()));
    verify(dialog).fireSavedEvent();
    verify(dialog).close();
  }

  @Test
  public void save() throws Throwable {
    when(service.files(any())).thenReturn(new ArrayList<>());
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);
    Files.createDirectories(uploadFolder(sample));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(0).toPath()));
    Files.createFile(uploadFolder(sample).resolve(this.files.get(1).toPath()));

    presenter.save(locale);

    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(0).toPath())));
    assertTrue(files.contains(uploadFolder(sample).resolve(this.files.get(1).toPath())));
    verify(dialog).showNotification(resources.message(SAVED, 2, sample.getName()));
    verify(dialog).fireSavedEvent();
    verify(dialog).close();
  }

  @Test
  public void save_NoFiles() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample, locale);

    presenter.save(locale);

    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(0, files.size());
    assertFalse(Files.exists(uploadFolder(sample)));
    verify(dialog).showNotification(resources.message(SAVED, 0, sample.getName()));
    verify(dialog).fireSavedEvent();
    verify(dialog).close();
  }
}
