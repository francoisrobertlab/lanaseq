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
