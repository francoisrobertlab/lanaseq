package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SAVE_STARTED;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.jobs.JobService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.testbench.unit.SpringUIUnitTest;
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
 * Integration tests for {@link AddSampleFilesDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AddSampleFilesDialogIT extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(AddSampleFilesDialog.class);
  @Autowired
  private SampleRepository repository;
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

  private void copyFiles(Sample sample)
      throws IOException, URISyntaxException, SecurityException, IllegalArgumentException {
    Path folder = configuration.getUpload().folder(sample);
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
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog filesDialog = $(SampleFilesDialog.class).first();
    test(filesDialog.addLargeFiles).click();
    AddSampleFilesDialog dialog = $(AddSampleFilesDialog.class).first();
    Sample sample = repository.findById(10L).orElseThrow();
    assertEquals(0, test(dialog.files).size());
    copyFiles(sample);

    test(dialog.refresh).click();

    assertEquals(2, test(dialog.files).size());
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        configuration.getUpload().folder(sample).resolve("other.fastq"));
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve("prefix_" + sample.getName() + "_R1"));

    test(dialog.refresh).click();

    assertEquals(4, test(dialog.files).size());
  }

  @Test
  public void save() throws Throwable {
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.files).click();
    SampleFilesDialog filesDialog = $(SampleFilesDialog.class).first();
    test(filesDialog.addLargeFiles).click();
    AddSampleFilesDialog dialog = $(AddSampleFilesDialog.class).first();
    Sample sample = repository.findById(10L).orElseThrow();
    copyFiles(sample);
    String filenameInRoot = "prefix_" + sample.getName() + "_R1";
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        configuration.getUpload().getFolder().resolve(filenameInRoot));

    test(dialog.save).click();

    Notification notification = $(Notification.class).first();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + SAVE_STARTED, new Object[]{3, sample.getName()},
            UI.getCurrent().getLocale()), test(notification).getText());
    // Wait for files to finish copying.
    Thread.sleep(1000);
    Path folder = configuration.getHome().folder(sample);
    Path upload = configuration.getUpload().folder(sample);
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
