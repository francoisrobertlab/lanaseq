package ca.qc.ircm.lanaseq.analysis;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class AnalysisServiceTest {
  private static final String READ = "read";
  @Autowired
  private AnalysisService service;
  @MockBean
  private DatasetService datasetService;
  @MockBean
  private SampleService sampleService;
  @MockBean
  private AppConfiguration configuration;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private SampleRepository sampleRepository;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @Mock
  private Consumer<String> errorHandler;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Dataset dataset;
  private Sample sample;
  private Sample sample2;
  private Path paired1;
  private Path paired2;
  private List<Path> pairedPaths = new ArrayList<>();
  private Path pairedZip1;
  private Path pairedZip2;
  private List<Path> pairedZipPaths = new ArrayList<>();
  private Path unpaired;
  private List<Path> unpairedPaths = new ArrayList<>();
  private Path unpairedZip;
  private List<Path> unpairedZipPaths = new ArrayList<>();
  private Path secondPaired1;
  private Path secondPaired2;
  private List<Path> secondPairedPaths = new ArrayList<>();
  private Path secondPairedZip1;
  private Path secondPairedZip2;
  private List<Path> secondPairedZipPaths = new ArrayList<>();
  private Path secondUnpaired;
  private List<Path> secondUnpairedPaths = new ArrayList<>();
  private Path secondUnpairedZip;
  private List<Path> secondUnpairedZipPaths = new ArrayList<>();
  private Random random = new Random();
  private Locale locale = ENGLISH;
  private AppResources resources = new AppResources(AnalysisService.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    dataset = datasetRepository.findById(2L).get();
    sample = sampleRepository.findById(4L).get();
    sample2 = sampleRepository.findById(5L).get();
    Path folder = temporaryFolder.getRoot().toPath();
    paired1 = folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R1.fastq");
    paired2 = folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R2.fastq");
    pairedPaths.add(folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    pairedZip1 = folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R1.fastq.gz");
    pairedZip2 = folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R2.fastq.gz");
    pairedZipPaths.add(folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    pairedZipPaths.add(pairedZip1);
    pairedZipPaths.add(pairedZip2);
    unpaired = folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.fastq");
    unpairedPaths.add(folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    unpairedPaths.add(unpaired);
    unpairedZip = folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.fastq.gz");
    unpairedZipPaths.add(folder.resolve("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    unpairedZipPaths.add(unpairedZip);
    secondPaired1 = folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R1.fastq");
    secondPaired2 = folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R2.fastq");
    secondPairedPaths.add(folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondPairedPaths.add(secondPaired1);
    secondPairedPaths.add(secondPaired2);
    secondPairedZip1 = folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R1.fastq.gz");
    secondPairedZip2 = folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R2.fastq.gz");
    secondPairedZipPaths.add(folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondPairedZipPaths.add(secondPairedZip1);
    secondPairedZipPaths.add(secondPairedZip2);
    secondUnpaired = folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.fastq");
    secondUnpairedPaths.add(folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondUnpairedPaths.add(secondUnpaired);
    secondUnpairedZip = folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.fastq.gz");
    secondUnpairedZipPaths.add(folder.resolve("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondUnpairedZipPaths.add(secondUnpairedZip);
    when(configuration.analysis(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.getRoot().toPath().resolve(dataset.getName())
          : null;
    });
  }

  private byte[] writeRandom(Path file) throws IOException {
    byte[] content = new byte[2048];
    random.nextBytes(content);
    Files.write(file, content, StandardOpenOption.CREATE);
    return content;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPaired() {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedNoSampleName() {
    Path folder = temporaryFolder.getRoot().toPath();
    paired1 = folder.resolve("R1.fastq");
    paired2 = folder.resolve("R2.fastq");
    pairedPaths.clear();
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedFirstMissing() {
    pairedPaths.remove(paired1);
    secondPairedPaths.remove(secondPaired1);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedFirstMissingFirstSample() {
    pairedPaths.remove(paired1);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedSecondMissing() {
    pairedPaths.remove(paired2);
    secondPairedPaths.remove(secondPaired2);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedSecondMissingFirstSample() {
    pairedPaths.remove(paired2);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedZip() {
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedZipFirstMissing() {
    pairedZipPaths.remove(pairedZip1);
    secondPairedZipPaths.remove(secondPairedZip1);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedZipFirstMissingFirstSample() {
    pairedZipPaths.remove(pairedZip1);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedZipSecondMissing() {
    pairedZipPaths.remove(pairedZip2);
    secondPairedZipPaths.remove(secondPairedZip2);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedZipSecondMissingFirstSample() {
    pairedZipPaths.remove(pairedZip2);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetUnpaired() {
    when(sampleService.files(any())).thenReturn(unpairedPaths, secondUnpairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondUnpairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetUnpairedZip() {
    when(sampleService.files(any())).thenReturn(unpairedZipPaths, secondUnpairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetPairedZipSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondUnpairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetUnpairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(unpairedPaths, secondUnpairedZipPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void validate_DatasetNoFastq() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>());
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", dataset.getSamples().get(0).getName()));
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", dataset.getSamples().get(1).getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetNoFastqFistSample() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>(), secondPairedPaths);
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", dataset.getSamples().get(0).getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetNoFastqSecondSample() {
    when(sampleService.files(any())).thenReturn(pairedPaths, new ArrayList<>());
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", dataset.getSamples().get(1).getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetFirstPairedSecondUnpairedThirdNoFastq() {
    Sample sample = sampleRepository.findById(1L).get();
    dataset.getSamples().add(sample);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondUnpairedPaths,
        new ArrayList<>());
    service.validate(dataset, locale, errorHandler);
    verify(errorHandler).accept(resources.message("sample.noFastq", sample.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_Dataset() throws Throwable {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    Path folder = service.copyResources(dataset);
    assertEquals(configuration.analysis(dataset), folder);
    assertTrue(Files.exists(folder));
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
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(dataset.getName() + "\t" + sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_DatasetNoSampleName() throws Throwable {
    Path home = temporaryFolder.getRoot().toPath();
    paired1 = home.resolve("R1.fastq");
    paired2 = home.resolve("R2.fastq");
    pairedPaths.clear();
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    Path folder = service.copyResources(dataset);
    assertEquals(configuration.analysis(dataset), folder);
    assertTrue(Files.exists(folder));
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
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(dataset.getName() + "\t" + sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_DatasetSymlinks() throws Throwable {
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    Path folder = service.copyResources(dataset);
    assertEquals(configuration.analysis(dataset), folder);
    assertTrue(Files.exists(folder));
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
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(dataset.getName() + "\t" + sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_DatasetZip() throws Throwable {
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    final byte[] fastq1Content = writeRandom(pairedZip1);
    final byte[] fastq2Content = writeRandom(pairedZip2);
    final byte[] fastq3Content = writeRandom(secondPairedZip1);
    final byte[] fastq4Content = writeRandom(secondPairedZip2);
    Path folder = service.copyResources(dataset);
    assertEquals(configuration.analysis(dataset), folder);
    assertTrue(Files.exists(folder));
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
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(dataset.getName() + "\t" + sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_DatasetUnpaired() throws Throwable {
    when(sampleService.files(any())).thenReturn(unpairedPaths, secondUnpairedPaths);
    final byte[] fastq1Content = writeRandom(unpaired);
    final byte[] fastq3Content = writeRandom(secondUnpaired);
    Path folder = service.copyResources(dataset);
    assertEquals(configuration.analysis(dataset), folder);
    assertTrue(Files.exists(folder));
    Path fastq1 = folder.resolve(sample.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq1));
    assertFalse(Files.isSymbolicLink(fastq1));
    assertArrayEquals(fastq1Content, Files.readAllBytes(fastq1));
    Path fastq2 = folder.resolve(sample.getName() + "_R2.fastq");
    assertFalse(Files.exists(fastq2));
    Path fastq3 = folder.resolve(sample2.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq3));
    assertFalse(Files.isSymbolicLink(fastq3));
    assertArrayEquals(fastq3Content, Files.readAllBytes(fastq3));
    Path fastq4 = folder.resolve(sample2.getName() + "_R2.fastq");
    assertFalse(Files.exists(fastq4));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(dataset.getName() + "\t" + sample.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }
}
