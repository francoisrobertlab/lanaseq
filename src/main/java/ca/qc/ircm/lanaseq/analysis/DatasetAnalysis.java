package ca.qc.ircm.lanaseq.analysis;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import java.util.List;

/**
 * Dataset analysis metadata.
 */
public class DatasetAnalysis {
  /**
   * Dataset.
   */
  public Dataset dataset;
  /**
   * Metadata for dataset's samples.
   */
  public List<SampleAnalysis> samples;
}
