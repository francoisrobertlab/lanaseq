package ca.qc.ircm.lanaseq.analysis;

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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

/**
 * Services for analysis.
 */
@Service
public class AnalysisService {

  private DatasetService datasetService;
  private SampleService sampleService;
  private AppConfiguration configuration;

  @Autowired
  protected AnalysisService(DatasetService datasetService, SampleService sampleService,
      AppConfiguration configuration) {
    this.datasetService = datasetService;
    this.sampleService = sampleService;
    this.configuration = configuration;
  }

  /**
   * Copy dataset resources used for analysis to a new folder.
   * <p>
   * Files that match filename patterns will also be included. Any file associated with dataset or
   * one of its samples will have its name compared with specified filename patterns.
   * </p>
   *
   * @param datasets         datasets
   * @param filenamePatterns filename patterns linked to dataset or samples to include in analysis
   * @return folder the folder containing analysis files
   * @throws IOException              could not copy analysis files to folder
   * @throws IllegalArgumentException dataset analysis validation failed
   */
  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #datasets, 'read')")
  public Path copyDatasetsResources(Collection<Dataset> datasets,
      Collection<String> filenamePatterns) throws IOException {
    Objects.requireNonNull(datasets, "datasets parameter cannot be null");
    Objects.requireNonNull(filenamePatterns, "filenamePatterns parameter cannot be null");
    if (datasets.isEmpty()) {
      throw new IllegalArgumentException("datasets parameter cannot be empty");
    }

    boolean symlinks = configuration.isAnalysisSymlinks();
    Path folder = configuration.getAnalysis().folder(datasets);
    FileSystemUtils.deleteRecursively(folder);
    Files.createDirectories(folder);
    Collection<Sample> samples =
        datasets.stream().flatMap(dataset -> dataset.getSamples().stream()).distinct().toList();
    Path samplesFile = folder.resolve("samples.txt");
    LinkedHashSet<String> samplesLines = new LinkedHashSet<>();
    samplesLines.add("#sample");
    samples.forEach(sample -> samplesLines.add(sample.getName()));
    Files.write(samplesFile, samplesLines, StandardOpenOption.CREATE);
    Path datasetFile = folder.resolve("dataset.txt");
    LinkedHashSet<String> datasetLines = new LinkedHashSet<>();
    datasetLines.add("#merge\tsamples");
    datasets.forEach(dataset -> datasetLines.add(dataset.getName() + "\t"
        + dataset.getSamples().stream().map(Sample::getName).collect(Collectors.joining("\t"))));
    Files.write(datasetFile, datasetLines, StandardOpenOption.CREATE);
    List<PathMatcher> pathMatchers = filenamePatterns.stream()
        .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern)).toList();
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
   * @param samples          samples
   * @param filenamePatterns filename patterns linked to samples to include in analysis
   * @return folder the folder containing analysis files
   * @throws IOException              could not copy analysis files to folder
   * @throws IllegalArgumentException sample analysis validation failed
   */
  @PreAuthorize("@permissionEvaluator.hasCollectionPermission(authentication, #samples, 'read')")
  public Path copySamplesResources(Collection<Sample> samples, Collection<String> filenamePatterns)
      throws IOException {
    Objects.requireNonNull(samples, "samples parameter cannot be null");
    Objects.requireNonNull(filenamePatterns, "filenamePatterns parameter cannot be null");
    if (samples.isEmpty()) {
      throw new IllegalArgumentException("samples parameter cannot be empty");
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
        .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern)).toList();
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

  private void copy(Path source, Path destination, boolean symlink) throws IOException {
    if (symlink) {
      Files.createSymbolicLink(destination, source);
    } else {
      Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
