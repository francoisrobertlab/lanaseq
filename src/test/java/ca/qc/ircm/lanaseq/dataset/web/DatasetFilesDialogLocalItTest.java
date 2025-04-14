package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES_SUCCESS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractLocalBrowserTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.BrowserTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link DatasetFilesDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetFilesDialogLocalItTest extends AbstractLocalBrowserTestCase {

  private static final String MESSAGE_PREFIX = messagePrefix(DatasetFilesDialog.class);
  @Value("${download-home}")
  protected Path downloadHome;
  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;
  @Autowired
  private MessageSource messageSource;
  private Path file1;

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
    setArchive(Files.createDirectory(temporaryFolder.resolve("archives")));
    setUpload(Files.createDirectory(temporaryFolder.resolve("upload")));
    file1 = Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI());
  }

  private void open() {
    openView(VIEW_NAME);
  }

  @BrowserTest
  public void download() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("R1.fastq");
    Files.deleteIfExists(downloaded);
    Dataset dataset = repository.findById(2L).orElseThrow();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();

    dialog.files().download(0).click();

    // Wait for file to download.
    Thread.sleep(2000);
    assertTrue(Files.exists(downloaded));
    try {
      assertArrayEquals(Files.readAllBytes(file), Files.readAllBytes(downloaded));
    } finally {
      Files.delete(downloaded);
    }
  }

  @BrowserTest
  public void upload() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().controlClick(3);
    DatasetFilesDialogElement dialog = view.filesDialog();
    Dataset dataset = repository.findById(2L).orElseThrow();

    dialog.upload().upload(file1.toFile());

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    Assertions.assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + FILES_SUCCESS, new Object[]{file1.getFileName()},
            currentLocale()), notification.getText());
    Path folder = configuration.getHome().folder(dataset);
    assertTrue(Files.exists(folder.resolve(file1.getFileName())));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(file1.getFileName())));
  }
}
