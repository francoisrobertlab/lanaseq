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

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Services for analysis.
 */
@Service
public class AnalysisService {
  private static final String FASTQ = "\\.fastq(?:\\.gz)?";
  private static final String FASTQ_PATTERN = ".*" + FASTQ;
  private static final String FASTQ1_PATTERN = "(?:.*_)?R1" + FASTQ;
  private static final String FASTQ2_PATTERN = "(?:.*_)?R2" + FASTQ;
  private static final String BAM_PATTERN = "(?:.*)\\.bam";
  private SampleService sampleService;
  private AppConfiguration configuration;

  @Autowired
  protected AnalysisService(SampleService sampleService, AppConfiguration configuration) {
    this.sampleService = sampleService;
    this.configuration = configuration;
  }

  /**
   * Validates if dataset can be analyzed.
   *
   * @param dataset
   *          dataset
   * @param locale
   *          locale for error messages
   * @param errorHandler
   *          handles error messages
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public void validate(Dataset dataset, Locale locale, Consumer<String> errorHandler) {
    AppResources resources = new AppResources(AnalysisService.class, locale);
    metadata(dataset, resources, errorHandler);
  }

  private Optional<DatasetAnalysis> metadata(Dataset dataset, AppResources resources,
      Consumer<String> errorHandler) {
    List<Optional<SampleAnalysis>> samples = dataset.getSamples().stream()
        .map(sample -> metadata(sample, resources, errorHandler)).collect(Collectors.toList());
    if (!samples.stream().filter(sample -> !sample.isPresent()).findAny().isPresent()) {
      boolean paired =
          samples.stream().findFirst().map(sample -> sample.get().paired).orElse(false);
      if (samples.stream().filter(sample -> sample.get().paired != paired).findAny().isPresent()) {
        errorHandler.accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
        return Optional.empty();
      } else {
        DatasetAnalysis analysis = new DatasetAnalysis();
        analysis.dataset = dataset;
        analysis.samples =
            samples.stream().map(sample -> sample.get()).collect(Collectors.toList());
        return Optional.of(analysis);
      }
    } else {
      return Optional.empty();
    }
  }

  private Optional<SampleAnalysis> metadata(Sample sample, AppResources resources,
      Consumer<String> errorHandler) {
    Pattern fastqPattern = Pattern.compile(FASTQ_PATTERN);
    Pattern fastq1Pattern = Pattern.compile(FASTQ1_PATTERN);
    Pattern fastq2Pattern = Pattern.compile(FASTQ2_PATTERN);
    Pattern bamPattern = Pattern.compile(BAM_PATTERN);
    List<Path> files = sampleService.files(sample);
    List<Path> fastqs =
        files.stream().filter(file -> fastqPattern.matcher(file.toString()).matches())
            .collect(Collectors.toList());
    Path fastq1 = fastqs.stream()
        .filter(file -> fastq1Pattern.matcher(file.getFileName().toString()).matches()).findAny()
        .orElse(fastqs.isEmpty() ? null : fastqs.get(0));
    Path fastq2 = fastqs.stream()
        .filter(file -> fastq2Pattern.matcher(file.getFileName().toString()).matches()).findAny()
        .orElse(null);
    List<Path> bams = files.stream().filter(file -> bamPattern.matcher(file.toString()).matches())
        .collect(Collectors.toList());
    if (fastq1 == null) {
      String message = resources.message("sample.noFastq", sample.getName());
      errorHandler.accept(message);
      return Optional.empty();
    } else {
      SampleAnalysis analysis = new SampleAnalysis();
      analysis.sample = sample;
      analysis.paired = fastq2 != null && !fastq2.equals(fastq1);
      analysis.fastq1 = fastq1;
      analysis.fastq2 = fastq2;
      analysis.bams = bams;
      return Optional.of(analysis);
    }
  }

  /**
   * Copy dataset resources used for analysis to a new folder.
   *
   * @param dataset
   *          dataset
   * @return folder the folder containing analysis files
   * @throws IOException
   *           could not copy analysis files to folder
   * @throws IllegalArgumentException
   *           dataset analysis validation failed
   */
  @PreAuthorize("hasPermission(#dataset, 'read')")
  public Path copyResources(Dataset dataset) throws IOException {
    DatasetAnalysis analysis =
        metadata(dataset, new AppResources(AnalysisService.class, Locale.getDefault()), message -> {
        }).orElse(null);
    if (analysis == null) {
      throw new IllegalArgumentException(
          "dataset " + dataset + " is missing files required for analysis");
    }
    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.analysis(analysis.dataset);
    Files.createDirectories(folder);
    for (SampleAnalysis sample : analysis.samples) {
      Path fastq1 = folder.resolve(sample.sample.getName() + "_R1.fastq"
          + (sample.fastq1.toString().endsWith(".gz") ? ".gz" : ""));
      copy(sample.fastq1, fastq1, symlinks);
      if (sample.paired) {
        Path fastq2 = folder.resolve(sample.sample.getName() + "_R2.fastq"
            + (sample.fastq2.toString().endsWith(".gz") ? ".gz" : ""));
        copy(sample.fastq2, fastq2, symlinks);
      }
      for (Path bam : sample.bams) {
        copy(bam, folder.resolve(bam.getFileName()), symlinks);
      }
    }
    Path samples = folder.resolve("samples.txt");
    List<String> samplesLines = new ArrayList<>();
    samplesLines.add("#sample");
    for (SampleAnalysis sample : analysis.samples) {
      samplesLines.add(sample.sample.getName());
    }
    Files.write(samples, samplesLines, StandardOpenOption.CREATE);
    Path datasetFile = folder.resolve("dataset.txt");
    List<String> datasetLines = new ArrayList<>();
    datasetLines.add("#merge\tsamples");
    datasetLines.add(analysis.dataset.getName() + "\t" + analysis.samples.stream()
        .map(sample -> sample.sample.getName()).collect(Collectors.joining("\t")));
    Files.write(datasetFile, datasetLines, StandardOpenOption.CREATE);
    return folder;
  }

  private void copy(Path source, Path destination, boolean symlink) throws IOException {
    if (symlink) {
      Files.createSymbolicLink(destination, source);
    } else {
      Files.copy(source, destination);
    }
  }
}
