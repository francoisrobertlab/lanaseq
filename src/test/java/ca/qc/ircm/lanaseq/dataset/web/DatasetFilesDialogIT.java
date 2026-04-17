package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES_SAVE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.editItem;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFile;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFileRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.EditableFile;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.EditorImpl;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link DatasetFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetFilesDialogIT extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(DatasetFilesDialog.class);
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private DatasetPublicFileRepository datasetPublicFileRepository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private JobService jobService;
  private Path file1;

  @BeforeEach
  public void beforeTest() throws Throwable {
    file1 = Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI());
  }

  @AfterEach
  public void afterTest() {
    jobService.getJobs().forEach(jobService::removeJob);
  }

  private <T> int column(Grid<T> grid, Column<T> column) {
    return grid.getColumns().indexOf(column);
  }

  @Test
  public void files() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Path archive = configuration.getArchives().get(0).folder(dataset);
    Files.createDirectories(archive);
    Path file2 = archive.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        file2);
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    List<Span> labels = test(dialog.folders).find(Span.class).all();
    Assertions.assertEquals(2, labels.size());
    Assertions.assertEquals(configuration.getHome().label(dataset, !SystemUtils.IS_OS_WINDOWS),
        labels.get(0).getText());
    Assertions.assertEquals(
        configuration.getArchives().get(0).label(dataset, !SystemUtils.IS_OS_WINDOWS),
        labels.get(1).getText());
    Assertions.assertEquals(2, test(dialog.files).size());
    Assertions.assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "title",
            String.class));
    Assertions.assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "filename",
            String.class));
    Assertions.assertEquals(file2.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(1, dialog.filename.getKey(), "title",
            String.class));
    Assertions.assertEquals(file2.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(1, dialog.filename.getKey(), "filename",
            String.class));
  }

  @Test
  public void rename() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();

    EditableFile editableFile = dialog.files.getListDataView().getItem(0);
    EditorImpl<EditableFile> editor = (EditorImpl<EditableFile>) dialog.files.getEditor();
    editItem(editor, editableFile);
    dialog.filenameEdit.setValue(dataset.getName() + "_R1.fastq");
    editor.closeEditor();

    assertTrue(Files.exists(file.resolveSibling(dataset.getName() + "_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(file.resolveSibling(dataset.getName() + "_R1.fastq")));
    assertFalse(Files.exists(file));
  }

  @Test
  public void publicFile() throws Throwable {
    Dataset dataset = repository.findById(6L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(1);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    assertFalse(
        ((Checkbox) test(dialog.files).getCellComponent(0, dialog.publicFile.getKey())).getValue());

    test((Checkbox) test(dialog.files).getCellComponent(0, dialog.publicFile.getKey())).click();

    assertTrue(
        ((Checkbox) test(dialog.files).getCellComponent(0, dialog.publicFile.getKey())).getValue());
    Optional<DatasetPublicFile> optionalDatasetPublicFile = datasetPublicFileRepository.findByDatasetAndPath(
        dataset, "R1.fastq");
    assertTrue(optionalDatasetPublicFile.isPresent());
    DatasetPublicFile datasetPublicFile = optionalDatasetPublicFile.orElseThrow();
    Assertions.assertEquals(LocalDate.now().plus(configuration.getPublicFilePeriod()),
        datasetPublicFile.getExpiryDate());
  }

  @Test
  public void delete() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();

    test(test(dialog.files).getCellComponent(0, dialog.delete.getKey())).click();

    assertFalse(Files.exists(file));
    Path deleted = folder.resolve(DELETED_FILENAME);
    List<String> deletedLines = Files.readAllLines(deleted);
    String[] deletedFileColumns = deletedLines.get(deletedLines.size() - 1).split("\t", -1);
    Assertions.assertEquals(3, deletedFileColumns.length);
    Assertions.assertEquals("R1.fastq", deletedFileColumns[0]);
    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    Assertions.assertEquals(modifiedTime,
        LocalDateTime.from(formatter.parse(deletedFileColumns[1])));
    LocalDateTime deletedTime = LocalDateTime.from(formatter.parse(deletedFileColumns[2]));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(deletedTime));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(deletedTime));
  }

  @Test
  public void viewFiles_Sample() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    test(dialog.samples).doubleClickRow(0);

    assertTrue($(SampleFilesDialog.class).exists());
  }

  @Test
  public void refresh() throws Throwable {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path home = configuration.getHome().folder(dataset);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Assertions.assertEquals(0, test(dialog.files).size());

    test(dialog.refresh).click();

    Assertions.assertEquals(1, test(dialog.files).size());
    Assertions.assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "title",
            String.class));
    Assertions.assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "filename",
            String.class));
  }

  @Test
  public void addLargeFiles() {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();

    test(dialog.addLargeFiles).click();

    assertTrue($(AddDatasetFilesDialog.class).exists());
  }

  @Test
  public void upload() throws Throwable {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog dialog = $(DatasetFilesDialog.class).first();
    Dataset dataset = repository.findById(2L).orElseThrow();

    test(dialog.upload).upload(file1.toFile());

    Notification notification = $(Notification.class).first();
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FILES_SAVE,
            new Object[]{file1.getFileName(), dataset.getName()}, UI.getCurrent().getLocale()),
        test(notification).getText());
    // Wait for files to finish copying.
    Thread.sleep(1000);
    Path folder = configuration.getHome().folder(dataset);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
    Assertions.assertEquals(1, jobService.getJobs().size());
  }
}
