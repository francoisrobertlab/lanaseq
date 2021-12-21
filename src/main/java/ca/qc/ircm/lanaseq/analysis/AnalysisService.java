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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

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
  private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
  private SampleService sampleService;
  private AppConfiguration configuration;

  @Autowired
  protected AnalysisService(SampleService sampleService, AppConfiguration configuration) {
    this.sampleService = sampleService;
    this.configuration = configuration;
  }

  /**
   * Validates if datasets can be analyzed.
   *
   * @param datasets
   *          datasets
   * @param locale
   *          locale for error messages
   * @param errorHandler
   *          handles error messages
   */
  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #datasets, 'read')")
  public void validateDatasets(Collection<Dataset> datasets, Locale locale,
      Consumer<String> errorHandler) {
    if (datasets == null) {
      throw new NullPointerException("datasets parameter cannot be null");
    }
    if (datasets.isEmpty()) {
      // Nothing to validate.
      return;
    }

    AppResources resources = new AppResources(AnalysisService.class, locale);
    List<Optional<DatasetAnalysis>> metadatas =
        datasets.stream().map(ds -> metadata(ds, resources, errorHandler))
            .filter(md -> md.isPresent()).collect(Collectors.toList());
    boolean paired =
        metadatas.stream().flatMap(omd -> omd.map(md -> md.samples.stream()).orElse(Stream.empty()))
            .map(ms -> ms.paired).findFirst().orElse(false);
    if (metadatas.stream().flatMap(omd -> omd.get().samples.stream())
        .filter(ms -> ms.paired != paired).findFirst().isPresent()) {
      errorHandler.accept(resources.message("datasets.pairedMissmatch"));
    }
  }

  /**
   * Validates if samples can be analyzed.
   *
   * @param samples
   *          samples
   * @param locale
   *          locale for error messages
   * @param errorHandler
   *          handles error messages
   */
  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #samples, 'read')")
  public void validateSamples(Collection<Sample> samples, Locale locale,
      Consumer<String> errorHandler) {
    if (samples == null) {
      throw new NullPointerException("samples parameter cannot be null");
    }
    if (samples.isEmpty()) {
      // Nothing to validate.
      return;
    }

    AppResources resources = new AppResources(AnalysisService.class, locale);
    List<Optional<SampleAnalysis>> metadatas =
        samples.stream().map(sa -> metadata(sa, resources, errorHandler))
            .filter(ms -> ms.isPresent()).collect(Collectors.toList());
    boolean paired = metadatas.stream().map(oms -> oms.map(ms -> ms.paired).orElse(false))
        .findFirst().orElse(false);
    if (metadatas.stream().map(oms -> oms.get()).filter(ms -> ms.paired != paired).findFirst()
        .isPresent()) {
      errorHandler.accept(resources.message("samples.pairedMissmatch"));
    }
  }

  private Optional<DatasetAnalysis> metadata(Dataset dataset, AppResources resources,
      Consumer<String> errorHandler) {
    if (dataset.getSamples().isEmpty()) {
      errorHandler.accept(resources.message("dataset.noSample", dataset.getName()));
      return Optional.empty();
    }

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
   * Copy datasets resources used for analysis to a new folder.
   *
   * @param datasets
   *          datasets
   * @return folder the folder containing analysis files
   * @throws IOException
   *           could not copy analysis files to folder
   * @throws IllegalArgumentException
   *           dataset analysis validation failed
   */
  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #datasets, 'read')")
  public Path copyDatasetsResources(Collection<Dataset> datasets) throws IOException {
    if (datasets == null || datasets.isEmpty()) {
      throw new IllegalArgumentException("datasets parameter cannot be null or empty");
    }

    Collection<DatasetAnalysis> analyses = datasets.stream().map(ds -> metadata(ds,
        new AppResources(AnalysisService.class, Locale.getDefault()), message -> {
        })).filter(opt -> opt.isPresent()).map(Optional::get).collect(Collectors.toList());
    if (analyses.size() != datasets.size()) {
      throw new IllegalArgumentException(
          "at least one dataset is missing files required for analysis");
    }
    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.datasetAnalysis(datasets);
    FileSystemUtils.deleteRecursively(folder);
    Files.createDirectories(folder);
    for (DatasetAnalysis analysis : analyses) {
      for (SampleAnalysis sample : analysis.samples) {
        copyFiles(sample, folder, symlinks);
      }
    }
    Path samples = folder.resolve("samples.txt");
    List<String> samplesLines = new ArrayList<>();
    samplesLines.add("#sample");
    analyses.stream().flatMap(analysis -> analysis.dataset.getSamples().stream())
        .forEach(sample -> samplesLines.add(sample.getName()));
    Files.write(samples, samplesLines, StandardOpenOption.CREATE);
    Path datasetFile = folder.resolve("dataset.txt");
    List<String> datasetLines = new ArrayList<>();
    datasetLines.add("#merge\tsamples");
    analyses
        .forEach(analysis -> datasetLines.add(analysis.dataset.getName() + "\t" + analysis.samples
            .stream().map(sample -> sample.sample.getName()).collect(Collectors.joining("\t"))));
    Files.write(datasetFile, datasetLines, StandardOpenOption.CREATE);
    return folder;
  }

  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #samples, 'read')")
  public Path copySamplesResources(Collection<Sample> samples) throws IOException {
    if (samples == null || samples.isEmpty()) {
      throw new IllegalArgumentException("samples parameter cannot be null or empty");
    }

    Collection<SampleAnalysis> analyses = samples.stream().map(sample -> metadata(sample,
        new AppResources(AnalysisService.class, Locale.getDefault()), message -> {
        })).filter(opt -> opt.isPresent()).map(Optional::get).collect(Collectors.toList());
    if (analyses.size() != samples.size()) {
      throw new IllegalArgumentException(
          "at least one dataset is missing files required for analysis");
    }
    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.sampleAnalysis(samples);
    FileSystemUtils.deleteRecursively(folder);
    Files.createDirectories(folder);
    for (SampleAnalysis analysis : analyses) {
      copyFiles(analysis, folder, symlinks);
    }
    Path samplesFile = folder.resolve("samples.txt");
    List<String> samplesLines = new ArrayList<>();
    samplesLines.add("#sample");
    analyses.stream().map(analysis -> analysis.sample)
        .forEach(sample -> samplesLines.add(sample.getName()));
    Files.write(samplesFile, samplesLines, StandardOpenOption.CREATE);
    return folder;
  }

  private void copyFiles(SampleAnalysis analysis, Path folder, boolean symlinks)
      throws IOException {
    Path fastq1 = folder.resolve(analysis.sample.getName() + "_R1.fastq"
        + (analysis.fastq1.toString().endsWith(".gz") ? ".gz" : ""));
    copy(analysis.fastq1, fastq1, symlinks);
    if (analysis.paired) {
      Path fastq2 = folder.resolve(analysis.sample.getName() + "_R2.fastq"
          + (analysis.fastq2.toString().endsWith(".gz") ? ".gz" : ""));
      copy(analysis.fastq2, fastq2, symlinks);
    }
    for (Path bam : analysis.bams) {
      copy(bam, folder.resolve(bam.getFileName()), symlinks);
    }
  }

  private void copy(Path source, Path destination, boolean symlink) throws IOException {
    if (symlink) {
      Files.createSymbolicLink(destination, source);
    } else {
      Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
