package ca.qc.ircm.lanaseq.dataset;

/**
 * Entity has files associated with it on disk.
 */
public interface HasFiles {
  /**
   * Returns filename prefix for entity.
   * 
   * @return filename prefix for entity
   */
  String getFilename();
}
