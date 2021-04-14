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

package ca.qc.ircm.lanaseq;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@NonTransactionalTestAnnotations
public class DeleteOldUploadFoldersTest {
  @Autowired
  private DeleteOldUploadFolders task;
  @MockBean
  private AppConfiguration configuration;
  @TempDir
  Path temporaryFolder;

  @BeforeEach
  public void beforeTest() {
    when(configuration.getUpload()).thenReturn(temporaryFolder);
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
