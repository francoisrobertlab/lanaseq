package ca.qc.ircm.lanaseq.files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Renamer}.
 */
public class RenamerTest {

  @TempDir
  Path tempDir;
  private final Random random = new Random();

  @Test
  public void moveFolder() throws Throwable {
    Path from = tempDir.resolve("from");
    Files.createDirectory(from);
    Path formFile = from.resolve("test.txt");
    byte[] content = new byte[2048];
    random.nextBytes(content);
    Files.write(formFile, content, StandardOpenOption.CREATE);
    Path to = tempDir.resolve("to");
    Renamer.moveFolder(from, to);
    assertFalse(Files.exists(from));
    assertTrue(Files.exists(to));
    Path toFile = to.resolve("test.txt");
    assertTrue(Files.exists(toFile));
    assertArrayEquals(content, Files.readAllBytes(toFile));
  }

  @Test
  public void moveFolder_DestinationParentDoesNotExists() throws Throwable {
    Path from = tempDir.resolve("from_parent/from");
    Files.createDirectories(from);
    Path formFile = from.resolve("test.txt");
    byte[] content = new byte[2048];
    random.nextBytes(content);
    Files.write(formFile, content, StandardOpenOption.CREATE);
    Path to = tempDir.resolve("to_parent/to");
    Renamer.moveFolder(from, to);
    assertFalse(Files.exists(from));
    assertTrue(Files.exists(to));
    Path toFile = to.resolve("test.txt");
    assertTrue(Files.exists(toFile));
    assertArrayEquals(content, Files.readAllBytes(toFile));
  }

  @Test
  public void moveFolder_FromDoesNotExists() {
    Path from = tempDir.resolve("from");
    Path to = tempDir.resolve("to");
    Renamer.moveFolder(from, to);
    assertFalse(Files.exists(from));
    assertFalse(Files.exists(to));
  }

  @Test
  public void moveFolder_DestinationSameAsSource() throws Throwable {
    Path from = tempDir.resolve("from");
    Files.createDirectory(from);
    Path formFile = from.resolve("test.txt");
    byte[] content = new byte[2048];
    random.nextBytes(content);
    Files.write(formFile, content, StandardOpenOption.CREATE);
    Path to = tempDir.resolve("from");
    Renamer.moveFolder(from, to);
    assertTrue(Files.exists(from));
    assertTrue(Files.exists(to));
    Path toFile = to.resolve("test.txt");
    assertTrue(Files.exists(toFile));
    assertArrayEquals(content, Files.readAllBytes(toFile));
  }

  @Test
  public void renameFiles() throws Throwable {
    String oldName = "old_name";
    String newName = "new_name";
    Path suffix = tempDir.resolve(oldName + "test.txt");
    byte[] suffixContent = new byte[2048];
    random.nextBytes(suffixContent);
    Files.write(suffix, suffixContent, StandardOpenOption.CREATE);
    Path prefix = tempDir.resolve("test_" + oldName);
    byte[] prefixContent = new byte[2048];
    random.nextBytes(prefixContent);
    Files.write(prefix, prefixContent, StandardOpenOption.CREATE);
    Path middle = tempDir.resolve("prefix_" + oldName + "_suffix.txt");
    byte[] middleContent = new byte[2048];
    random.nextBytes(middleContent);
    Files.write(middle, middleContent, StandardOpenOption.CREATE);
    Path md5 = tempDir.resolve("prefix_" + oldName + "_suffix.txt.md5");
    Files.writeString(md5,
        "2d5fb8660262af5a205c485bed4fe6b1  prefix_" + oldName + "_suffix.txt.md5",
        StandardOpenOption.CREATE);
    Path subfolderFile = tempDir.resolve("subfolder").resolve(oldName + "test.txt");
    Files.createDirectory(subfolderFile.getParent());
    byte[] subfolderFileContent = new byte[2048];
    random.nextBytes(subfolderFileContent);
    Files.write(subfolderFile, subfolderFileContent, StandardOpenOption.CREATE);
    Path other = tempDir.resolve("test.txt");
    byte[] otherContent = new byte[2048];
    random.nextBytes(otherContent);
    Files.write(other, otherContent, StandardOpenOption.CREATE);
    Path otherMd5 = tempDir.resolve("test.txt.md5");
    Files.writeString(otherMd5, "93bde352cb4ceb943f7188d307bcd72f  test.txt.md5",
        StandardOpenOption.CREATE);
    Renamer.renameFiles(oldName, newName, tempDir);
    assertFalse(Files.exists(suffix));
    assertFalse(Files.exists(prefix));
    assertFalse(Files.exists(middle));
    assertFalse(Files.exists(md5));
    assertTrue(Files.exists(subfolderFile));
    assertArrayEquals(subfolderFileContent, Files.readAllBytes(subfolderFile));
    assertTrue(Files.exists(other));
    assertArrayEquals(otherContent, Files.readAllBytes(other));
    assertTrue(Files.exists(otherMd5));
    assertEquals("93bde352cb4ceb943f7188d307bcd72f  test.txt.md5" + System.lineSeparator(),
        Files.readString(otherMd5));
    suffix = tempDir.resolve(newName + "test.txt");
    assertTrue(Files.exists(suffix));
    assertArrayEquals(suffixContent, Files.readAllBytes(suffix));
    prefix = tempDir.resolve("test_" + newName);
    assertTrue(Files.exists(prefix));
    assertArrayEquals(prefixContent, Files.readAllBytes(prefix));
    middle = tempDir.resolve("prefix_" + newName + "_suffix.txt");
    assertTrue(Files.exists(middle));
    assertArrayEquals(middleContent, Files.readAllBytes(middle));
    md5 = tempDir.resolve("prefix_" + newName + "_suffix.txt.md5");
    assertTrue(Files.exists(md5));
    assertEquals("2d5fb8660262af5a205c485bed4fe6b1  prefix_" + newName + "_suffix.txt.md5"
        + System.lineSeparator(), Files.readString(md5));
  }
}
