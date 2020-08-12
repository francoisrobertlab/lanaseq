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
  private static final String FASTQ1_PATTERN = ".*_R1" + FASTQ;
  private static final String FASTQ2_PATTERN = ".*_R2" + FASTQ;
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
    List<Path> fastqs = sampleService.files(sample).stream()
        .filter(file -> fastqPattern.matcher(file.toString()).matches())
        .collect(Collectors.toList());
    Path fastq1 = fastqs.stream().filter(file -> fastq1Pattern.matcher(file.toString()).matches())
        .findAny().orElse(fastqs.isEmpty() ? null : fastqs.get(0));
    Path fastq2 = fastqs.stream().filter(file -> fastq2Pattern.matcher(file.toString()).matches())
        .findAny().orElse(null);
    if (fastq1 == null) {
      String message = resources.message("sample.noFastq", sample.getName());
      errorHandler.accept(message);
      return Optional.empty();
    } else if (fastq2 == null || fastq2.equals(fastq1)) {
      SampleAnalysis analysis = new SampleAnalysis();
      analysis.sample = sample;
      analysis.paired = false;
      analysis.fastq1 = fastq1;
      return Optional.of(analysis);
    } else {
      SampleAnalysis analysis = new SampleAnalysis();
      analysis.sample = sample;
      analysis.paired = true;
      analysis.fastq1 = fastq1;
      analysis.fastq2 = fastq2;
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
