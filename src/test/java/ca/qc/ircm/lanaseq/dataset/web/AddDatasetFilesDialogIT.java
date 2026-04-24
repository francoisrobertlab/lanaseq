package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SAVE_STARTED;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link AddDatasetFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddDatasetFilesDialogIT extends SpringBrowserlessTest {

  private static final String MESSAGE_PREFIX = messagePrefix(AddDatasetFilesDialog.class);
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private JobService jobService;
  private Path file1;
  private Path file2;

  @AfterEach
  public void afterTest() {
    jobService.getJobs().forEach(jobService::removeJob);
  }

  private void copyFiles(Dataset dataset)
      throws IOException, URISyntaxException, SecurityException, IllegalArgumentException {
    Path folder = configuration.getUpload().folder(dataset);
    Files.createDirectories(folder);
    file1 = folder.resolve("R1.fastq");
    file2 = folder.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file1);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        file2);
  }

  @Test
  public void refresh() throws Throwable {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog filesDialog = $(DatasetFilesDialog.class).first();
    test(filesDialog.addLargeFiles).click();
    AddDatasetFilesDialog dialog = $(AddDatasetFilesDialog.class).first();
    Dataset dataset = repository.findById(2L).orElseThrow();
    assertEquals(0, test(dialog.files).size());
    copyFiles(dataset);

    test(dialog.refresh).click();

    assertEquals(2, test(dialog.files).size());
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        configuration.getUpload().folder(dataset).resolve("other.fastq"));
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve("prefix_" + dataset.getName() + "_R1"));

    test(dialog.refresh).click();

    assertEquals(4, test(dialog.files).size());
  }

  @Test
  public void save() throws Throwable {
    DatasetsView view = navigate(DatasetsView.class);
    test(view.datasets).select(3);
    test(view.files).click();
    DatasetFilesDialog filesDialog = $(DatasetFilesDialog.class).first();
    test(filesDialog.addLargeFiles).click();
    AddDatasetFilesDialog dialog = $(AddDatasetFilesDialog.class).first();
    Dataset dataset = repository.findById(2L).orElseThrow();
    copyFiles(dataset);
    String filenameInRoot = "prefix_" + dataset.getName() + "_R1";
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve(filenameInRoot));

    test(dialog.save).click();

    Notification notification = $(Notification.class).first();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVE_STARTED, new Object[]{3, dataset.getName()},
            UI.getCurrent().getLocale()), test(notification).getText());
    // Wait for files to finish copying.
    Thread.sleep(1000);
    Path folder = configuration.getHome().folder(dataset);
    Path upload = configuration.getUpload().folder(dataset);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertTrue(Files.exists(folder.resolve(file2.getFileName())));
    assertFalse(Files.exists(upload.getParent().resolve(filenameInRoot)));
    assertFalse(Files.exists(upload.resolve(file1.getFileName())));
    assertFalse(Files.exists(upload.resolve(file2.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file2.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(filenameInRoot)));
    assertEquals(1, jobService.getJobs().size());
  }
}
