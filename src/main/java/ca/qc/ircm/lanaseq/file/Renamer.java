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

package ca.qc.ircm.lanaseq.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    if (oldFolder != null && Files.exists(oldFolder) && !oldFolder.equals(newFolder)) {
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
      files = stream.collect(Collectors.toList());
    } catch (IOException e) {
      files = new ArrayList<>();
    }
    for (Path file : files) {
      String filename = Optional.of(file.getFileName()).map(fn -> fn.toString()).orElse(null);
      if (filename != null) {
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
}
