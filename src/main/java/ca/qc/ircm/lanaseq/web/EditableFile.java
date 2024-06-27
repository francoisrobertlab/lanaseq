package ca.qc.ircm.lanaseq.web;

import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.File;
import java.io.Serializable;

/**
 * File with editable name.
 */
@GeneratePropertyNames
public class EditableFile implements Serializable {
  private static final long serialVersionUID = -3555492104705517702L;
  private File file;
  private String filename;

  public EditableFile(File file) {
    this.file = file;
    this.filename = file.getName();
  }

  @Override
  public String toString() {
    return "EditableFile [file=" + file + ", filename=" + filename + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EditableFile other = (EditableFile) obj;
    if (file == null) {
      if (other.file != null) {
        return false;
      }
    } else if (!file.equals(other.file)) {
      return false;
    }
    return true;
  }

  public File getFile() {
    return file;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }
}
