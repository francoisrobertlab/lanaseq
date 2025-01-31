package ca.qc.ircm.lanaseq.analysis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.PermissionEvaluatorDelegator;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link AnalysisService}.
 */
@ServiceTestAnnotations
@WithMockUser
public class AnalysisServiceTest {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private AnalysisService service;
  @MockitoBean
  private DatasetService datasetService;
  @MockitoBean
  private SampleService sampleService;
  @MockitoBean
  private AppConfiguration configuration;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private SampleRepository sampleRepository;
  @MockitoBean
  private PermissionEvaluatorDelegator permissionEvaluator;
  private final List<Dataset> datasets = new ArrayList<>();
  private final List<Sample> samples = new ArrayList<>();
  private Dataset dataset;
  private Sample sample;
  private Sample sample2;
  private Path paired1;
  private Path paired2;
  private final List<Path> pairedPaths = new ArrayList<>();
  private Path pairedZip1;
  private Path pairedZip2;
  private final List<Path> pairedZipPaths = new ArrayList<>();
  private Path secondPaired1;
  private Path secondPaired2;
  private final List<Path> secondPairedPaths = new ArrayList<>();
  private Path secondPairedZip1;
  private Path secondPairedZip2;
  private final List<Path> secondPairedZipPaths = new ArrayList<>();
  private Path thirdPaired1;
  private Path thirdPaired2;
  private final List<Path> thirdPairedPaths = new ArrayList<>();
  private Path thirdPairedZip1;
  private Path thirdPairedZip2;
  private final List<Path> thirdPairedZipPaths = new ArrayList<>();
  private Path bam;
  private Path bam2;
  private Path rawbam;
  private Path secondBam;
  private Path secondBam2;
  private Path secondRawbam;
  private Path thirdBam;
  private Path thirdBam2;
  private Path thirdRawbam;
  private final Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(permissionEvaluator.hasCollectionPermission(any(), any(), any())).thenReturn(true);
    dataset = datasetRepository.findById(2L).orElseThrow();
    datasets.add(datasetRepository.findById(7L).orElseThrow());
    datasets.add(dataset);
    datasets.forEach(ds -> samples.addAll(ds.getSamples()));
    sample = sampleRepository.findById(4L).orElseThrow();
    sample2 = sampleRepository.findById(5L).orElseThrow();
    paired1 = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022_R1.fastq");
    paired2 = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022_R2.fastq");
    pairedPaths.add(temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.bed"));
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    pairedZip1 = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022_R1.fastq.gz");
    pairedZip2 = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022_R2.fastq.gz");
    pairedZipPaths.add(temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.bed"));
    pairedZipPaths.add(pairedZip1);
    pairedZipPaths.add(pairedZip2);
    secondPaired1 = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022_R1.fastq");
    secondPaired2 = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022_R2.fastq");
    secondPairedPaths.add(temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondPairedPaths.add(secondPaired1);
    secondPairedPaths.add(secondPaired2);
    secondPairedZip1 =
        temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022_R1.fastq.gz");
    secondPairedZip2 =
        temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022_R2.fastq.gz");
    secondPairedZipPaths
        .add(temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondPairedZipPaths.add(secondPairedZip1);
    secondPairedZipPaths.add(secondPairedZip2);
    thirdPaired1 = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211_R1.fastq");
    thirdPaired2 = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211_R2.fastq");
    thirdPairedPaths.add(temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bed"));
    thirdPairedPaths.add(thirdPaired1);
    thirdPairedPaths.add(thirdPaired2);
    thirdPairedZip1 =
        temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211_R1.fastq.gz");
    thirdPairedZip2 =
        temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211_R2.fastq.gz");
    thirdPairedZipPaths
        .add(temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bed"));
    thirdPairedZipPaths.add(thirdPairedZip1);
    thirdPairedZipPaths.add(thirdPairedZip2);
    bam = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.bam");
    bam2 = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022-test.bam");
    rawbam = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022-raw.bam");
    secondBam = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.bam");
    secondBam2 = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022-test.bam");
    secondRawbam = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022-raw.bam");
    thirdBam = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bam");
    thirdBam2 = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211-test.bam");
    thirdRawbam = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211-raw.bam");
    @SuppressWarnings("unchecked")
    AppConfiguration.NetworkDrive<Collection<? extends DataWithFiles>> analysisFolder =
        mock(AppConfiguration.NetworkDrive.class);
    when(configuration.getAnalysis()).thenReturn(analysisFolder);
    when(configuration.getAnalysis().folder(anyCollection())).then(i -> {
      Collection<?> collection = i.getArgument(0);
      if (collection == null || collection.isEmpty()) {
        return null;
      }
      if (collection.stream().findFirst().get() instanceof Dataset) {
        @SuppressWarnings("unchecked")
        Collection<Dataset> datasets = (Collection<Dataset>) collection;
        return temporaryFolder.resolve(datasets.iterator().next().getName());
      } else {
        @SuppressWarnings("unchecked")
        Collection<Sample> samples = (Collection<Sample>) collection;
        return temporaryFolder.resolve(samples.iterator().next().getName());
      }
    });
  }

  private byte[] writeRandom(Path file) throws IOException {
    byte[] content = new byte[2048];
    random.nextBytes(content);
    Files.write(file, content, StandardOpenOption.CREATE);
    return content;
  }

  @Test
  public void copyDatasetsResources_Fastq() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);

    Path folder = service.copyDatasetsResources(datasets, List.of("*.fastq"));

    assertEquals(configuration.getAnalysis().folder(datasets), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(8, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(datasets.get(0).getName() + "\t" + datasets.get(0).getSamples().get(0).getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset.getName() + "\t" + this.sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(2));
  }

  @Test
  public void copyDatasetsResources_NoFastq() throws Throwable {
    Path folder = service.copyDatasetsResources(datasets, List.of("*.fastq"));

    assertEquals(configuration.getAnalysis().folder(datasets), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(2, files.count());
    }
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(datasets.get(0).getName() + "\t" + datasets.get(0).getSamples().get(0).getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset.getName() + "\t" + this.sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(2));
  }

  @Test
  public void copyDatasetsResources_Fastq_Symlinks() throws Throwable {
    assumeFalse(SystemUtils.IS_OS_WINDOWS); // Symbolic links don't work on Windows.
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);

    Path folder = service.copyDatasetsResources(datasets, List.of("*.fastq"));

    assertEquals(configuration.getAnalysis().folder(datasets), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(8, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertTrue(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertTrue(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertTrue(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertTrue(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertTrue(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertTrue(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(datasets.get(0).getName() + "\t" + datasets.get(0).getSamples().get(0).getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset.getName() + "\t" + this.sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(2));
  }

  @Test
  public void copyDatasetsResources_Fastq_Zip() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths).thenReturn(pairedZipPaths)
        .thenReturn(secondPairedZipPaths);
    final byte[] fastq1Content = writeRandom(pairedZip1);
    final byte[] fastq2Content = writeRandom(pairedZip2);
    final byte[] fastq3Content = writeRandom(secondPairedZip1);
    final byte[] fastq4Content = writeRandom(secondPairedZip2);
    final byte[] fastq5Content = writeRandom(thirdPairedZip1);
    final byte[] fastq6Content = writeRandom(thirdPairedZip2);

    Path folder = service.copyDatasetsResources(datasets, List.of("*.fastq*"));

    assertEquals(configuration.getAnalysis().folder(datasets), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(8, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq.gz");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq.gz");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq.gz");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq.gz");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq.gz");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq.gz");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(datasets.get(0).getName() + "\t" + datasets.get(0).getSamples().get(0).getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset.getName() + "\t" + this.sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(2));
  }

  @Test
  public void copyDatasetsResources_Fastq_Bam() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    final byte[] bamContent = writeRandom(bam);
    final byte[] bam2Content = writeRandom(bam2);
    final byte[] rawbamContent = writeRandom(rawbam);
    pairedPaths.add(bam);
    pairedPaths.add(bam2);
    pairedPaths.add(rawbam);
    final byte[] secondBamContent = writeRandom(secondBam);
    final byte[] secondBam2Content = writeRandom(secondBam2);
    final byte[] secondRawbamContent = writeRandom(secondRawbam);
    secondPairedPaths.add(secondBam);
    secondPairedPaths.add(secondBam2);
    secondPairedPaths.add(secondRawbam);
    final byte[] thirdBamContent = writeRandom(thirdBam);
    final byte[] thirdBam2Content = writeRandom(thirdBam2);
    final byte[] thirdRawbamContent = writeRandom(thirdRawbam);
    thirdPairedPaths.add(thirdBam);
    thirdPairedPaths.add(thirdBam2);
    thirdPairedPaths.add(thirdRawbam);

    Path folder = service.copyDatasetsResources(datasets, Arrays.asList("*.fastq", "*.bam"));

    assertEquals(configuration.getAnalysis().folder(datasets), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(17, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path bam1 = folder.resolve(sample.getName() + ".bam");
    assertTrue(Files.exists(bam1));
    assertFalse(Files.isSymbolicLink(bam1));
    assertArrayEquals(bamContent, Files.readAllBytes(bam1));
    Path bam2 = folder.resolve(sample.getName() + "-test.bam");
    assertTrue(Files.exists(bam2));
    assertFalse(Files.isSymbolicLink(bam2));
    assertArrayEquals(bam2Content, Files.readAllBytes(bam2));
    Path bam3 = folder.resolve(sample.getName() + "-raw.bam");
    assertTrue(Files.exists(bam3));
    assertFalse(Files.isSymbolicLink(bam3));
    assertArrayEquals(rawbamContent, Files.readAllBytes(bam3));
    Path bam4 = folder.resolve(sample2.getName() + ".bam");
    assertTrue(Files.exists(bam4));
    assertFalse(Files.isSymbolicLink(bam4));
    assertArrayEquals(secondBamContent, Files.readAllBytes(bam4));
    Path bam5 = folder.resolve(sample2.getName() + "-test.bam");
    assertTrue(Files.exists(bam5));
    assertFalse(Files.isSymbolicLink(bam5));
    assertArrayEquals(secondBam2Content, Files.readAllBytes(bam5));
    Path bam6 = folder.resolve(sample2.getName() + "-raw.bam");
    assertTrue(Files.exists(bam6));
    assertFalse(Files.isSymbolicLink(bam6));
    assertArrayEquals(secondRawbamContent, Files.readAllBytes(bam6));
    Path bam7 = folder.resolve(sample3.getName() + ".bam");
    assertTrue(Files.exists(bam7));
    assertFalse(Files.isSymbolicLink(bam7));
    assertArrayEquals(thirdBamContent, Files.readAllBytes(bam7));
    Path bam8 = folder.resolve(sample3.getName() + "-test.bam");
    assertTrue(Files.exists(bam8));
    assertFalse(Files.isSymbolicLink(bam8));
    assertArrayEquals(thirdBam2Content, Files.readAllBytes(bam8));
    Path bam9 = folder.resolve(sample3.getName() + "-raw.bam");
    assertTrue(Files.exists(bam9));
    assertFalse(Files.isSymbolicLink(bam9));
    assertArrayEquals(thirdRawbamContent, Files.readAllBytes(bam9));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(datasets.get(0).getName() + "\t" + datasets.get(0).getSamples().get(0).getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset.getName() + "\t" + this.sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(2));
  }

  @Test
  public void copyDatasetsResources_Fastq_FolderAlreadyExists() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = configuration.getAnalysis().folder(datasets);
    String extraFilename = "test.bam";
    Files.createDirectories(folder);
    Files.write(folder.resolve(paired1.getFileName()), fastq2Content);
    Files.write(folder.resolve(extraFilename), fastq1Content);

    Path copyFolder = service.copyDatasetsResources(datasets, List.of("*.fastq"));

    assertEquals(folder, copyFolder);
    assertFalse(Files.exists(folder.resolve(extraFilename)));
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(8, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(datasets.get(0).getName() + "\t" + datasets.get(0).getSamples().get(0).getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset.getName() + "\t" + sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(2));
  }

  @Test
  public void copyDatasetsResources_SameSamples() throws Throwable {
    List<Dataset> datasets = new ArrayList<>();
    datasets.add(datasetRepository.findById(2L).orElseThrow());
    datasets.add(datasetRepository.findById(6L).orElseThrow());

    Path folder = service.copyDatasetsResources(datasets, new ArrayList<>());

    assertEquals(configuration.getAnalysis().folder(datasets), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(2, files.count());
    }
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022", samplesContent.get(1));
    assertEquals("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022", samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(
        "ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022\tJS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022\t"
            + "JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022",
        datasetMetaContent.get(1));
    assertEquals(
        "ChIPseq_Spt16_yFR101_G24D_JS1_20181208\tJS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022",
        datasetMetaContent.get(2));
  }

  @Test
  public void copyDatasetsResources_EmptyDatasets() {
    assertThrows(IllegalArgumentException.class,
        () -> service.copyDatasetsResources(new ArrayList<>(), new ArrayList<>()));
  }

  @Test
  public void copyDatasetsResources_EmptyFilenamePatterns() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    Files.createFile(paired1);
    Files.createFile(paired2);
    Files.createFile(secondPaired1);
    Files.createFile(secondPaired2);
    Files.createFile(thirdPaired1);
    Files.createFile(thirdPaired2);

    Path folder = service.copyDatasetsResources(datasets, new ArrayList<>());

    assertEquals(configuration.getAnalysis().folder(datasets), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(2, files.count());
    }
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(datasets.get(0).getName() + "\t" + datasets.get(0).getSamples().get(0).getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset.getName() + "\t" + this.sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(2));
  }

  @Test
  public void copySamplesResources_Fastq() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);

    Path folder = service.copySamplesResources(samples, List.of("*.fastq"));

    assertEquals(configuration.getAnalysis().folder(samples), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(7, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void copySamplesResources_NoFastq() throws Throwable {
    Path folder = service.copySamplesResources(samples, List.of("*.fastq"));

    assertEquals(configuration.getAnalysis().folder(samples), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(1, files.count());
    }
    Sample sample3 = samples.get(0);
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void copySamplesResources_Fastq_Symlinks() throws Throwable {
    assumeFalse(SystemUtils.IS_OS_WINDOWS); // Symbolic links don't work on Windows.
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);

    Path folder = service.copySamplesResources(samples, List.of("*.fastq"));

    assertEquals(configuration.getAnalysis().folder(samples), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(7, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertTrue(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertTrue(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertTrue(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertTrue(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertTrue(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertTrue(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void copySamplesResources_Fastq_Zip() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths).thenReturn(pairedZipPaths)
        .thenReturn(secondPairedZipPaths);
    final byte[] fastq1Content = writeRandom(pairedZip1);
    final byte[] fastq2Content = writeRandom(pairedZip2);
    final byte[] fastq3Content = writeRandom(secondPairedZip1);
    final byte[] fastq4Content = writeRandom(secondPairedZip2);
    final byte[] fastq5Content = writeRandom(thirdPairedZip1);
    final byte[] fastq6Content = writeRandom(thirdPairedZip2);

    Path folder = service.copySamplesResources(samples, List.of("*.fastq*"));

    assertEquals(configuration.getAnalysis().folder(samples), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(7, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq.gz");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq.gz");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq.gz");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq.gz");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq.gz");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq.gz");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void copySamplesResources_Fastq_Bam() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    final byte[] bamContent = writeRandom(bam);
    final byte[] bam2Content = writeRandom(bam2);
    final byte[] rawbamContent = writeRandom(rawbam);
    pairedPaths.add(bam);
    pairedPaths.add(bam2);
    pairedPaths.add(rawbam);
    final byte[] secondBamContent = writeRandom(secondBam);
    final byte[] secondBam2Content = writeRandom(secondBam2);
    final byte[] secondRawbamContent = writeRandom(secondRawbam);
    secondPairedPaths.add(secondBam);
    secondPairedPaths.add(secondBam2);
    secondPairedPaths.add(secondRawbam);
    final byte[] thirdBamContent = writeRandom(thirdBam);
    final byte[] thirdBam2Content = writeRandom(thirdBam2);
    final byte[] thirdRawbamContent = writeRandom(thirdRawbam);
    thirdPairedPaths.add(thirdBam);
    thirdPairedPaths.add(thirdBam2);
    thirdPairedPaths.add(thirdRawbam);

    Path folder = service.copySamplesResources(samples, Arrays.asList("*.fastq", "*.bam"));

    assertEquals(configuration.getAnalysis().folder(samples), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(16, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path bam1 = folder.resolve(sample.getName() + ".bam");
    assertTrue(Files.exists(bam1));
    assertFalse(Files.isSymbolicLink(bam1));
    assertArrayEquals(bamContent, Files.readAllBytes(bam1));
    Path bam2 = folder.resolve(sample.getName() + "-test.bam");
    assertTrue(Files.exists(bam2));
    assertFalse(Files.isSymbolicLink(bam2));
    assertArrayEquals(bam2Content, Files.readAllBytes(bam2));
    Path bam3 = folder.resolve(sample.getName() + "-raw.bam");
    assertTrue(Files.exists(bam3));
    assertFalse(Files.isSymbolicLink(bam3));
    assertArrayEquals(rawbamContent, Files.readAllBytes(bam3));
    Path bam4 = folder.resolve(sample2.getName() + ".bam");
    assertTrue(Files.exists(bam4));
    assertFalse(Files.isSymbolicLink(bam4));
    assertArrayEquals(secondBamContent, Files.readAllBytes(bam4));
    Path bam5 = folder.resolve(sample2.getName() + "-test.bam");
    assertTrue(Files.exists(bam5));
    assertFalse(Files.isSymbolicLink(bam5));
    assertArrayEquals(secondBam2Content, Files.readAllBytes(bam5));
    Path bam6 = folder.resolve(sample2.getName() + "-raw.bam");
    assertTrue(Files.exists(bam6));
    assertFalse(Files.isSymbolicLink(bam6));
    assertArrayEquals(secondRawbamContent, Files.readAllBytes(bam6));
    Path bam7 = folder.resolve(sample3.getName() + ".bam");
    assertTrue(Files.exists(bam7));
    assertFalse(Files.isSymbolicLink(bam7));
    assertArrayEquals(thirdBamContent, Files.readAllBytes(bam7));
    Path bam8 = folder.resolve(sample3.getName() + "-test.bam");
    assertTrue(Files.exists(bam8));
    assertFalse(Files.isSymbolicLink(bam8));
    assertArrayEquals(thirdBam2Content, Files.readAllBytes(bam8));
    Path bam9 = folder.resolve(sample3.getName() + "-raw.bam");
    assertTrue(Files.exists(bam9));
    assertFalse(Files.isSymbolicLink(bam9));
    assertArrayEquals(thirdRawbamContent, Files.readAllBytes(bam9));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void copySamplesResources_Fastq_AlreadyExists() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = configuration.getAnalysis().folder(samples);
    String extraFilename = "test.bam";
    Files.createDirectories(folder);
    Files.write(folder.resolve(paired1.getFileName()), fastq2Content);
    Files.write(folder.resolve(extraFilename), fastq1Content);

    Path copyFolder = service.copySamplesResources(samples, List.of("*.fastq"));

    assertEquals(folder, copyFolder);
    assertFalse(Files.exists(folder.resolve(extraFilename)));
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(7, files.count());
    }
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq2));
    assertFalse(Files.isSymbolicLink(fastq2));
    assertArrayEquals(fastq2Content, Files.readAllBytes(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq4));
    assertFalse(Files.isSymbolicLink(fastq4));
    assertArrayEquals(fastq4Content, Files.readAllBytes(fastq4));
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertFalse(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void copySamplesResources_EmptySamples() {
    assertThrows(IllegalArgumentException.class,
        () -> service.copySamplesResources(new ArrayList<>(), new ArrayList<>()));
  }

  @Test
  public void copySamplesResources_EmptyFilenamePatterns() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths).thenReturn(pairedPaths)
        .thenReturn(secondPairedPaths);
    Files.createFile(paired1);
    Files.createFile(paired2);
    Files.createFile(secondPaired1);
    Files.createFile(secondPaired2);
    Files.createFile(thirdPaired1);
    Files.createFile(thirdPaired2);

    Path folder = service.copySamplesResources(samples, new ArrayList<>());

    assertEquals(configuration.getAnalysis().folder(samples), folder);
    assertTrue(Files.exists(folder));
    try (Stream<Path> files = Files.list(folder)) {
      assertEquals(1, files.count());
    }
    Sample sample3 = samples.get(0);
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample3.getName(), samplesContent.get(1));
    assertEquals(sample.getName(), samplesContent.get(2));
    assertEquals(sample2.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }
}
