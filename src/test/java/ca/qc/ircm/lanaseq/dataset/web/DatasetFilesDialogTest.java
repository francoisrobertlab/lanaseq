package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.Constants.REFRESH;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.SAMPLES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ADD_LARGE_FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME_HTML;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES_IOEXCEPTION;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES_SUCCESS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILE_COUNT;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FOLDERS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MAXIMUM_SMALL_FILES_COUNT;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MAXIMUM_SMALL_FILES_SIZE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.id;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.PUBLIC_FILE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import ca.qc.ircm.lanaseq.web.EditableFile;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorCloseEvent;
import com.vaadin.flow.component.grid.editor.EditorImpl;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
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
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link DatasetFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetFilesDialogTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(DatasetFilesDialog.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @TempDir
  Path temporaryFolder;
  private DatasetFilesDialog dialog;
  @MockitoBean
  private DatasetService service;
  @MockitoBean
  private SampleService sampleService;
  @MockitoBean
  private AppConfiguration configuration;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private SampleRepository sampleRepository;
  @Mock
  private Grid<EditableFile> filesGrid;
  @Mock
  private Editor<EditableFile> filesGridEditor;
  private final Locale locale = Locale.ENGLISH;
  private final List<File> files = new ArrayList<>();
  private final List<String> labels = new ArrayList<>();
  private List<Sample> samples = new ArrayList<>();
  private final byte[] fileContent = new byte[5120];
  private final Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    when(sampleService.get(anyLong())).then(
        i -> i.getArgument(0) != null ? sampleRepository.findById(i.getArgument(0))
            : Optional.empty());
    Dataset defaultDataset = repository.findById(2L).orElseThrow();
    files.add(
        new File(defaultDataset.getName(), "ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022_R1.fastq"));
    files.add(
        new File(defaultDataset.getName(), "ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022_R2.fastq"));
    files.add(new File(defaultDataset.getName(), "ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022.bw"));
    files.add(new File("archives", "sample.png"));
    samples = sampleRepository.findAll();
    when(service.files(any())).thenReturn(
        files.stream().map(File::toPath).collect(Collectors.toList()));
    labels.add("\\\\lanaseq01\\lanaseq");
    labels.add("\\\\lanaseq01\\archives");
    labels.add("\\\\lanaseq02\\archives2");
    when(service.folderLabels(any(), anyBoolean())).thenReturn(labels);
    @SuppressWarnings("unchecked") AppConfiguration.NetworkDrive<DataWithFiles> homeFolder = mock(
        AppConfiguration.NetworkDrive.class);
    when(configuration.getHome()).thenReturn(homeFolder);
    when(configuration.getHome().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null ? Paths.get(dataset.getName()) : null;
    });
    List<AppConfiguration.NetworkDrive<DataWithFiles>> archives = new ArrayList<>();
    @SuppressWarnings("unchecked") AppConfiguration.NetworkDrive<DataWithFiles> archiveFolder1 = mock(
        AppConfiguration.NetworkDrive.class);
    archives.add(archiveFolder1);
    @SuppressWarnings("unchecked") AppConfiguration.NetworkDrive<DataWithFiles> archiveFolder2 = mock(
        AppConfiguration.NetworkDrive.class);
    archives.add(archiveFolder2);
    when(configuration.getArchives()).thenReturn(archives);
    when(configuration.getArchives().get(0).folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null ? temporaryFolder.resolve("archives").resolve(dataset.getName())
          : null;
    });
    when(configuration.getArchives().get(1).folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null ? temporaryFolder.resolve("archives2").resolve(dataset.getName())
          : null;
    });
    @SuppressWarnings("unchecked") AppConfiguration.NetworkDrive<DataWithFiles> uploadFolder = mock(
        AppConfiguration.NetworkDrive.class);
    when(configuration.getUpload()).thenReturn(uploadFolder);
    when(configuration.getUpload().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null ? temporaryFolder.resolve("upload").resolve(dataset.getName()) : null;
    });
    when(service.relativize(any(), any())).then(i -> i.getArgument(1));
    when(configuration.getUrl(any())).then(i -> "https://localhost:8080/" + i.getArgument(0));
    when(configuration.getPublicFilePeriod()).thenReturn(Period.ofDays(30));
    random.nextBytes(fileContent);

    UI.getCurrent().setLocale(locale);
    DatasetsView view = navigate(DatasetsView.class);
    view.datasets.setItems(repository.findAll());
    test(view.datasets).clickRow(1, new MetaKeys().ctrl());
    dialog = $(DatasetFilesDialog.class).first();
  }

  private EditableFile editableFile(String filename) {
    return new EditableFile(new File(filename));
  }

  private Sample sampleName(String name) {
    Sample sample = new Sample();
    sample.setName(name);
    return sample;
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(MESSAGE), dialog.message.getId().orElse(""));
    assertEquals(id(FOLDERS), dialog.folders.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(FILENAME), dialog.filenameEdit.getId().orElse(""));
    assertEquals(id(SAMPLES), dialog.samples.getId().orElse(""));
    assertEquals(id(REFRESH), dialog.refresh.getId().orElse(""));
    validateIcon(VaadinIcon.REFRESH.create(), dialog.refresh.getIcon());
    assertEquals(id(UPLOAD), dialog.upload.getId().orElse(""));
    assertEquals(id(ADD_LARGE_FILES), dialog.addLargeFiles.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), dialog.addLargeFiles.getIcon());
  }

  @Test
  public void labels() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    for (int i = 0; i < labels.size(); i++) {
      assertEquals(labels.get(i), ((Span) dialog.folders.getComponentAt(i)).getText());
    }
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DOWNLOAD),
        headerRow.getCell(dialog.download).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + PUBLIC_FILE),
        headerRow.getCell(dialog.publicFile).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DELETE),
        headerRow.getCell(dialog.delete).getText());
    HeaderRow samplesHeaderRow = dialog.samples.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + NAME),
        samplesHeaderRow.getCell(dialog.name).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILE_COUNT),
        samplesHeaderRow.getCell(dialog.fileCount).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + REFRESH), dialog.refresh.getText());
    validateEquals(englishUploadI18N(), dialog.upload.getI18n());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + ADD_LARGE_FILES),
        dialog.addLargeFiles.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    Dataset dataset = repository.findById(2L).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    for (int i = 0; i < labels.size(); i++) {
      assertEquals(labels.get(i), ((Span) dialog.folders.getComponentAt(i)).getText());
    }
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DOWNLOAD),
        headerRow.getCell(dialog.download).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + PUBLIC_FILE),
        headerRow.getCell(dialog.publicFile).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DELETE),
        headerRow.getCell(dialog.delete).getText());
    HeaderRow samplesHeaderRow = dialog.samples.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + NAME),
        samplesHeaderRow.getCell(dialog.name).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILE_COUNT),
        samplesHeaderRow.getCell(dialog.fileCount).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + REFRESH), dialog.refresh.getText());
    validateEquals(frenchUploadI18N(), dialog.upload.getI18n());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + ADD_LARGE_FILES),
        dialog.addLargeFiles.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void folderLabels() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    verify(service).folderLabels(dataset, false);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DatasetFilesDialog.MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertInstanceOf(Span.class, dialog.folders.getComponentAt(i));
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void folderLabels_Linux() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    verify(service).folderLabels(dataset, true);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertInstanceOf(Span.class, dialog.folders.getComponentAt(i));
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void folderLabels_Mac() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    verify(service).folderLabels(dataset, true);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DatasetFilesDialog.MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    IntStream.range(0, labels.size()).forEach(i -> {
      assertInstanceOf(Span.class, dialog.folders.getComponentAt(i));
      assertEquals(labels.get(i), dialog.folders.getComponentAt(i).getElement().getText());
    });
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void folderLabels_One() {
    IntStream.range(1, labels.size()).forEach(i -> labels.remove(1));
    Dataset dataset = repository.findById(2L).orElseThrow();
    dialog.setDatasetId(2L);
    verify(service, times(2)).folderLabels(dataset, false);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, 1), dialog.message.getText());
    assertEquals(1, dialog.folders.getComponentCount());
    assertInstanceOf(Span.class, dialog.folders.getComponentAt(0));
    assertEquals(labels.get(0), dialog.folders.getComponentAt(0).getElement().getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void folderLabels_None() {
    labels.clear();
    Dataset dataset = repository.findById(2L).orElseThrow();
    dialog.setDatasetId(2L);
    verify(service, times(2)).folderLabels(dataset, false);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, 0), dialog.message.getText());
    assertEquals(0, dialog.folders.getComponentCount());
  }

  @Test
  public void files() {
    assertEquals(4, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertTrue(dialog.files.getColumnByKey(FILENAME).isSortable());
    assertNotNull(dialog.files.getColumnByKey(DOWNLOAD));
    assertFalse(dialog.files.getColumnByKey(DOWNLOAD).isSortable());
    assertNotNull(dialog.files.getColumnByKey(PUBLIC_FILE));
    assertFalse(dialog.files.getColumnByKey(PUBLIC_FILE).isSortable());
    assertNotNull(dialog.files.getColumnByKey(DELETE));
    assertFalse(dialog.files.getColumnByKey(DELETE).isSortable());
    List<EditableFile> files = dialog.files.getListDataView().getItems().toList();
    assertEquals(this.files.size(), files.size());
    for (File file : this.files) {
      assertTrue(files.stream().anyMatch(ef -> ef.getFile().equals(file)));
    }
  }

  @Test
  public void files_ColumnsValueProvider() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    dialog.setDatasetId(2L);
    when(service.isFilePublic(any(), any())).then(
        i -> files.indexOf(((Path) i.getArgument(1)).toFile()) > 2);
    for (int i = 0; i < files.size(); i++) {
      File path = files.get(i);
      EditableFile file = new EditableFile(path);
      Renderer<EditableFile> filenameRawRenderer = dialog.files.getColumnByKey(
          SampleFilesDialog.FILENAME).getRenderer();
      assertInstanceOf(LitRenderer.class, filenameRawRenderer);
      LitRenderer<EditableFile> filenameRenderer = (LitRenderer<EditableFile>) filenameRawRenderer;
      assertEquals(FILENAME_HTML, rendererTemplate(filenameRenderer));
      assertTrue(properties(filenameRenderer).containsKey("title"));
      assertTrue(properties(filenameRenderer).containsKey("filename"));
      assertEquals(file.getFilename(), properties(filenameRenderer).get("title").apply(file));
      if (file.getFilename().contains(dataset.getName())) {
        String filename = file.getFilename().replaceAll(dataset.getName(), "");
        assertEquals("ChIPseq_Spt..._20181022" + filename,
            properties(filenameRenderer).get("filename").apply(file));
      } else {
        assertEquals(file.getFilename(), properties(filenameRenderer).get("filename").apply(file));
      }
      Anchor downloadAnchor = (Anchor) test(dialog.files).getCellComponent(i,
          dialog.download.getKey());
      assertTrue(downloadAnchor.hasClassName(DOWNLOAD));
      assertTrue(downloadAnchor.getElement().hasAttribute("download"));
      assertEquals("", downloadAnchor.getElement().getAttribute("download"));
      assertNotEquals("", downloadAnchor.getHref());
      assertEquals(1, downloadAnchor.getChildren().toArray().length);
      Component downloadAnchorChild = downloadAnchor.getChildren().findFirst().orElseThrow();
      assertInstanceOf(Button.class, downloadAnchorChild);
      Button downloadButton = (Button) downloadAnchorChild;
      validateIcon(VaadinIcon.DOWNLOAD.create(), downloadButton.getIcon());
      assertEquals("", downloadButton.getText());
      Checkbox publicFileCheckbox = (Checkbox) test(dialog.files).getCellComponent(i,
          dialog.publicFile.getKey());
      assertEquals(i > 2, publicFileCheckbox.getValue());
      publicFileCheckbox.setValue(!publicFileCheckbox.getValue());
      if (publicFileCheckbox.getValue()) {
        verify(service).allowPublicFileAccess(dataset, path.toPath(),
            LocalDate.now().plus(configuration.getPublicFilePeriod()));
      } else {
        verify(service).revokePublicFileAccess(dataset, path.toPath());
      }
      Button deleteButton = (Button) test(dialog.files).getCellComponent(i, dialog.delete.getKey());
      assertTrue(deleteButton.hasClassName(DELETE));
      assertTrue(deleteButton.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(i < files.size() - 1, deleteButton.isEnabled(), path.toString());
      validateIcon(VaadinIcon.TRASH.create(), deleteButton.getIcon());
      assertEquals("", deleteButton.getText());
    }
  }

  @Test
  public void files_FilenameColumnComparator() {
    Comparator<EditableFile> comparator = dialog.filename.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(editableFile("a"), editableFile("a")));
    assertTrue(comparator.compare(editableFile("a"), editableFile("e")) < 0);
    assertTrue(comparator.compare(editableFile("e"), editableFile("a")) > 0);
  }

  @Test
  public void files_DeleteButton() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    File path = files.get(0);
    Button deleteButton = (Button) test(dialog.files).getCellComponent(0, dialog.delete.getKey());
    clickButton(deleteButton);
    verify(service).deleteFile(dataset, path.toPath());
    verify(service, atLeast(2)).files(dataset);
  }

  @Test
  public void filenameEdit_Empty() {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = dialog.validateDatasetFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + REQUIRED)),
        error.getMessage());
  }

  @Test
  public void filenameEdit_Invalid() {
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

    BinderValidationStatus<EditableFile> status = dialog.validateDatasetFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(MESSAGE_PREFIX + FILENAME_REGEX_ERROR)),
        error.getMessage());
  }

  @Test
  public void filenameEdit_Exists() throws Throwable {
    File path = Files.createFile(temporaryFolder.resolve("source.txt")).toFile();
    Files.createFile(temporaryFolder.resolve("abc.txt"));
    EditableFile file = new EditableFile(path);
    dialog.files = filesGrid;
    when(dialog.files.getEditor()).thenReturn(filesGridEditor);
    when(dialog.files.getEditor().getItem()).thenReturn(file);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<EditableFile> status = dialog.validateDatasetFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + ALREADY_EXISTS)),
        error.getMessage());
  }

  @Test
  public void filenameEdit_ExistsSameFile() throws Throwable {
    Files.createFile(temporaryFolder.resolve("abc.txt"));
    dialog.files = filesGrid;
    when(dialog.files.getEditor()).thenReturn(filesGridEditor);
    when(dialog.files.getEditor().getItem()).thenReturn(null);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<EditableFile> status = dialog.validateDatasetFile();
    assertTrue(status.isOk());
  }

  @Test
  public void filenameEdit_ItemNull() {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<EditableFile> status = dialog.validateDatasetFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + REQUIRED)),
        error.getMessage());
  }

  @Test
  public void filenameEdit_Saved() throws Exception {
    Path path = temporaryFolder.resolve(files.get(0).toPath());
    EditableFile file = new EditableFile(path.toFile());
    Files.createDirectories(path.getParent());
    Files.write(path, fileContent);
    Path sibling = path.resolveSibling("new_name.txt");
    file.setFilename(sibling.getFileName().toString());
    EditorImpl<EditableFile> editor = (EditorImpl<EditableFile>) dialog.files.getEditor();
    Method method = EditorImpl.class.getDeclaredMethod("fireCloseEvent", EditorCloseEvent.class);
    method.setAccessible(true);

    method.invoke(editor, new EditorCloseEvent<>(editor, file));

    assertTrue(Files.exists(sibling));
    assertFalse(Files.exists(path));
    assertArrayEquals(fileContent, Files.readAllBytes(sibling));
  }

  @Test
  public void samples() {
    assertEquals(2, dialog.samples.getColumns().size());
    assertNotNull(dialog.samples.getColumnByKey(NAME));
    assertNotNull(dialog.samples.getColumnByKey(FILE_COUNT));
    Dataset dataset = repository.findById(2L).orElseThrow();
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertEquals(dataset.getSamples().size(), samples.size());
    for (Sample sample : dataset.getSamples()) {
      assertTrue(samples.contains(sample));
    }
  }

  @Test
  public void samples_ColumnsValueProvider() {
    when(sampleService.files(any())).then(new Answer<List<File>>() {
      int count = 0;

      @Override
      public List<File> answer(InvocationOnMock invocation) {
        List<File> files = new ArrayList<>();
        IntStream.range(0, count++).forEach(i -> files.add(new File(i + ".txt")));
        return files;
      }
    });
    dialog.samples.setItems(samples);
    for (int i = 0; i < samples.size(); i++) {
      Sample sample = samples.get(i);
      assertEquals(sample.getName(),
          test(dialog.samples).getCellText(i, dialog.samples.getColumns().indexOf(dialog.name)));
      assertEquals(String.valueOf(samples.indexOf(sample)), test(dialog.samples).getCellText(i,
          dialog.samples.getColumns().indexOf(dialog.fileCount)));
      verify(sampleService, atLeastOnce()).files(sample);
    }
  }

  @Test
  public void samples_NameColumnComparator() {
    Comparator<Sample> comparator = dialog.name.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(sampleName("éê"), sampleName("ee")));
    assertTrue(comparator.compare(sampleName("a"), sampleName("e")) < 0);
    assertTrue(comparator.compare(sampleName("a"), sampleName("é")) < 0);
    assertTrue(comparator.compare(sampleName("e"), sampleName("a")) > 0);
    assertTrue(comparator.compare(sampleName("é"), sampleName("a")) > 0);
  }

  @Test
  public void samples_ViewFiles() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Sample sample = dataset.getSamples().get(0);
    doubleClickItem(dialog.samples, sample);
    SampleFilesDialog sampleFilesDialog = $(SampleFilesDialog.class).first();
    assertEquals(sample.getId(), sampleFilesDialog.getSampleId());
  }

  @Test
  public void samples_ViewFiles_RefreshOnClose() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Sample sample = dataset.getSamples().get(0);
    doubleClickItem(dialog.samples, sample);
    SampleFilesDialog sampleFilesDialog = $(SampleFilesDialog.class).first();
    assertEquals(sample.getId(), sampleFilesDialog.getSampleId());
    @SuppressWarnings("unchecked") ListDataProvider<Sample> sampleDataProvider = mock(
        ListDataProvider.class);
    dialog.samples.setItems(sampleDataProvider);
    sampleFilesDialog.close();
    verify(dialog.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void refresh() {
    Dataset dataset = repository.findById(dialog.getDatasetId()).orElseThrow();
    files.add(new File("new_file_refresh.txt"));
    when(service.files(any())).thenReturn(
        files.stream().map(File::toPath).collect(Collectors.toList()));
    test(dialog.refresh).click();
    verify(service, times(2)).files(dataset);
    List<EditableFile> files = dialog.files.getListDataView().getItems().toList();
    assertEquals(this.files.size(), files.size());
    for (File file : this.files) {
      assertTrue(files.stream().anyMatch(ef -> ef.getFile().equals(file)));
    }
  }

  @Test
  public void upload() {
    assertEquals(MAXIMUM_SMALL_FILES_COUNT, dialog.upload.getMaxFiles());
    assertEquals(MAXIMUM_SMALL_FILES_SIZE, dialog.upload.getMaxFileSize());
  }

  @Test
  public void upload_File() throws Exception {
    Dataset dataset = repository.findById(2L).orElseThrow();
    String filename = "test_file.txt";
    String mimeType = "text/plain";
    final Path tempFile = temporaryFolder.resolve("lanaseq-test-");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Mockito.doAnswer(i -> {
      Collection<Path> files = i.getArgument(1);
      Path file = files.stream().findFirst().orElseThrow();
      Files.copy(file, tempFile, StandardCopyOption.REPLACE_EXISTING);
      assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
      return null;
    }).when(service).saveFiles(any(), any());
    SecurityContextHolder.getContext().setAuthentication(null);

    test(dialog.upload).upload(filename, mimeType, fileContent);

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    assertEquals(1, filesCaptor.getValue().size());
    Path file = filesCaptor.getValue().stream().findFirst().orElseThrow();
    assertEquals(filename, file.getFileName().toString());
    assertArrayEquals(fileContent, Files.readAllBytes(tempFile));
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILES_SUCCESS, filename),
        test(notification).getText());
    assertFalse(Files.exists(file));
    assertFalse(Files.exists(file.getParent()));
  }

  @Test
  public void upload_Error() {
    Dataset dataset = repository.findById(2L).orElseThrow();
    String filename = "test_file.txt";
    String mimeType = "text/plain";
    doThrow(new IllegalStateException("test")).when(service).saveFiles(any(), any());
    SecurityContextHolder.getContext().setAuthentication(null);

    test(dialog.upload).upload(filename, mimeType, fileContent);

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    assertEquals(1, filesCaptor.getValue().size());
    Path file = filesCaptor.getValue().stream().findFirst().orElseThrow();
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILES_IOEXCEPTION, filename),
        test(notification).getText());
    assertFalse(Files.exists(file));
    assertFalse(Files.exists(file.getParent()));
  }

  @Test
  public void getDatasetId() {
    assertEquals(2L, dialog.getDatasetId());
  }

  @Test
  public void setDatasetId_Dataset() {
    Dataset dataset = repository.findById(6L).orElseThrow();

    dialog.setDatasetId(6L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    for (int i = 0; i < labels.size(); i++) {
      assertEquals(labels.get(i), ((Span) dialog.folders.getComponentAt(i)).getText());
    }
    verify(service).files(dataset);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertTrue(dialog.delete.isVisible());
    assertFalse(dialog.filenameEdit.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.addLargeFiles.isVisible());
  }

  @Test
  public void setDatasetId_CannotWrite() {
    Dataset dataset = repository.findById(1L).orElseThrow();

    dialog.setDatasetId(1L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    for (int i = 0; i < labels.size(); i++) {
      assertEquals(labels.get(i), ((Span) dialog.folders.getComponentAt(i)).getText());
    }
    verify(service).files(dataset);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
    assertFalse(dialog.upload.isVisible());
    assertFalse(dialog.addLargeFiles.isVisible());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void setDatasetId_NotEditable() {
    Dataset dataset = repository.findById(5L).orElseThrow();

    dialog.setDatasetId(5L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE, labels.size()),
        dialog.message.getText());
    assertEquals(labels.size(), dialog.folders.getComponentCount());
    for (int i = 0; i < labels.size(); i++) {
      assertEquals(labels.get(i), ((Span) dialog.folders.getComponentAt(i)).getText());
    }
    verify(service).files(dataset);
    List<EditableFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (EditableFile file : files) {
      assertTrue(this.files.contains(file.getFile()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
    assertFalse(dialog.upload.isVisible());
    assertFalse(dialog.addLargeFiles.isVisible());
  }

  @Test
  public void setDatasetId_0() {
    assertThrows(NoSuchElementException.class, () -> dialog.setDatasetId(0));
  }

  @Test
  public void addLargeFiles() {
    Dataset dataset = repository.findById(2L).orElseThrow();

    dialog.addLargeFiles.click();

    AddDatasetFilesDialog largeFilesDialog = $(AddDatasetFilesDialog.class).first();
    assertEquals(dataset.getId(), largeFilesDialog.getDatasetId());
    largeFilesDialog.fireSavedEvent();
    verify(service, atLeast(2)).files(dataset);
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
