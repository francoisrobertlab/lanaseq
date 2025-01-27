package ca.qc.ircm.lanaseq.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renames files and folders.
 */
public class Renamer {
  private static final Logger logger = LoggerFactory.getLogger(Renamer.class);

  /**
   * Renames oldFolder to folder and creates parent directories if necessary.
   *
   * @param oldFolder
   *          old folder
   * @param newFolder
   *          new folder
   */
  public static void moveFolder(Path oldFolder, Path newFolder) {
    if (Files.exists(oldFolder) && !oldFolder.equals(newFolder)) {
      try {
        logger.debug("moving folder {} to {}", oldFolder, newFolder);
        Path parent = newFolder.getParent();
        if (parent != null) {
          Files.createDirectories(parent);
        }
        Files.move(oldFolder, newFolder);
      } catch (IOException e) {
        throw new IllegalStateException("could not move folder " + oldFolder + " to " + newFolder,
            e);
      }
    }
  }

  /**
   * Renames all files in folder that contains the old name to the new name. <br>
   * Also fixes the filenames present in <code>.md5</code> files.
   *
   * @param oldName
   *          old name
   * @param newName
   *          new name
   * @param folder
   *          folder
   */
  public static void renameFiles(String oldName, String newName, Path folder) {
    List<Path> files;
    try (Stream<Path> stream = Files.list(folder)) {
      files = stream.toList();
    } catch (IOException e) {
      files = new ArrayList<>();
    }
    for (Path file : files) {
      String filename = file.getFileName().toString();
      if (filename.endsWith(".md5")) {
        try {
          List<String> lines = Files.readAllLines(file);
          List<String> newlines = new ArrayList<>();
          for (String line : lines) {
            if (line.contains(oldName)) {
              line = line.replaceFirst(Pattern.quote(oldName), newName);
            }
            newlines.add(line);
          }
          Files.write(file, newlines);
        } catch (IOException e) {
          throw new IllegalStateException("could not rename files in md5 file " + file, e);
        }
      }
      if (filename.contains(oldName)) {
        String newFilename = filename.replaceFirst(Pattern.quote(oldName), newName);
        Path newFile = file.resolveSibling(newFilename);
        try {
          logger.debug("renaming file {} to {}", file, newFile);
          Files.move(file, newFile);
        } catch (IOException e) {
          throw new IllegalStateException("could not move file " + file + " to " + newFile, e);
        }
      }
    }
  }
}
