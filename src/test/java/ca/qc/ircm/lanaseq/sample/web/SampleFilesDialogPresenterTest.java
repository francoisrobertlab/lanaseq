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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.MESSAGE_TITLE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import ca.qc.ircm.lanaseq.web.EditableFile;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
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
public class SampleFilesDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private SampleFilesDialogPresenter presenter;
  @Mock
  private SampleFilesDialog dialog;
  @MockBean
  private SampleService service;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private AuthorizationService authorizationService;
  @MockBean
  private AppConfiguration configuration;
  @Captor
  private ArgumentCaptor<Sample> sampleCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<AddSampleFilesDialog>>> savedListenerCaptor;
  @Autowired
  private SampleRepository repository;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<File> files = new ArrayList<>();
  private Path folder;
  private String folderLabelLinux = "lanaseq/upload";
  private String folderLabelWindows = "lanaseq\\upload";
  private String folderNetworkLinux = "smb://lanaseq01/lanaseq";
  private String folderNetworkWindows = "\\\\lanaseq01\\lanaseq";
  private Random random = new Random();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    ui.setLocale(locale);
    dialog.header = new H3();
    dialog.message = new Div();
    dialog.files = new Grid<>();
    dialog.delete = dialog.files.addColumn(file -> file);
    dialog.filenameEdit = new TextField();
    dialog.addFilesDialog = mock(AddSampleFilesDialog.class);
    files.add(new File("sample_R1.fastq"));
    files.add(new File("sample_R2.fastq"));
    files.add(new File("sample.bw"));
    files.add(new File("sample.png"));
    when(dialog.getUI()).thenReturn(Optional.of(ui));
    when(service.files(any()))
        .thenReturn(files.stream().map(file -> file.toPath()).collect(Collectors.toList()));
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    when(configuration.folder(any(Sample.class))).thenReturn(folder);
    when(configuration.folderLabel(any(Sample.class), anyBoolean())).then(i -> {
      Sample sample = i.getArgument(0);
      boolean linux = i.getArgument(1);
      return (linux ? folderLabelLinux : folderLabelWindows) + "/" + sample.getName();
    });
    when(configuration.folderNetwork(anyBoolean())).then(i -> {
      boolean linux = i.getArgument(0);
      return (linux ? folderNetworkLinux : folderNetworkWindows);
    });
    presenter.init(dialog);
    presenter.localeChange(locale);
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(sample, false)),
        dialog.message.getText());
    assertEquals(resources.message(MESSAGE_TITLE, configuration.folderNetwork(false)),
        dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void labels_Linux() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(sample, true)),
        dialog.message.getText());
    assertEquals(resources.message(MESSAGE_TITLE, configuration.folderNetwork(true)),
        dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void labels_Mac() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(sample, true)),
        dialog.message.getText());
    assertEquals(resources.message(MESSAGE_TITLE, configuration.folderNetwork(true)),
        dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Empty() {
    Sample sample = repository.findById(1L).get();
    when(configuration.folderNetwork(anyBoolean())).thenReturn("");
    presenter.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(sample, true)),
        dialog.message.getText());
    assertEquals("", dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Null() {
    Sample sample = repository.findById(1L).get();
    when(configuration.folderNetwork(anyBoolean())).thenReturn(null);
    presenter.setSample(sample);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(sample, true)),
        dialog.message.getText());
    assertEquals("", dialog.message.getTitle().orElse(""));
  }

  @Test
  public void getSample() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    assertEquals(sample, presenter.getSample());
  }

  @Test(expected = NullPointerException.class)
  public void setSample_NewSample() {
    Sample sample = new Sample();

    presenter.setSample(sample);
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertTrue(dialog.delete.isVisible());
    assertFalse(dialog.filenameEdit.isReadOnly());
    verify(dialog.addFilesDialog).setSample(sample);
  }

  @Test
  public void setSample_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test
  public void setSample_NotEditable() {
    Sample sample = repository.findById(8L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test(expected = NullPointerException.class)
  public void setSample_Null() {
    presenter.setSample(null);
  }

  @Test
  public void filenameEdit_Empty() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void filenameEdit_Invalid() throws Throwable {
    dialog.filenameEdit.setValue("abc?.txt");

    BinderValidationStatus<EditableFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(resources.message(FILENAME_REGEX_ERROR)), error.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void filenameEdit_Exists() throws Throwable {
    File path = temporaryFolder.newFile("source.txt");
    temporaryFolder.newFile("abc.txt");
    EditableFile file = new EditableFile(path);
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(file);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<EditableFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(ALREADY_EXISTS)), error.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void filenameEdit_ExistsSameFile() throws Throwable {
    temporaryFolder.newFile("abc.txt");
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(null);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<EditableFile> status = presenter.validateSampleFile();
    assertTrue(status.isOk());
  }

  @Test
  public void filenameEdit_ItemNull() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void add() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);

    presenter.add();

    verify(dialog.addFilesDialog).open();
  }

  @Test
  public void add_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);

    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NotEditable() {
    Sample sample = repository.findById(8L).get();
    presenter.setSample(sample);

    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NoSample() {
    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_Saved() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    verify(dialog.addFilesDialog).addSavedListener(savedListenerCaptor.capture());

    savedListenerCaptor.getValue().onComponentEvent(new SavedEvent<>(dialog.addFilesDialog, false));

    verify(service, atLeast(2)).files(sample);
  }

  @Test
  public void rename() throws Throwable {
    File path = temporaryFolder.newFile("source.txt");
    EditableFile file = new EditableFile(path);
    file.setFilename("target.txt");
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path.toPath(), bytes);

    presenter.rename(file);

    assertTrue(Files.exists(path.toPath().resolveSibling("target.txt")));
    assertArrayEquals(bytes, Files.readAllBytes(path.toPath().resolveSibling("target.txt")));
    assertFalse(Files.exists(path.toPath()));
  }

  @Test
  public void deleteFile() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    File path = temporaryFolder.newFile("source.txt");
    EditableFile file = new EditableFile(path);
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path.toPath(), bytes);

    presenter.deleteFile(file);

    verify(service).deleteFile(sample, file.getFile().toPath());
  }
}
