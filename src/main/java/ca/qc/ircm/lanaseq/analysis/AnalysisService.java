package ca.qc.ircm.lanaseq.analysis;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
  private static final String MESSAGE_PREFIX = messagePrefix(AnalysisService.class);
  private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
  private DatasetService datasetService;
  private SampleService sampleService;
  private AppConfiguration configuration;
  private MessageSource messageSource;

  @Autowired
  protected AnalysisService(DatasetService datasetService, SampleService sampleService,
      AppConfiguration configuration, MessageSource messageSource) {
    this.datasetService = datasetService;
    this.sampleService = sampleService;
    this.configuration = configuration;
    this.messageSource = messageSource;
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

    List<Optional<DatasetAnalysis>> metadatas =
        datasets.stream().map(ds -> metadata(ds, locale, errorHandler)).filter(md -> md.isPresent())
            .collect(Collectors.toList());
    boolean paired =
        metadatas.stream().flatMap(omd -> omd.map(md -> md.samples.stream()).orElse(Stream.empty()))
            .map(ms -> ms.paired).findFirst().orElse(false);
    if (metadatas.stream().flatMap(omd -> omd.get().samples.stream())
        .filter(ms -> ms.paired != paired).findFirst().isPresent()) {
      errorHandler.accept(
          messageSource.getMessage(MESSAGE_PREFIX + "datasets.pairedMissmatch", null, locale));
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

    List<Optional<SampleAnalysis>> metadatas =
        samples.stream().map(sa -> metadata(sa, locale, errorHandler)).filter(ms -> ms.isPresent())
            .collect(Collectors.toList());
    boolean paired = metadatas.stream().map(oms -> oms.map(ms -> ms.paired).orElse(false))
        .findFirst().orElse(false);
    if (metadatas.stream().map(oms -> oms.get()).filter(ms -> ms.paired != paired).findFirst()
        .isPresent()) {
      errorHandler.accept(
          messageSource.getMessage(MESSAGE_PREFIX + "samples.pairedMissmatch", null, locale));
    }
  }

  private Optional<DatasetAnalysis> metadata(Dataset dataset, Locale locale,
      Consumer<String> errorHandler) {
    if (dataset.getSamples().isEmpty()) {
      errorHandler.accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.noSample",
          new Object[] { dataset.getName() }, locale));
      return Optional.empty();
    }

    List<Optional<SampleAnalysis>> samples = dataset.getSamples().stream()
        .map(sample -> metadata(sample, locale, errorHandler)).collect(Collectors.toList());
    if (!samples.stream().filter(sample -> !sample.isPresent()).findAny().isPresent()) {
      boolean paired =
          samples.stream().findFirst().map(sample -> sample.get().paired).orElse(false);
      if (samples.stream().filter(sample -> sample.get().paired != paired).findAny().isPresent()) {
        errorHandler.accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.pairedMissmatch",
            new Object[] { dataset.getName() }, locale));
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

  private Optional<SampleAnalysis> metadata(Sample sample, Locale locale,
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
      String message = messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
          new Object[] { sample.getName() }, locale);
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

    Collection<DatasetAnalysis> analyses =
        datasets.stream().map(ds -> metadata(ds, Locale.getDefault(), message -> {
        })).filter(opt -> opt.isPresent()).map(Optional::get).collect(Collectors.toList());
    if (analyses.size() != datasets.size()) {
      throw new IllegalArgumentException(
          "at least one dataset is missing files required for analysis");
    }
    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.getAnalysis().folder(datasets);
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

  /**
   * Copy dataset resources used for analysis to a new folder.
   * <p>
   * Files that match filename patterns will also be included. Any file associated with dataset or
   * one of its samples will have its name compared with specified filename patterns.
   * </p>
   *
   * @param datasets
   *          datasets
   * @param filenamePatterns
   *          filename patterns linked to dataset or samples to include in analysis
   * @return folder the folder containing analysis files
   * @throws IOException
   *           could not copy analysis files to folder
   * @throws IllegalArgumentException
   *           dataset analysis validation failed
   */
  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #datasets, 'read')")
  public Path copyDatasetsResources(Collection<Dataset> datasets, List<String> filenamePatterns)
      throws IOException {
    if (datasets == null || datasets.isEmpty()) {
      throw new IllegalArgumentException("datasets parameter cannot be null or empty");
    }
    if (filenamePatterns == null) {
      filenamePatterns = new ArrayList<>();
    }

    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.getAnalysis().folder(datasets);
    FileSystemUtils.deleteRecursively(folder);
    Files.createDirectories(folder);
    Collection<Sample> samples = datasets.stream().flatMap(dataset -> dataset.getSamples().stream())
        .distinct().collect(Collectors.toList());
    Path samplesFile = folder.resolve("samples.txt");
    LinkedHashSet<String> samplesLines = new LinkedHashSet<>();
    samplesLines.add("#sample");
    samples.forEach(sample -> samplesLines.add(sample.getName()));
    Files.write(samplesFile, samplesLines, StandardOpenOption.CREATE);
    Path datasetFile = folder.resolve("dataset.txt");
    LinkedHashSet<String> datasetLines = new LinkedHashSet<>();
    datasetLines.add("#merge\tsamples");
    datasets.forEach(dataset -> datasetLines.add(dataset.getName() + "\t" + dataset.getSamples()
        .stream().map(sample -> sample.getName()).collect(Collectors.joining("\t"))));
    Files.write(datasetFile, datasetLines, StandardOpenOption.CREATE);
    List<PathMatcher> pathMatchers = filenamePatterns.stream()
        .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
        .collect(Collectors.toList());
    Function<List<Path>,
        List<Path>> matchAnyPattern = files -> files.stream()
            .filter(file -> pathMatchers.stream()
                .anyMatch(pathMatcher -> pathMatcher.matches(file.getFileName())))
            .collect(Collectors.toList());
    List<Path> filesToCopy = new ArrayList<>();
    for (Sample sample : samples) {
      List<Path> files = sampleService.files(sample);
      List<Path> toCopy = matchAnyPattern.apply(files);
      filesToCopy.addAll(toCopy);
    }
    for (Dataset dataset : datasets) {
      List<Path> files = datasetService.files(dataset);
      List<Path> toCopy = matchAnyPattern.apply(files);
      filesToCopy.addAll(toCopy);
    }
    for (Path file : filesToCopy) {
      copy(file, folder.resolve(file.getFileName()), symlinks);
    }
    return folder;
  }

  /**
   * Copy sample resources used for analysis to a new folder.
   * <p>
   * Files that match filename patterns will also be included. Any file associated with a sample
   * will have its name compared with specified filename patterns.
   * </p>
   *
   * @param samples
   *          samples
   * @param filenamePatterns
   *          filename patterns linked to samples to include in analysis
   * @return folder the folder containing analysis files
   * @throws IOException
   *           could not copy analysis files to folder
   * @throws IllegalArgumentException
   *           sample analysis validation failed
   */
  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #samples, 'read')")
  public Path copySamplesResources(Collection<Sample> samples, List<String> filenamePatterns)
      throws IOException {
    if (samples == null || samples.isEmpty()) {
      throw new IllegalArgumentException("samples parameter cannot be null or empty");
    }
    if (filenamePatterns == null) {
      filenamePatterns = new ArrayList<>();
    }

    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.getAnalysis().folder(samples);
    FileSystemUtils.deleteRecursively(folder);
    Files.createDirectories(folder);
    Path samplesFile = folder.resolve("samples.txt");
    LinkedHashSet<String> samplesLines = new LinkedHashSet<>();
    samplesLines.add("#sample");
    samples.forEach(sample -> samplesLines.add(sample.getName()));
    Files.write(samplesFile, samplesLines, StandardOpenOption.CREATE);
    List<PathMatcher> pathMatchers = filenamePatterns.stream()
        .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
        .collect(Collectors.toList());
    Function<List<Path>,
        List<Path>> matchAnyPattern = files -> files.stream()
            .filter(file -> pathMatchers.stream()
                .anyMatch(pathMatcher -> pathMatcher.matches(file.getFileName())))
            .collect(Collectors.toList());
    List<Path> filesToCopy = new ArrayList<>();
    for (Sample sample : samples) {
      List<Path> files = sampleService.files(sample);
      List<Path> toCopy = matchAnyPattern.apply(files);
      filesToCopy.addAll(toCopy);
    }
    for (Path file : filesToCopy) {
      copy(file, folder.resolve(file.getFileName()), symlinks);
    }
    return folder;
  }

  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #samples, 'read')")
  public Path copySamplesResources(Collection<Sample> samples) throws IOException {
    if (samples == null || samples.isEmpty()) {
      throw new IllegalArgumentException("samples parameter cannot be null or empty");
    }

    Collection<SampleAnalysis> analyses =
        samples.stream().map(sample -> metadata(sample, Locale.getDefault(), message -> {
        })).filter(opt -> opt.isPresent()).map(Optional::get).collect(Collectors.toList());
    if (analyses.size() != samples.size()) {
      throw new IllegalArgumentException(
          "at least one dataset is missing files required for analysis");
    }
    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.getAnalysis().folder(samples);
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
