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

package ca.qc.ircm.lanaseq.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class DeleteOldAnalysisFoldersTest {
  @Autowired
  private DeleteOldAnalysisFolders task;
  @MockBean
  private AppConfiguration configuration;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void beforeTest() {
    when(configuration.getAnalysis()).thenReturn(temporaryFolder.getRoot().toPath());
    when(configuration.getAnalysisDeleteAge()).thenReturn(Duration.ofHours(24));
  }

  @Test
  public void deleteOldAnalysisFolders_OldEmptyFolder() throws Throwable {
    Path folder = temporaryFolder.newFolder("test").toPath();
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldAnalysisFolders();
    assertFalse(Files.exists(folder));
  }

  @Test
  public void deleteOldAnalysisFolders_OldFolderWithOldFile() throws Throwable {
    Path folder = temporaryFolder.newFolder("test").toPath();
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldAnalysisFolders();
    assertFalse(Files.exists(folder));
  }

  @Test
  public void deleteOldAnalysisFolders_OldFolderWithRecentFile() throws Throwable {
    Path folder = temporaryFolder.newFolder("test").toPath();
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldAnalysisFolders();
    assertFalse(Files.exists(folder));
  }

  @Test
  public void deleteOldAnalysisFolders_OldFile() throws Throwable {
    Path file = temporaryFolder.newFile("test.txt").toPath();
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(file));
    task.deleteOldAnalysisFolders();
    assertFalse(Files.exists(file));
  }

  @Test
  public void deleteOldAnalysisFolders_NewEmptyFolder() throws Throwable {
    Path folder = temporaryFolder.newFolder("test").toPath();
    assertTrue(Files.exists(folder));
    task.deleteOldAnalysisFolders();
    assertTrue(Files.exists(folder));
  }

  @Test
  public void deleteOldAnalysisFolders_NewEmptyFolderWithOldFile() throws Throwable {
    Path folder = temporaryFolder.newFolder("test").toPath();
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldAnalysisFolders();
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(file));
  }

  @Test
  public void deleteOldAnalysisFolders_NewEmptyFolderWithNewFile() throws Throwable {
    Path folder = temporaryFolder.newFolder("test").toPath();
    Path file = folder.resolve("test.txt");
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    assertTrue(Files.exists(folder));
    task.deleteOldAnalysisFolders();
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(file));
  }

  @Test
  public void deleteOldAnalysisFolders_NewFile() throws Throwable {
    Path file = temporaryFolder.newFile("test.txt").toPath();
    Files.write(file, Stream.of("test").collect(Collectors.toList()), StandardOpenOption.CREATE);
    assertTrue(Files.exists(file));
    task.deleteOldAnalysisFolders();
    assertTrue(Files.exists(file));
  }

  @Test
  public void deleteOldAnalysisFolders_FutureEmptyFolder() throws Throwable {
    Path folder = temporaryFolder.newFolder("test").toPath();
    Files.setLastModifiedTime(folder, FileTime.from(Instant.now().plus(1, ChronoUnit.HOURS)));
    assertTrue(Files.exists(folder));
    task.deleteOldAnalysisFolders();
    assertTrue(Files.exists(folder));
  }
}
