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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE_TITLE;
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
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import ca.qc.ircm.lanaseq.web.EditableFile;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.GeneratedVaadinDialog.OpenedChangeEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.DataProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
public class DatasetFilesDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private DatasetFilesDialogPresenter presenter;
  @Mock
  private DatasetFilesDialog dialog;
  @MockBean
  private DatasetService service;
  @MockBean
  private SampleService sampleService;
  @MockBean
  private AuthorizationService authorizationService;
  @MockBean
  private AppConfiguration configuration;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<AddDatasetFilesDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<OpenedChangeEvent<Dialog>>> openedSampleFilesListenerCaptor;
  @Autowired
  private DatasetRepository repository;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Mock
  private Sample sample;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
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
    dialog.samples = new Grid<>();
    dialog.delete = dialog.files.addColumn(file -> file);
    dialog.filenameEdit = new TextField();
    dialog.addFilesDialog = mock(AddDatasetFilesDialog.class);
    dialog.sampleFilesDialog = mock(SampleFilesDialog.class);
    files.add(new File("dataset_R1.fastq"));
    files.add(new File("dataset_R2.fastq"));
    files.add(new File("dataset.bw"));
    files.add(new File("dataset.png"));
    when(dialog.getUI()).thenReturn(Optional.of(ui));
    when(service.files(any()))
        .thenReturn(files.stream().map(file -> file.toPath()).collect(Collectors.toList()));
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    when(configuration.folder(any(Dataset.class))).thenReturn(folder);
    when(configuration.folderLabel(any(Dataset.class), anyBoolean())).then(i -> {
      Dataset dataset = i.getArgument(0);
      boolean linux = i.getArgument(1);
      return (linux ? folderLabelLinux : folderLabelWindows) + "/" + dataset.getName();
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
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(dataset, false)),
        dialog.message.getText());
    assertEquals(resources.message(MESSAGE_TITLE, configuration.folderNetwork(false)),
        dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void labels_Linux() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(dataset, true)),
        dialog.message.getText());
    assertEquals(resources.message(MESSAGE_TITLE, configuration.folderNetwork(true)),
        dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void labels_Mac() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(dataset, true)),
        dialog.message.getText());
    assertEquals(resources.message(MESSAGE_TITLE, configuration.folderNetwork(true)),
        dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Empty() {
    Dataset dataset = repository.findById(1L).get();
    when(configuration.folderNetwork(anyBoolean())).thenReturn("");
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(dataset, true)),
        dialog.message.getText());
    assertEquals("", dialog.message.getTitle().orElse(""));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void network_Null() {
    Dataset dataset = repository.findById(1L).get();
    when(configuration.folderNetwork(anyBoolean())).thenReturn(null);
    presenter.setDataset(dataset);
    assertEquals(resources.message(MESSAGE, configuration.folderLabel(dataset, true)),
        dialog.message.getText());
    assertEquals("", dialog.message.getTitle().orElse(""));
  }

  @Test
  public void getDataset() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(dataset, presenter.getDataset());
  }

  @Test(expected = NullPointerException.class)
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();

    presenter.setDataset(dataset);
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    verify(service).files(dataset);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertTrue(dialog.delete.isVisible());
    assertFalse(dialog.filenameEdit.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(dataset.getSamples().size(), samples.size());
    for (Sample sample : dataset.getSamples()) {
      assertTrue(samples.contains(sample));
    }
    verify(dialog.addFilesDialog).setDataset(dataset);
  }

  @Test
  public void setDataset_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    verify(service).files(dataset);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(dataset.getSamples().size(), samples.size());
    for (Sample sample : dataset.getSamples()) {
      assertTrue(samples.contains(sample));
    }
  }

  @Test
  public void setDataset_NotEditable() {
    Dataset dataset = repository.findById(5L).get();

    presenter.setDataset(dataset);

    verify(service).files(dataset);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(dataset.getSamples().size(), samples.size());
    for (Sample sample : dataset.getSamples()) {
      assertTrue(samples.contains(sample));
    }
  }

  @Test(expected = NullPointerException.class)
  public void setDataset_Null() {
    presenter.setDataset(null);
  }

  @Test
  public void filenameEdit_Empty() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = presenter.validateDatasetFile();
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

    BinderValidationStatus<EditableFile> status = presenter.validateDatasetFile();
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
    File file = temporaryFolder.newFile("source.txt");
    temporaryFolder.newFile("abc.txt");
    EditableFile efile = new EditableFile(file);
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(efile);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<EditableFile> status = presenter.validateDatasetFile();
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

    BinderValidationStatus<EditableFile> status = presenter.validateDatasetFile();
    assertTrue(status.isOk());
  }

  @Test
  public void filenameEdit_ItemNull() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = presenter.validateDatasetFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void fileCount() {
    when(sampleService.files(any())).thenReturn(
        Arrays.asList(Paths.get("test1.txt"), Paths.get("test2.txt"), Paths.get("test3.txt")));
    assertEquals(3, presenter.fileCount(sample));
    verify(sampleService).files(sample);
  }

  @Test
  public void viewFiles() {
    presenter.viewFiles(sample);
    verify(dialog.sampleFilesDialog).setSample(sample);
    verify(dialog.sampleFilesDialog).open();
  }

  @Test
  public void add() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);

    presenter.add();

    verify(dialog.addFilesDialog).open();
  }

  @Test
  public void add_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);

    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NotEditable() {
    Dataset dataset = repository.findById(5L).get();
    presenter.setDataset(dataset);

    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NoDataset() {
    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_Saved() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(dialog.addFilesDialog).addSavedListener(savedListenerCaptor.capture());

    savedListenerCaptor.getValue().onComponentEvent(new SavedEvent<>(dialog.addFilesDialog, false));

    verify(service, atLeast(2)).files(dataset);
  }

  @Test
  public void rename() throws Throwable {
    File file = temporaryFolder.newFile("source.txt");
    EditableFile efile = new EditableFile(file);
    efile.setFilename("target.txt");
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(file.toPath(), bytes);

    presenter.rename(efile);

    assertTrue(Files.exists(file.toPath().resolveSibling("target.txt")));
    assertArrayEquals(bytes, Files.readAllBytes(file.toPath().resolveSibling("target.txt")));
    assertFalse(Files.exists(file.toPath()));
  }

  @Test
  public void deleteFile() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    File file = temporaryFolder.newFile("source.txt");
    EditableFile efile = new EditableFile(file);
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(file.toPath(), bytes);

    presenter.deleteFile(efile);

    verify(service).deleteFile(dataset, efile.getFile().toPath());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshSamplesOnSampleFilesDialogClose() {
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    dialog.samples.setDataProvider(mock(DataProvider.class));
    verify(dialog.sampleFilesDialog)
        .addOpenedChangeListener(openedSampleFilesListenerCaptor.capture());
    ComponentEventListener<OpenedChangeEvent<Dialog>> openedSampleFilesListener =
        openedSampleFilesListenerCaptor.getValue();
    OpenedChangeEvent<Dialog> event = mock(OpenedChangeEvent.class);
    when(event.isOpened()).thenReturn(false);
    openedSampleFilesListener.onComponentEvent(event);
    verify(dialog.samples.getDataProvider()).refreshAll();
  }
}
