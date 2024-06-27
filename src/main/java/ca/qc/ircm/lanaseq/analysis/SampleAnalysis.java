package ca.qc.ircm.lanaseq.analysis;

import ca.qc.ircm.lanaseq.sample.Sample;
import java.nio.file.Path;
import java.util.List;

/**
 * Sample analysis metadata.
 */
public class SampleAnalysis {
  /**
   * Sample.
   */
  public Sample sample;
  /**
   * True if sample FASTQ files are paired.
   */
  public boolean paired;
  /**
   * First FASTQ file.
   */
  public Path fastq1;
  /**
   * Second FASTQ file, if paired.
   */
  public Path fastq2;
  /**
   * Bam files.
   */
  public List<Path> bams;
}
