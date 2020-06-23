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
import ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.DatasetFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
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
  private ProtocolService protocolService;
  @MockBean
  private AuthorizationService authorizationService;
  @MockBean
  private AppConfiguration configuration;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<AddDatasetFilesDialog>>> savedListenerCaptor;
  @Autowired
  private DatasetRepository repository;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Path> files = new ArrayList<>();
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
    dialog.addFilesDialog = mock(AddDatasetFilesDialog.class);
    files.add(Paths.get("dataset_R1.fastq"));
    files.add(Paths.get("dataset_R2.fastq"));
    files.add(Paths.get("dataset.bw"));
    files.add(Paths.get("dataset.png"));
    when(dialog.getUI()).thenReturn(Optional.of(ui));
    when(service.files(any())).thenReturn(files);
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
    List<DatasetFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (DatasetFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertTrue(dialog.delete.isVisible());
    assertFalse(dialog.filenameEdit.isReadOnly());
    verify(dialog.addFilesDialog).setDataset(dataset);
  }

  @Test
  public void setDataset_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    verify(service).files(dataset);
    List<DatasetFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (DatasetFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test
  public void setDataset_NotEditable() {
    Dataset dataset = repository.findById(5L).get();

    presenter.setDataset(dataset);

    verify(service).files(dataset);
    List<DatasetFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (DatasetFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test(expected = NullPointerException.class)
  public void setDataset_Null() {
    presenter.setDataset(null);
  }

  @Test
  public void filenameEdit_Empty() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<DatasetFile> status = presenter.validateDatasetFile();
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

    BinderValidationStatus<DatasetFile> status = presenter.validateDatasetFile();
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
    Path path = temporaryFolder.newFile("source.txt").toPath();
    temporaryFolder.newFile("abc.txt");
    DatasetFile file = new DatasetFile(path);
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(file);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<DatasetFile> status = presenter.validateDatasetFile();
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

    BinderValidationStatus<DatasetFile> status = presenter.validateDatasetFile();
    assertTrue(status.isOk());
  }

  @Test
  public void filenameEdit_ItemNull() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<DatasetFile> status = presenter.validateDatasetFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
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
    Path path = temporaryFolder.newFile("source.txt").toPath();
    DatasetFile file = new DatasetFile(path);
    file.setFilename("target.txt");
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path, bytes);

    presenter.rename(file);

    assertTrue(Files.exists(path.resolveSibling("target.txt")));
    assertArrayEquals(bytes, Files.readAllBytes(path.resolveSibling("target.txt")));
    assertFalse(Files.exists(path));
  }

  @Test
  public void deleteFile() throws Throwable {
    Path path = temporaryFolder.newFile("source.txt").toPath();
    DatasetFile file = new DatasetFile(path);
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path, bytes);

    presenter.deleteFile(file);

    assertFalse(Files.exists(path));
  }
}
