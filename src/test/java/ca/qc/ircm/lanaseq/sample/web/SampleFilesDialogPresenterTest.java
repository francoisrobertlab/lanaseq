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
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES_IOEXCEPTION;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES_SUCCESS;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link SampleFilesDialogPresenter}.
 */
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
  private AuthenticatedUser authenticatedUser;
  @MockBean
  private AppConfiguration configuration;
  @Captor
  private ArgumentCaptor<Sample> sampleCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SavedEvent<AddSampleFilesDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Autowired
  private SampleRepository repository;
  @Mock
  private VaadinSession session;
  @TempDir
  Path temporaryFolder;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleFilesDialog.class, locale);
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
    dialog.header = new H3();
    dialog.message = new Div();
    dialog.folders = new VerticalLayout();
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
    when(configuration.getArchives().get(0).folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null
          ? temporaryFolder.resolve("archives").resolve(sample.getName())
          : null;
    });
    when(configuration.getArchives().get(1).folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null
          ? temporaryFolder.resolve("archives2").resolve(sample.getName())
          : null;
    });
    random.nextBytes(fileContent);
    presenter.init(dialog);
    presenter.localeChange(locale);
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    verify(service).folderLabels(sample, false);
    assertEquals(resources.message(DatasetFilesDialog.MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertTrue(dialog.folders.getComponentAt(i) instanceof Span);
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void labels_Linux() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    verify(service).folderLabels(sample, true);
    assertEquals(resources.message(DatasetFilesDialog.MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertTrue(dialog.folders.getComponentAt(i) instanceof Span);
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void labels_Mac() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    verify(service).folderLabels(sample, true);
    assertEquals(resources.message(DatasetFilesDialog.MESSAGE, labels.size()),
        dialog.message.getText());
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
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    verify(service).folderLabels(sample, false);
    assertEquals(resources.message(MESSAGE, 1), dialog.message.getText());
    assertEquals(1, dialog.folders.getComponentCount());
    assertTrue(dialog.folders.getComponentAt(0) instanceof Span);
    assertEquals(labels.get(0), dialog.folders.getComponentAt(0).getElement().getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void labels_None() {
    labels.clear();
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    verify(service).folderLabels(sample, false);
    assertEquals(resources.message(MESSAGE, 0), dialog.message.getText());
    assertEquals(0, dialog.folders.getComponentCount());
  }

  @Test
  public void getSample() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    assertEquals(sample, presenter.getSample());
  }

  @Test
  public void setSample_NewSample() {
    assertThrows(NullPointerException.class, () -> {
      Sample sample = new Sample();

      presenter.setSample(sample);
    });
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
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(false);
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

  @Test
  public void setSample_Null() {
    assertThrows(NullPointerException.class, () -> {
      presenter.setSample(null);
    });
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
    File path = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
    Files.createFile(temporaryFolder.resolve("abc.txt"));
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
    Files.createFile(temporaryFolder.resolve("abc.txt"));
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
  public void addSmallFiles() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    final Path tempfile = temporaryFolder.resolve("lanaseq-test-");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
    verify(service).saveFiles(eq(sample), filesCaptor.capture());
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
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    doThrow(new IllegalStateException("test")).when(service).saveFiles(any(), any());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityContextHolder.getContext().setAuthentication(null);

    presenter.addSmallFile(filename, new ByteArrayInputStream(fileContent));

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    assertEquals(1, filesCaptor.getValue().size());
    Path file = filesCaptor.getValue().stream().findFirst().get();
    verify(dialog).showNotification(resources.message(FILES_IOEXCEPTION, filename));
    assertFalse(Files.exists(file));
    assertFalse(Files.exists(file.getParent()));
  }

  @Test
  public void addLargeFiles() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);

    presenter.addLargeFiles();

    verify(dialog.addFilesDialog).open();
  }

  @Test
  public void addLargeFiles_CannotWrite() {
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(false);
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);

    presenter.addLargeFiles();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NotEditable() {
    Sample sample = repository.findById(8L).get();
    presenter.setSample(sample);

    presenter.addLargeFiles();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NoSample() {
    presenter.addLargeFiles();

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
    File path = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
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
  public void download() throws Throwable {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
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
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    File path = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
    EditableFile file = new EditableFile(path);
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path.toPath(), bytes);

    presenter.deleteFile(file);

    verify(service).deleteFile(sample, file.getFile().toPath());
  }
}
