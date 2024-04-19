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
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ADD_LARGE_FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME_HTML;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES_IOEXCEPTION;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES_SUCCESS;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FOLDERS;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.MAXIMUM_SMALL_FILES_COUNT;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.MAXIMUM_SMALL_FILES_SIZE;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.properties;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.englishUploadI18N;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.frenchUploadI18N;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.EditableFile;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorCloseEvent;
import com.vaadin.flow.component.grid.editor.EditorCloseListener;
import com.vaadin.flow.component.grid.editor.EditorImpl;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.testbench.unit.MetaKeys;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link SampleFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleFilesDialogTest extends SpringUIUnitTest {
  @TempDir
  Path temporaryFolder;
  private SampleFilesDialog dialog;
  @MockBean
  private ObjectFactory<AddSampleFilesDialog> addFilesDialogFactory;
  @MockBean
  private SampleService service;
  @MockBean
  private AppConfiguration configuration;
  @Captor
  private ArgumentCaptor<LitRenderer<EditableFile>> litRendererCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Anchor, EditableFile>> anchorRendererCaptor;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, EditableFile>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<EditorCloseListener<EditableFile>> closeListenerCaptor;
  @Captor
  private ArgumentCaptor<Comparator<EditableFile>> comparatorCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<SampleDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<SampleDialog>> deletedListener;
  @Autowired
  private SampleRepository repository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<File> files = new ArrayList<>();
  private List<String> labels = new ArrayList<>();
  private byte[] fileContent = new byte[5120];
  private Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    Sample defaultSample = repository.findById(4L).get();
    files.add(new File(defaultSample.getName(),
        "FR2_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R2_20181020_R1.fastq"));
    files.add(new File(defaultSample.getName(),
        "FR2_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R2_20181020_R2.fastq"));
    files.add(
        new File(defaultSample.getName(), "FR2_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R2_20181020.bw"));
    files.add(new File("archives", "sample.png"));
    when(service.files(any()))
        .thenReturn(files.stream().map(file -> file.toPath()).collect(Collectors.toList()));
    labels.add("\\\\lanaseq01\\lanaseq");
    labels.add("\\\\lanaseq01\\archives");
    labels.add("\\\\lanaseq02\\archives2");
    when(service.folderLabels(any(), anyBoolean())).thenReturn(labels);
    when(configuration.getHome()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getHome().folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null ? Paths.get(sample.getName()) : null;
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
    when(configuration.getUpload()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    random.nextBytes(fileContent);
    UI.getCurrent().setLocale(locale);
    SamplesView view = navigate(SamplesView.class);
    view.samples.setItems(repository.findAll());
    test(view.samples).clickRow(1, new MetaKeys().ctrl());
    dialog = $(SampleFilesDialog.class).first();
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(MESSAGE), dialog.message.getId().orElse(""));
    assertEquals(id(FOLDERS), dialog.folders.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(FILENAME), dialog.filenameEdit.getId().orElse(""));
    assertEquals(id(UPLOAD), dialog.upload.getId().orElse(""));
    assertEquals(id(ADD_LARGE_FILES), dialog.addLargeFiles.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), dialog.addLargeFiles.getIcon());
  }

  @Test
  public void labels() {
    Sample sample = dialog.getSample();
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(resources.message(FILENAME), headerRow.getCell(dialog.filename).getText());
    assertEquals(webResources.message(DOWNLOAD), headerRow.getCell(dialog.download).getText());
    assertEquals(webResources.message(DELETE), headerRow.getCell(dialog.delete).getText());
    validateEquals(englishUploadI18N(), dialog.upload.getI18n());
    assertEquals(resources.message(ADD_LARGE_FILES), dialog.addLargeFiles.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(SampleFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    UI.getCurrent().setLocale(locale);
    Sample sample = dialog.getSample();
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(resources.message(FILENAME), headerRow.getCell(dialog.filename).getText());
    assertEquals(webResources.message(DOWNLOAD), headerRow.getCell(dialog.download).getText());
    assertEquals(webResources.message(DELETE), headerRow.getCell(dialog.delete).getText());
    validateEquals(frenchUploadI18N(), dialog.upload.getI18n());
    assertEquals(resources.message(ADD_LARGE_FILES), dialog.addLargeFiles.getText());
  }

  @Test
  public void files() {
    assertEquals(3, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertTrue(dialog.files.getColumnByKey(FILENAME).isSortable());
    assertNotNull(dialog.files.getColumnByKey(DOWNLOAD));
    assertFalse(dialog.files.getColumnByKey(DOWNLOAD).isSortable());
    assertNotNull(dialog.files.getColumnByKey(DELETE));
    assertFalse(dialog.files.getColumnByKey(DELETE).isSortable());
  }

  @Test
  public void files_ColumnsValueProvider() {
    Sample sample = repository.findById(4L).get();
    dialog.setSample(sample);
    for (int i = 0; i < files.size(); i++) {
      File path = files.get(i);
      EditableFile file = new EditableFile(path);
      Renderer<EditableFile> filenameRawRenderer =
          dialog.files.getColumnByKey(FILENAME).getRenderer();
      assertTrue(filenameRawRenderer instanceof LitRenderer<EditableFile>);
      LitRenderer<EditableFile> filenameRenderer = (LitRenderer<EditableFile>) filenameRawRenderer;
      assertEquals(FILENAME_HTML, rendererTemplate(filenameRenderer));
      assertTrue(properties(filenameRenderer).containsKey("title"));
      assertTrue(properties(filenameRenderer).containsKey("filename"));
      assertEquals(file.getFilename(), properties(filenameRenderer).get("title").apply(file));
      if (file.getFilename().contains(sample.getName())) {
        String filename = file.getFilename().replaceAll(sample.getName(), "");
        assertEquals("FR2_MNasese..._20181020" + filename,
            properties(filenameRenderer).get("filename").apply(file));
      } else {
        assertEquals(file.getFilename(), properties(filenameRenderer).get("filename").apply(file));
      }
      Anchor downloadAnchor =
          (Anchor) test(dialog.files).getCellComponent(i, dialog.download.getKey());
      assertTrue(downloadAnchor.hasClassName(DOWNLOAD));
      assertTrue(downloadAnchor.getElement().hasAttribute("download"));
      assertEquals("", downloadAnchor.getElement().getAttribute("download"));
      assertNotEquals("", downloadAnchor.getHref());
      assertEquals(1, downloadAnchor.getChildren().toArray().length);
      Component downloadAnchorChild = downloadAnchor.getChildren().findFirst().get();
      assertTrue(downloadAnchorChild instanceof Button);
      Button downloadButton = (Button) downloadAnchorChild;
      validateIcon(VaadinIcon.DOWNLOAD.create(), downloadButton.getIcon());
      assertEquals("", downloadButton.getText());
      Button deleteButton = (Button) test(dialog.files).getCellComponent(i, dialog.delete.getKey());
      assertTrue(deleteButton.hasClassName(DELETE));
      assertTrue(deleteButton.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(i < files.size() - 1, deleteButton.isEnabled(), path.toString());
      validateIcon(VaadinIcon.TRASH.create(), deleteButton.getIcon());
      assertEquals("", deleteButton.getText());
      clickButton(deleteButton);
      verify(service).deleteFile(sample, path.toPath());
      verify(service, atLeast(2)).files(sample);
    }
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void folderLabels() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
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
  public void folderLabels_Linux() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
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
  public void folderLabels_Mac() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
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
  public void folderLabels_One() {
    IntStream.range(1, labels.size()).forEach(i -> labels.remove(1));
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    verify(service).folderLabels(sample, false);
    assertEquals(resources.message(MESSAGE, 1), dialog.message.getText());
    assertEquals(1, dialog.folders.getComponentCount());
    assertTrue(dialog.folders.getComponentAt(0) instanceof Span);
    assertEquals(labels.get(0), dialog.folders.getComponentAt(0).getElement().getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void folderLabels_None() {
    labels.clear();
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);
    verify(service).folderLabels(sample, false);
    assertEquals(resources.message(MESSAGE, 0), dialog.message.getText());
    assertEquals(0, dialog.folders.getComponentCount());
  }

  @Test
  public void filenameEdit_Empty() {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = dialog.validateSampleFile();
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

  public void filenameEdit_Invalid(String filename) {
    dialog.filenameEdit.setValue(filename);

    BinderValidationStatus<EditableFile> status = dialog.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(resources.message(FILENAME_REGEX_ERROR)), error.getMessage());
  }

  @Test
  public void filenameEdit_Exists() throws Throwable {
    File path = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
    Files.createFile(temporaryFolder.resolve("abc.txt"));
    EditableFile file = new EditableFile(path);
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(file);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<EditableFile> status = dialog.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(ALREADY_EXISTS)), error.getMessage());
  }

  @Test
  public void filenameEdit_ExistsSameFile() throws Throwable {
    Files.createFile(temporaryFolder.resolve("abc.txt"));
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(null);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<EditableFile> status = dialog.validateSampleFile();
    assertTrue(status.isOk());
  }

  @Test
  public void filenameEdit_ItemNull() {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = dialog.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void filenameEdit_Saved() throws Exception {
    Path path = temporaryFolder.resolve(files.get(0).toPath());
    EditableFile file = new EditableFile(path.toFile());
    Files.createDirectories(path.getParent());
    Files.write(path, fileContent);
    Path sibling = path.resolveSibling("new_name.txt");
    file.setFilename(sibling.getFileName().toString());
    EditorImpl<EditableFile> editor = (EditorImpl) dialog.files.getEditor();
    Method method = EditorImpl.class.getDeclaredMethod("fireCloseEvent", EditorCloseEvent.class);
    method.setAccessible(true);

    method.invoke(editor, new EditorCloseEvent(editor, file));

    assertTrue(Files.exists(sibling));
    assertFalse(Files.exists(path));
    assertArrayEquals(fileContent, Files.readAllBytes(sibling));
  }

  @Test
  public void upload() {
    assertEquals(MAXIMUM_SMALL_FILES_COUNT, dialog.upload.getMaxFiles());
    assertEquals(MAXIMUM_SMALL_FILES_SIZE, dialog.upload.getMaxFileSize());
  }

  @Test
  public void upload_File() throws Exception {
    Sample sample = dialog.getSample();
    String filename = "test_file.txt";
    String mimeType = "text/plain";
    final Path tempFile = temporaryFolder.resolve("lanaseq-test-");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Mockito.doAnswer(i -> {
      Collection<Path> files = i.getArgument(1);
      Path file = files.stream().findFirst().orElse(null);
      Files.copy(file, tempFile, StandardCopyOption.REPLACE_EXISTING);
      assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
      return null;
    }).when(service).saveFiles(any(), any());
    SecurityContextHolder.getContext().setAuthentication(null);

    test(dialog.upload).upload(filename, mimeType, fileContent);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    assertEquals(1, filesCaptor.getValue().size());
    Path file = filesCaptor.getValue().stream().findFirst().get();
    assertEquals(filename, file.getFileName().toString());
    assertArrayEquals(fileContent, Files.readAllBytes(tempFile));
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(FILES_SUCCESS, filename), test(notification).getText());
    assertFalse(Files.exists(file));
    assertFalse(Files.exists(file.getParent()));
  }

  @Test
  public void upload_Error() {
    Sample sample = dialog.getSample();
    String filename = "test_file.txt";
    String mimeType = "text/plain";
    doThrow(new IllegalStateException("test")).when(service).saveFiles(any(), any());
    SecurityContextHolder.getContext().setAuthentication(null);

    test(dialog.upload).upload(filename, mimeType, fileContent);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(service).saveFiles(eq(sample), filesCaptor.capture());
    assertEquals(1, filesCaptor.getValue().size());
    Path file = filesCaptor.getValue().stream().findFirst().get();
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(FILES_IOEXCEPTION, filename), test(notification).getText());
    assertFalse(Files.exists(file));
    assertFalse(Files.exists(file.getParent()));
  }

  @Test
  public void getSample() {
    Sample sample = repository.findById(10L).get();
    assertEquals(sample, dialog.getSample());
  }

  @Test
  public void setSample_NewSample() {
    assertThrows(NullPointerException.class, () -> {
      Sample sample = new Sample();

      dialog.setSample(sample);
    });
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(10L).get();

    dialog.setSample(sample);

    verify(service, atLeastOnce()).files(sample);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
    assertTrue(dialog.delete.isVisible());
    assertFalse(dialog.filenameEdit.isReadOnly());
  }

  @Test
  public void setSample_CannotWrite() {
    Sample sample = repository.findById(1L).get();

    dialog.setSample(sample);

    verify(service).files(sample);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void setSample_NotEditable() {
    Sample sample = repository.findById(8L).get();

    dialog.setSample(sample);

    verify(service).files(sample);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertEquals(resources.message(HEADER, sample.getName()), dialog.getHeaderTitle());
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test
  public void setSample_Null() {
    assertThrows(NullPointerException.class, () -> {
      dialog.setSample(null);
    });
  }

  @Test
  public void addLargeFiles() {
    Sample sample = dialog.getSample();
    verify(service).files(sample);

    dialog.addLargeFiles.click();

    AddSampleFilesDialog largeFilesDialog = $(AddSampleFilesDialog.class).first();
    assertEquals(sample, largeFilesDialog.getSample());
    largeFilesDialog.fireSavedEvent();
    verify(service, atLeast(2)).files(sample);
  }

  @Test
  public void addLargeFiles_CannotWrite() {
    Sample sample = repository.findById(1L).get();
    dialog.setSample(sample);

    dialog.addLargeFiles.click();

    assertFalse($(AddSampleFilesDialog.class).exists());
  }

  @Test
  public void addLargeFiles_NotEditable() {
    Sample sample = repository.findById(8L).get();
    dialog.setSample(sample);

    dialog.addLargeFiles.click();

    assertFalse($(AddSampleFilesDialog.class).exists());
  }

  @Test
  public void download() throws Throwable {
    File path = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
    EditableFile file = new EditableFile(path);
    Files.write(path.toPath(), fileContent);

    StreamResource resource = dialog.download(file);

    assertEquals("source.txt", resource.getName());
    StreamResourceWriter writer = resource.getWriter();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writer.accept(output, UI.getCurrent().getSession());
    assertArrayEquals(fileContent, output.toByteArray());
  }
}
