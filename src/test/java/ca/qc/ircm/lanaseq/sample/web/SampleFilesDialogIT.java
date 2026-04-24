package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.AppConfiguration.DELETED_FILENAME;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES_SAVE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.editItem;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFile;
import ca.qc.ircm.lanaseq.sample.SamplePublicFileRepository;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.EditableFile;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.editor.EditorImpl;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SampleFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleFilesDialogIT extends SpringBrowserlessTest {

  private static final String MESSAGE_PREFIX = messagePrefix(SampleFilesDialog.class);
  @Autowired
  private SampleRepository repository;
  @Autowired
  private SamplePublicFileRepository samplePublicFileRepository;
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

  @Test
  public void files() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Path archive = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(archive);
    Path file2 = archive.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        file2);
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();
    List<Span> labels = test(dialog.folders).find(Span.class).all();
    assertEquals(2, labels.size());
    assertEquals(configuration.getHome().label(sample, !SystemUtils.IS_OS_WINDOWS),
        labels.get(0).getText());
    assertEquals(configuration.getArchives().getFirst().label(sample, !SystemUtils.IS_OS_WINDOWS),
        labels.get(1).getText());
    assertEquals(2, test(dialog.files).size());
    assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "title",
            String.class));
    assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "filename",
            String.class));
    assertEquals(file2.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(1, dialog.filename.getKey(), "title",
            String.class));
    assertEquals(file2.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(1, dialog.filename.getKey(), "filename",
            String.class));
  }

  @Test
  public void rename() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();

    EditableFile editableFile = dialog.files.getListDataView().getItem(0);
    EditorImpl<EditableFile> editor = (EditorImpl<EditableFile>) dialog.files.getEditor();
    editItem(editor, editableFile);
    dialog.filenameEdit.setValue(sample.getName() + "_R1.fastq");
    editor.closeEditor();

    assertTrue(Files.exists(file.resolveSibling(sample.getName() + "_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(file.resolveSibling(sample.getName() + "_R1.fastq")));
    assertFalse(Files.exists(file));
  }

  @Test
  public void publicFile() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();
    assertFalse(
        ((Checkbox) test(dialog.files).getCellComponent(0, dialog.publicFile.getKey())).getValue());

    test((Checkbox) test(dialog.files).getCellComponent(0, dialog.publicFile.getKey())).click();

    assertTrue(
        ((Checkbox) test(dialog.files).getCellComponent(0, dialog.publicFile.getKey())).getValue());
    Optional<SamplePublicFile> optionalSamplePublicFile = samplePublicFileRepository.findBySampleAndPath(
        sample, "R1.fastq");
    assertTrue(optionalSamplePublicFile.isPresent());
    SamplePublicFile samplePublicFile = optionalSamplePublicFile.orElseThrow();
    assertEquals(LocalDate.now().plus(configuration.getPublicFilePeriod()),
        samplePublicFile.getExpiryDate());
  }

  @Test
  public void delete() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();

    test(test(dialog.files).getCellComponent(0, dialog.delete.getKey())).click();

    assertFalse(Files.exists(file));
    Path deleted = folder.resolve(DELETED_FILENAME);
    List<String> deletedLines = Files.readAllLines(deleted);
    String[] deletedFileColumns = deletedLines.get(deletedLines.size() - 1).split("\t", -1);
    assertEquals(3, deletedFileColumns.length);
    assertEquals("R1.fastq", deletedFileColumns[0]);
    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    assertEquals(modifiedTime, LocalDateTime.from(formatter.parse(deletedFileColumns[1])));
    LocalDateTime deletedTime = LocalDateTime.from(formatter.parse(deletedFileColumns[2]));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(deletedTime));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(deletedTime));
  }

  @Test
  public void refresh() throws Throwable {
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();
    Sample sample = repository.findById(10L).orElseThrow();
    Path home = configuration.getHome().folder(sample);
    Files.createDirectories(home);
    Path file1 = home.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    assertEquals(0, test(dialog.files).size());

    test(dialog.refresh).click();

    assertEquals(1, test(dialog.files).size());
    assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "title",
            String.class));
    assertEquals(file1.getFileName().toString(),
        test(dialog.files).getLitRendererPropertyValue(0, dialog.filename.getKey(), "filename",
            String.class));
  }

  @Test
  public void addLargeFiles() {
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(0);
    test(view.files).click();
    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();

    test(dialog.addLargeFiles).click();

    assertTrue($(AddSampleFilesDialog.class).exists());
  }

  @Test
  public void upload() throws Throwable {
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();
    Sample sample = repository.findById(10L).orElseThrow();

    test(dialog.upload).upload(file1.toFile());

    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + FILES_SAVE,
            new Object[]{file1.getFileName(), sample.getName()}, UI.getCurrent().getLocale()),
        test(notification).getText());
    // Wait for files to finish copying.
    Thread.sleep(1000);
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
    assertEquals(1, jobService.getJobs().size());
  }
}
