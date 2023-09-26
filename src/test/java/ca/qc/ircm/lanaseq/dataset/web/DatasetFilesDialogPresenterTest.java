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

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.*;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DatasetFilesDialogPresenter}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetFilesDialogPresenterTest extends AbstractKaribuTestCase {
  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetFilesDialogPresenter presenter;
  @Mock
  private DatasetFilesDialog dialog;
  @MockBean
  private DatasetService service;
  @MockBean
  private SampleService sampleService;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @MockBean
  private AppConfiguration configuration;
  @Autowired
  private ObjectFactory<AddDatasetFilesDialog> addFilesDialogFactory;
  @MockBean
  private AddDatasetFilesDialog addFilesDialog;
  @Autowired
  private ObjectFactory<SampleFilesDialog> sampleFilesDialogFactory;
  @MockBean
  private SampleFilesDialog sampleFilesDialog;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<AddDatasetFilesDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<OpenedChangeEvent<Dialog>>> openedSampleFilesListenerCaptor;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Autowired
  private DatasetRepository repository;
  @Mock
  private VaadinSession session;
  @Mock
  private Sample sample;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<File> files = new ArrayList<>();
  private List<String> labels = new ArrayList<>();
  private String folderLabelLinux = "lanaseq/upload";
  private String folderLabelWindows = "lanaseq\\upload";
  private String filename = "test file";
  private byte[] fileContent = new byte[5120];
  private Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog.message = new Div();
    dialog.folders = new VerticalLayout();
    dialog.files = new Grid<>();
    dialog.samples = new Grid<>();
    dialog.delete = dialog.files.addColumn(file -> file);
    dialog.filenameEdit = new TextField();
    dialog.addFilesDialogFactory = addFilesDialogFactory;
    dialog.sampleFilesDialogFactory = sampleFilesDialogFactory;
    files.add(new File("dataset_R1.fastq"));
    files.add(new File("dataset_R2.fastq"));
    files.add(new File("dataset.bw"));
    files.add(new File("dataset.png"));
    when(dialog.getUI()).thenReturn(Optional.of(ui));
    when(service.files(any()))
        .thenReturn(files.stream().map(file -> file.toPath()).collect(Collectors.toList()));
    labels.add("\\\\lanaseq01\\lanaseq");
    labels.add("\\\\lanaseq01\\archives");
    labels.add("\\\\lanaseq02\\archives2");
    when(service.folderLabels(any(), anyBoolean())).thenReturn(labels);
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    when(configuration.getHome()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getHome().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve(dataset.getName())
          : null;
    });
    List archives = new ArrayList();
    archives.add(mock(AppConfiguration.NetworkDrive.class));
    archives.add(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getArchives()).thenReturn(archives);
    when(configuration.getArchives().get(0).folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve("archives").resolve(dataset.getName())
          : null;
    });
    when(configuration.getArchives().get(1).folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve("archives2").resolve(dataset.getName())
          : null;
    });
    random.nextBytes(fileContent);
    presenter.init(dialog);
    presenter.localeChange(locale);
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(service).folderLabels(dataset, false);
    assertEquals(resources.message(MESSAGE, labels.size()), dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertTrue(dialog.folders.getComponentAt(i) instanceof Span);
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void labels_Linux() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(service).folderLabels(dataset, true);
    assertEquals(resources.message(MESSAGE, labels.size()), dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertTrue(dialog.folders.getComponentAt(i) instanceof Span);
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void labels_Mac() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(service).folderLabels(dataset, true);
    assertEquals(resources.message(MESSAGE, labels.size()), dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertTrue(dialog.folders.getComponentAt(i) instanceof Span);
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels_One() {
    IntStream.range(1, labels.size()).forEach(i -> labels.remove(1));
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(service).folderLabels(dataset, false);
    assertEquals(resources.message(MESSAGE, 1), dialog.message.getText());
    assertEquals(1, dialog.folders.getComponentCount());
    assertTrue(dialog.folders.getComponentAt(0) instanceof Span);
    assertEquals(labels.get(0), dialog.folders.getComponentAt(0).getElement().getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels_None() {
    labels.clear();
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    verify(service).folderLabels(dataset, false);
    assertEquals(resources.message(MESSAGE, 0), dialog.message.getText());
    assertEquals(0, dialog.folders.getComponentCount());
  }

  @Test
  public void getDataset() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(dataset, presenter.getDataset());
  }

  @Test
  public void setDataset_NewDataset() {
    assertThrows(NullPointerException.class, () -> {
      Dataset dataset = new Dataset();

      presenter.setDataset(dataset);
    });
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
  }

  @Test
  public void setDataset_CannotWrite() {
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(false);
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

  @Test
  public void setDataset_Null() {
    assertThrows(NullPointerException.class, () -> {
      presenter.setDataset(null);
    });
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
    filenameEdit_Invalid("abc#.txt");
    filenameEdit_Invalid("abc%.txt");
    filenameEdit_Invalid("abc&.txt");
    filenameEdit_Invalid("abc{.txt");
    filenameEdit_Invalid("abc}.txt");
    filenameEdit_Invalid("abc\\.txt");
    filenameEdit_Invalid("abc<.txt");
    filenameEdit_Invalid("abc>.txt");
    filenameEdit_Invalid("abc*.txt");
    filenameEdit_Invalid("abc?.txt");
    filenameEdit_Invalid("abc/.txt");
    filenameEdit_Invalid("abc .txt");
    filenameEdit_Invalid("abc$.txt");
    filenameEdit_Invalid("abc!.txt");
    filenameEdit_Invalid("abc'.txt");
    filenameEdit_Invalid("abc\".txt");
    filenameEdit_Invalid("abc:.txt");
    filenameEdit_Invalid("abc@.txt");
    filenameEdit_Invalid("abc+.txt");
    filenameEdit_Invalid("abc`.txt");
    filenameEdit_Invalid("abc|.txt");
    filenameEdit_Invalid("abc=.txt");
  }

  public void filenameEdit_Invalid(String filename) throws Throwable {
    dialog.filenameEdit.setValue(filename);

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
    File file = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
    Files.createFile(temporaryFolder.resolve("abc.txt"));
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
    Files.createFile(temporaryFolder.resolve("abc.txt"));
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
  public void isArchive_False() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    File file = configuration.getHome().folder(dataset).resolve("source.txt").toFile();
    EditableFile efile = new EditableFile(file);
    assertFalse(presenter.isArchive(efile));
  }

  @Test
  public void isArchive_True() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    File file = configuration.getArchives().get(0).folder(dataset).resolve("source.txt").toFile();
    EditableFile efile = new EditableFile(file);
    assertTrue(presenter.isArchive(efile));
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
    verify(sampleFilesDialog).setSample(sample);
    verify(sampleFilesDialog).addOpenedChangeListener(any());
    verify(sampleFilesDialog).open();
  }

  @Test
  public void addSmallFiles() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final Path tempfile = temporaryFolder.resolve("lanaseq-test-");
    Mockito.doAnswer(i -> {
      Collection<Path> files = i.getArgument(1);
      Path file = files.stream().findFirst().orElse(null);
      Files.copy(file, tempfile, StandardCopyOption.REPLACE_EXISTING);
      assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
      return null;
    }).when(service).saveFiles(any(), any());
    SecurityContextHolder.getContext().setAuthentication(null);

    presenter.addSmallFile(filename, new ByteArrayInputStream(fileContent));

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    assertEquals(1, filesCaptor.getValue().size());
    Path file = filesCaptor.getValue().stream().findFirst().get();
    assertEquals(filename, file.getFileName().toString());
    assertArrayEquals(fileContent, Files.readAllBytes(tempfile));
    verify(dialog).showNotification(resources.message(FILES_SUCCESS, filename));
    assertFalse(Files.exists(file));
    assertFalse(Files.exists(file.getParent()));
  }

  @Test
  public void addSmallFiles_Error() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    doThrow(new IllegalStateException("test")).when(service).saveFiles(any(), any());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityContextHolder.getContext().setAuthentication(null);

    presenter.addSmallFile(filename, new ByteArrayInputStream(fileContent));

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    assertEquals(1, filesCaptor.getValue().size());
    Path file = filesCaptor.getValue().stream().findFirst().get();
    verify(dialog).showNotification(resources.message(FILES_IOEXCEPTION, filename));
    assertFalse(Files.exists(file));
    assertFalse(Files.exists(file.getParent()));
  }

  @Test
  public void addLargeFiles() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);

    presenter.addLargeFiles();

    verify(addFilesDialog).setDataset(dataset);
    verify(addFilesDialog).addSavedListener(any());
    verify(addFilesDialog).open();
  }

  @Test
  public void addLargeFiles_CannotWrite() {
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(false);
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);

    presenter.addLargeFiles();

    verify(addFilesDialog, never()).setDataset(any());
    verify(addFilesDialog, never()).addSavedListener(any());
    verify(addFilesDialog, never()).open();
  }

  @Test
  public void addLargeFiles_NotEditable() {
    Dataset dataset = repository.findById(5L).get();
    presenter.setDataset(dataset);

    presenter.addLargeFiles();

    verify(addFilesDialog, never()).setDataset(any());
    verify(addFilesDialog, never()).addSavedListener(any());
    verify(addFilesDialog, never()).open();
  }

  @Test
  public void addLargeFiles_NoDataset() {
    presenter.addLargeFiles();

    verify(addFilesDialog, never()).setDataset(any());
    verify(addFilesDialog, never()).addSavedListener(any());
    verify(addFilesDialog, never()).open();
  }

  @Test
  public void addLargeFiles_Saved() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.addLargeFiles();
    verify(addFilesDialog).addSavedListener(savedListenerCaptor.capture());

    savedListenerCaptor.getValue().onComponentEvent(new SavedEvent<>(addFilesDialog, false));

    verify(service, atLeast(2)).files(dataset);
  }

  @Test
  public void rename() throws Throwable {
    File file = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
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
  public void download() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    File path = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
    EditableFile file = new EditableFile(path);
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path.toPath(), bytes);

    StreamResource resource = presenter.download(file);

    assertEquals("source.txt", resource.getName());
    StreamResourceWriter writer = resource.getWriter();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writer.accept(output, session);
    assertArrayEquals(bytes, output.toByteArray());
  }

  @Test
  public void deleteFile() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    File file = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
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
    dialog.samples.setItems(mock(DataProvider.class));
    presenter.viewFiles(sample);

    verify(sampleFilesDialog).addOpenedChangeListener(openedSampleFilesListenerCaptor.capture());
    ComponentEventListener<OpenedChangeEvent<Dialog>> openedSampleFilesListener =
        openedSampleFilesListenerCaptor.getValue();
    OpenedChangeEvent<Dialog> event = mock(OpenedChangeEvent.class);
    when(event.isOpened()).thenReturn(false);
    openedSampleFilesListener.onComponentEvent(event);
    verify(dialog.samples.getDataProvider()).refreshAll();
  }
}
