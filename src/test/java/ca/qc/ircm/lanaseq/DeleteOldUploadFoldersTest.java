package ca.qc.ircm.lanaseq;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link DeleteOldUploadFolders}.
 */
@NonTransactionalTestAnnotations
public class DeleteOldUploadFoldersTest {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private DeleteOldUploadFolders task;
  @MockitoBean
  private AppConfiguration configuration;
  @Mock
  private AppConfiguration.NetworkDrive<DataWithFiles> upload;

  @BeforeEach
  public void beforeTest() {
    when(configuration.getUpload()).thenReturn(upload);
    when(configuration.getUpload().getFolder()).thenReturn(temporaryFolder);
    when(configuration.getUploadDeleteAge()).thenReturn(Duration.ofHours(24));
  }

  @Test
  public void deleteOldUploadFolders_OldEmptyFolder() throws Throwable {
    Path folder = Files.createDirectory(temporaryFolder.resolve("test"));
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldUploadFolders();
    assertFalse(Files.exists(folder));
  }

  @Test
  public void deleteOldUploadFolders_OldFolderWithOldFile() throws Throwable {
    Path folder = Files.createDirectory(temporaryFolder.resolve("test"));
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldUploadFolders();
    assertFalse(Files.exists(folder));
  }

  @Test
  public void deleteOldUploadFolders_OldFolderWithRecentFile() throws Throwable {
    Path folder = Files.createDirectory(temporaryFolder.resolve("test"));
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldUploadFolders();
    assertFalse(Files.exists(folder));
  }

  @Test
  public void deleteOldUploadFolders_OldFile() throws Throwable {
    Path file = Files.createFile(temporaryFolder.resolve("test.txt"));
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(file));
    task.deleteOldUploadFolders();
    assertFalse(Files.exists(file));
  }

  @Test
  public void deleteOldUploadFolders_NewEmptyFolder() throws Throwable {
    Path folder = Files.createDirectory(temporaryFolder.resolve("test"));
    assertTrue(Files.exists(folder));
    task.deleteOldUploadFolders();
    assertTrue(Files.exists(folder));
  }

  @Test
  public void deleteOldUploadFolders_NewEmptyFolderWithOldFile() throws Throwable {
    Path folder = Files.createDirectory(temporaryFolder.resolve("test"));
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldUploadFolders();
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(file));
  }

  @Test
  public void deleteOldUploadFolders_NewEmptyFolderWithNewFile() throws Throwable {
    Path folder = Files.createDirectory(temporaryFolder.resolve("test"));
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    assertTrue(Files.exists(folder));
    task.deleteOldUploadFolders();
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(file));
  }

  @Test
  public void deleteOldUploadFolders_NewFile() throws Throwable {
    Path file = Files.createFile(temporaryFolder.resolve("test.txt"));
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    assertTrue(Files.exists(file));
    task.deleteOldUploadFolders();
    assertTrue(Files.exists(file));
  }

  @Test
  public void deleteOldUploadFolders_FutureEmptyFolder() throws Throwable {
    Path folder = Files.createDirectory(temporaryFolder.resolve("test"));
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().plus(1, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldUploadFolders();
    assertTrue(Files.exists(folder));
  }
}
