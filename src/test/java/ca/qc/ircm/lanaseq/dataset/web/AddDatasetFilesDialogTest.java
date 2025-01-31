package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REFRESH;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.OVERWRITE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.OVERWRITE_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SIZE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SIZE_VALUE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.testbench.unit.MetaKeys;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link AddDatasetFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddDatasetFilesDialogTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(AddDatasetFilesDialog.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @TempDir
  Path temporaryFolder;
  private AddDatasetFilesDialog dialog;
  @MockitoBean
  private DatasetService service;
  @MockitoBean
  private SampleService sampleService;
  @MockitoBean
  private AppConfiguration configuration;
  @Mock
  private Dialog.OpenedChangeEvent openedChangeEvent;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<Dialog.OpenedChangeEvent>> openedChangeListenerCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<AddDatasetFilesDialog>> savedListener;
  @Captor
  private ArgumentCaptor<Collection<Path>> filesCaptor;
  @Autowired
  private DatasetRepository repository;
  private Locale locale = Locale.ENGLISH;
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
    when(service.get(anyLong())).then(i -> repository.findById(i.getArgument(0)));
    files.add(temporaryFolder.resolve("dataset_R1.fastq").toFile());
    files.add(temporaryFolder.resolve("dataset_R2.fastq").toFile());
    files.add(temporaryFolder.resolve("dataset.bw").toFile());
    files.add(temporaryFolder.resolve("dataset.png").toFile());
    for (File file : files) {
      writeFile(file.toPath(), random.nextInt(10) * 1024 ^ 2);
    }
    folder = temporaryFolder.resolve("dataset");
    @SuppressWarnings("unchecked")
    AppConfiguration.NetworkDrive<DataWithFiles> homeFolder =
        mock(AppConfiguration.NetworkDrive.class);
    when(configuration.getHome()).thenReturn(homeFolder);
    when(configuration.getHome().folder(any(Dataset.class)))
        .thenReturn(temporaryFolder.resolve("home"));
    @SuppressWarnings("unchecked")
    AppConfiguration.NetworkDrive<DataWithFiles> uploadFolder =
        mock(AppConfiguration.NetworkDrive.class);
    when(configuration.getUpload()).thenReturn(uploadFolder);
    when(configuration.getUpload().folder(any(Dataset.class))).thenReturn(folder);
    when(configuration.getUpload().label(any(Dataset.class), anyBoolean())).then(i -> {
      Dataset dataset = i.getArgument(0);
      boolean linux = i.getArgument(1);
      return (linux ? uploadLabelLinux : uploadLabelWindows) + "/" + dataset.getName();
    });
    when(service.uploadFiles(any())).thenReturn(
        files.stream().map(file -> folder.resolve(file.toPath())).collect(Collectors.toList()));
    when(service.files(any()))
        .thenReturn(files.subList(0, 2).stream().map(File::toPath).collect(Collectors.toList()));
    UI.getCurrent().setLocale(locale);
    DatasetsView view = navigate(DatasetsView.class);
    view.datasets.setItems(repository.findAll());
    test(view.datasets).clickRow(1, new MetaKeys().ctrl());
    DatasetFilesDialog filesDialog = $(DatasetFilesDialog.class).first();
    filesDialog.addLargeFiles.click();
    dialog = $(AddDatasetFilesDialog.class).first();
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
    assertEquals(id(REFRESH), dialog.refresh.getId().orElse(""));
    validateIcon(VaadinIcon.REFRESH.create(), dialog.refresh.getIcon());
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void labels() {
    Dataset dataset = repository.findById(dialog.getDatasetId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE,
        configuration.getUpload().label(dataset, true)), dialog.message.getText());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SIZE),
        headerRow.getCell(dialog.size).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + OVERWRITE),
        headerRow.getCell(dialog.overwrite).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + REFRESH), dialog.refresh.getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + SAVE), dialog.save.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    Dataset dataset = repository.findById(dialog.getDatasetId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE,
        configuration.getUpload().label(dataset, true)), dialog.message.getText());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SIZE),
        headerRow.getCell(dialog.size).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + OVERWRITE),
        headerRow.getCell(dialog.overwrite).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + REFRESH), dialog.refresh.getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + SAVE), dialog.save.getText());
  }

  @Test
  public void files() {
    assertEquals(3, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(SIZE));
    assertNotNull(dialog.files.getColumnByKey(OVERWRITE));
  }

  @Test
  public void files_ColumnsValueProvider() {
    when(service.uploadFiles(any())).thenReturn(files.stream().map(File::toPath).toList());
    when(service.files(any())).thenReturn(List.of(files.get(0).toPath()));
    test(dialog.refresh).click();
    for (int i = 0; i < files.size(); i++) {
      File file = files.get(i);
      Span span = (Span) test(dialog.files).getCellComponent(i, dialog.filename.getKey());
      assertEquals(file.getName(), span.getText());
      assertEquals(i == 0, span.hasClassName(ERROR_TEXT));
      assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SIZE_VALUE, file.length() / 1048576),
          test(dialog.files).getCellText(i, dialog.files.getColumns().indexOf(dialog.size)));
      assertInstanceOf(Checkbox.class,
          test(dialog.files).getCellComponent(i, dialog.overwrite.getKey()));
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

  private Path uploadFolder(Dataset dataset) {
    return configuration.getUpload().folder(dataset);
  }

  @Test
  public void createUploadFolder() {
    Dataset dataset = repository.findById(dialog.getDatasetId()).orElseThrow();
    assertTrue(Files.exists(uploadFolder(dataset)));
  }

  @Test
  public void keepUploadFolderOnClose() throws Throwable {
    Dataset dataset = repository.findById(dialog.getDatasetId()).orElseThrow();
    assertTrue(Files.exists(uploadFolder(dataset)));
    Files.createFile(uploadFolder(dataset).resolve("test.txt"));
    dialog.close();
    assertTrue(Files.exists(uploadFolder(dataset)));
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void message_Windows() {
    Dataset dataset = repository.findById(1L).orElseThrow();
    dialog.setDatasetId(1L);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE,
        configuration.getUpload().label(dataset, false)), dialog.message.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void message_Linux() {
    Dataset dataset = repository.findById(1L).orElseThrow();
    dialog.setDatasetId(1L);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE,
        configuration.getUpload().label(dataset, true)), dialog.message.getText());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void message_Mac() {
    Dataset dataset = repository.findById(1L).orElseThrow();
    dialog.setDatasetId(1L);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + MESSAGE,
        configuration.getUpload().label(dataset, true)), dialog.message.getText());
  }

  @Test
  public void getDatasetId() {
    assertEquals(2L, dialog.getDatasetId());
  }

  @Test
  public void setDatasetId() {
    Dataset dataset = repository.findById(1L).orElseThrow();

    dialog.setDatasetId(1L);

    verify(service, atLeastOnce()).uploadFiles(dataset);
    List<File> files = items(dialog.files);
    assertEquals(4, files.size());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()),
        dialog.getHeaderTitle());
  }

  @Test
  public void setDatasetId_0() {
    assertThrows(NoSuchElementException.class, () -> dialog.setDatasetId(0));
  }

  @Test
  public void refresh() {
    when(service.uploadFiles(any()))
        .thenReturn(files.subList(0, 2).stream().map(file -> folder.resolve(file.toPath()))
            .collect(Collectors.toList()))
        .thenReturn(
            files.stream().map(file -> folder.resolve(file.toPath())).collect(Collectors.toList()));
    Dataset dataset = repository.findById(dialog.getDatasetId()).orElseThrow();

    test(dialog.refresh).click();

    verify(service, times(2)).uploadFiles(dataset);
    List<File> files = items(dialog.files);
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(1).toPath()).toFile()));

    test(dialog.refresh).click();

    verify(service, times(3)).uploadFiles(dataset);
    files = items(dialog.files);
    assertEquals(4, files.size());
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(0).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(1).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(2).toPath()).toFile()));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(3).toPath()).toFile()));
  }

  @Test
  public void exists_False() {
    dialog.setDatasetId(1L);
    assertFalse(dialog.exists(files.get(2)));
  }

  @Test
  public void exists_True() {
    dialog.setDatasetId(1L);
    assertTrue(dialog.exists(files.get(1)));
  }

  @Test
  public void save_OverwriteNotAllowed() {
    dialog.addSavedListener(savedListener);

    dialog.save();

    assertTrue(dialog.error.isVisible());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + OVERWRITE_ERROR), dialog.error.getText());
    verify(service, never()).saveFiles(any(), any());
    assertFalse($(Notification.class).exists());
    verify(savedListener, never()).onComponentEvent(any());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void save_OverwriteAllowed() {
    dialog.addSavedListener(savedListener);
    dialog.files.getListDataView().getItems().forEach(f -> dialog.overwrite(f));
    Dataset dataset = repository.findById(dialog.getDatasetId()).orElseThrow();
    dialog.overwriteAll.setValue(true);

    dialog.save();

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(4, files.size());
    for (Path file : files) {
      assertTrue(files.contains(uploadFolder(dataset).resolve(file)));
    }
    assertFalse(dialog.error.isVisible());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, 4, dataset.getName()),
        test(notification).getText());
    verify(savedListener).onComponentEvent(any());
    assertFalse(dialog.isOpened());
  }

  @Test
  public void save() {
    when(service.files(any())).thenReturn(new ArrayList<>());
    when(service.uploadFiles(any())).thenReturn(files.subList(0, 2).stream()
        .map(file -> folder.resolve(file.toPath())).collect(Collectors.toList()));
    dialog.addSavedListener(savedListener);
    Dataset dataset = repository.findById(1L).orElseThrow();
    dialog.setDatasetId(1L);

    dialog.save();

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(2, files.size());
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(0).toPath())));
    assertTrue(files.contains(uploadFolder(dataset).resolve(this.files.get(1).toPath())));
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, 2, dataset.getName()),
        test(notification).getText());
    verify(savedListener).onComponentEvent(any());
    assertFalse(dialog.isOpened());
  }

  @Test
  public void save_NoFiles() {
    when(service.uploadFiles(any())).thenReturn(new ArrayList<>());
    dialog.addSavedListener(savedListener);
    Dataset dataset = repository.findById(1L).orElseThrow();
    dialog.setDatasetId(1L);

    dialog.save();

    verify(service).saveFiles(eq(dataset), filesCaptor.capture());
    Collection<Path> files = filesCaptor.getValue();
    assertEquals(0, files.size());
    assertTrue(Files.exists(uploadFolder(dataset)));
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, 0, dataset.getName()),
        test(notification).getText());
    verify(savedListener).onComponentEvent(any());
    assertFalse(dialog.isOpened());
  }
}
