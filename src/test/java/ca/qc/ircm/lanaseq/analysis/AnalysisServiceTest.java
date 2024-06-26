package ca.qc.ircm.lanaseq.analysis;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link AnalysisService}.
 */
@ServiceTestAnnotations
@WithMockUser
public class AnalysisServiceTest {
  private static final String MESSAGE_PREFIX = messagePrefix(AnalysisService.class);
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
  @Autowired
  private MessageSource messageSource;
  @MockBean
  private PermissionEvaluatorDelegator permissionEvaluator;
  @Mock
  private Consumer<String> errorHandler;
  @TempDir
  Path temporaryFolder;
  private List<Dataset> datasets = new ArrayList<>();
  private List<Sample> samples = new ArrayList<>();
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
  private Path thirdPaired1;
  private Path thirdPaired2;
  private List<Path> thirdPairedPaths = new ArrayList<>();
  private Path thirdPairedZip1;
  private Path thirdPairedZip2;
  private List<Path> thirdPairedZipPaths = new ArrayList<>();
  private Path thirdUnpaired;
  private List<Path> thirdUnpairedPaths = new ArrayList<>();
  private Path thirdUnpairedZip;
  private List<Path> thirdUnpairedZipPaths = new ArrayList<>();
  private Path bam;
  private Path bam2;
  private Path rawbam;
  private Path secondBam;
  private Path secondBam2;
  private Path secondRawbam;
  private Path thirdBam;
  private Path thirdBam2;
  private Path thirdRawbam;
  private Random random = new Random();
  private Locale locale = ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(permissionEvaluator.hasCollectionPermission(any(), any(), any())).thenReturn(true);
    dataset = datasetRepository.findById(2L).get();
    datasets.add(datasetRepository.findById(7L).get());
    datasets.add(dataset);
    datasets.forEach(ds -> samples.addAll(ds.getSamples()));
    sample = sampleRepository.findById(4L).get();
    sample2 = sampleRepository.findById(5L).get();
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
    unpaired = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.fastq");
    unpairedPaths.add(temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.bed"));
    unpairedPaths.add(unpaired);
    unpairedZip = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.fastq.gz");
    unpairedZipPaths.add(temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.bed"));
    unpairedZipPaths.add(unpairedZip);
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
    secondUnpaired = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.fastq");
    secondUnpairedPaths
        .add(temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondUnpairedPaths.add(secondUnpaired);
    secondUnpairedZip =
        temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.fastq.gz");
    secondUnpairedZipPaths
        .add(temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondUnpairedZipPaths.add(secondUnpairedZip);
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
    thirdUnpaired = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.fastq");
    thirdUnpairedPaths
        .add(temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bed"));
    thirdUnpairedPaths.add(thirdUnpaired);
    thirdUnpairedZip =
        temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.fastq.gz");
    thirdUnpairedZipPaths
        .add(temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bed"));
    thirdUnpairedZipPaths.add(thirdUnpairedZip);
    bam = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022.bam");
    bam2 = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022-test.bam");
    rawbam = temporaryFolder.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022-raw.bam");
    secondBam = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022.bam");
    secondBam2 = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022-test.bam");
    secondRawbam = temporaryFolder.resolve("JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022-raw.bam");
    thirdBam = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bam");
    thirdBam2 = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211-test.bam");
    thirdRawbam = temporaryFolder.resolve("JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211-raw.bam");
    when(configuration.getAnalysis()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getAnalysis().folder(any(Collection.class))).then(i -> {
      Collection<?> collection = i.getArgument(0);
      if (collection == null || collection.isEmpty()) {
        return null;
      }
      if (collection.stream().findFirst().get() instanceof Dataset) {
        Collection<Dataset> datasets = (Collection<Dataset>) collection;
        return datasets != null && !datasets.isEmpty()
            && datasets.iterator().next().getName() != null
                ? temporaryFolder.resolve(datasets.iterator().next().getName())
                : null;
      } else {
        Collection<Sample> samples = (Collection<Sample>) collection;
        return samples != null && !samples.isEmpty() && samples.iterator().next().getName() != null
            ? temporaryFolder.resolve(samples.iterator().next().getName())
            : null;
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
  @SuppressWarnings("unchecked")
  public void validateDatasets_NoSample() {
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    service.validateDatasets(datasets, locale, errorHandler);
    datasets.stream().forEach(ds -> verify(errorHandler).accept(messageSource
        .getMessage(MESSAGE_PREFIX + "dataset.noSample", new Object[] { ds.getName() }, locale)));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedMissmatch() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, pairedPaths, secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(
        messageSource.getMessage(MESSAGE_PREFIX + "datasets.pairedMissmatch", null, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_Paired() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedNoSampleName() {
    paired1 = temporaryFolder.resolve("R1.fastq");
    paired2 = temporaryFolder.resolve("R2.fastq");
    pairedPaths.clear();
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedFirstMissing() {
    pairedPaths.remove(paired1);
    secondPairedPaths.remove(secondPaired1);
    thirdPairedPaths.remove(thirdPaired1);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedFirstMissingFirstSample() {
    pairedPaths.remove(paired1);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.pairedMissmatch",
        new Object[] { dataset.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedSecondMissing() {
    pairedPaths.remove(paired2);
    secondPairedPaths.remove(secondPaired2);
    thirdPairedPaths.remove(thirdPaired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedSecondMissingFirstSample() {
    pairedPaths.remove(paired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.pairedMissmatch",
        new Object[] { dataset.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedZipMissmatch() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedZipPaths, pairedZipPaths,
        pairedZipPaths, secondPairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(
        messageSource.getMessage(MESSAGE_PREFIX + "datasets.pairedMissmatch", null, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedZip() {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedZipFirstMissing() {
    pairedZipPaths.remove(pairedZip1);
    secondPairedZipPaths.remove(secondPairedZip1);
    thirdPairedZipPaths.remove(thirdPairedZip1);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedZipFirstMissingFirstSample() {
    pairedZipPaths.remove(pairedZip1);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.pairedMissmatch",
        new Object[] { dataset.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedZipSecondMissing() {
    pairedZipPaths.remove(pairedZip2);
    secondPairedZipPaths.remove(secondPairedZip2);
    thirdPairedZipPaths.remove(thirdPairedZip2);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedZipSecondMissingFirstSample() {
    pairedZipPaths.remove(pairedZip2);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.pairedMissmatch",
        new Object[] { dataset.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths,
        secondPairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_Unpaired() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondUnpairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.pairedMissmatch",
        new Object[] { dataset.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_UnpairedZip() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedZipPaths, unpairedZipPaths,
        secondUnpairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_PairedZipSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondUnpairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "dataset.pairedMissmatch",
        new Object[] { dataset.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_UnpairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedZipPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  public void validateDatasets_NoFastq() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>());
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { datasets.get(0).getSamples().get(0).getName() }, locale));
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { datasets.get(1).getSamples().get(0).getName() }, locale));
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { datasets.get(1).getSamples().get(1).getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_NoFastqFistSample() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>(), new ArrayList<>(),
        secondPairedPaths);
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { datasets.get(0).getSamples().get(0).getName() }, locale));
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { datasets.get(1).getSamples().get(0).getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_NoFastqSecondSample() {
    when(sampleService.files(any())).thenReturn(pairedPaths, pairedPaths, new ArrayList<>());
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { sample2.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_FirstPairedSecondUnpairedThirdNoFastq() {
    Sample sample = sampleRepository.findById(1L).get();
    dataset.getSamples().add(sample);
    when(sampleService.files(any())).thenReturn(pairedPaths, pairedPaths, secondUnpairedPaths,
        new ArrayList<>());
    service.validateDatasets(datasets, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { sample.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_Null() {
    assertThrows(NullPointerException.class, () -> {
      service.validateDatasets((Collection<Dataset>) null, locale, errorHandler);
    });
    verify(errorHandler, never()).accept(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateDatasets_Empty() {
    service.validateDatasets(new ArrayList<>(), locale, errorHandler);
    verify(errorHandler, never()).accept(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedMissmatch() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, pairedPaths, secondPairedPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler)
        .accept(messageSource.getMessage(MESSAGE_PREFIX + "samples.pairedMissmatch", null, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_Paired() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedNoSampleName() {
    paired1 = temporaryFolder.resolve("R1.fastq");
    paired2 = temporaryFolder.resolve("R2.fastq");
    pairedPaths.clear();
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedFirstMissing() {
    pairedPaths.remove(paired1);
    secondPairedPaths.remove(secondPaired1);
    thirdPairedPaths.remove(thirdPaired1);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedSecondMissing() {
    pairedPaths.remove(paired2);
    secondPairedPaths.remove(secondPaired2);
    thirdPairedPaths.remove(thirdPaired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedZipMissmatch() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedZipPaths, pairedZipPaths,
        pairedZipPaths, secondPairedZipPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler)
        .accept(messageSource.getMessage(MESSAGE_PREFIX + "samples.pairedMissmatch", null, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedZip() {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedZipFirstMissing() {
    pairedZipPaths.remove(pairedZip1);
    secondPairedZipPaths.remove(secondPairedZip1);
    thirdPairedZipPaths.remove(thirdPairedZip1);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedZipSecondMissing() {
    pairedZipPaths.remove(pairedZip2);
    secondPairedZipPaths.remove(secondPairedZip2);
    thirdPairedZipPaths.remove(thirdPairedZip2);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_PairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths,
        secondPairedZipPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_Unpaired() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_UnpairedZip() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedZipPaths, unpairedZipPaths,
        secondUnpairedZipPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_UnpairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedZipPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  public void validateSamples_NoFastq() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>());
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { samples.get(0).getName() }, locale));
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { samples.get(1).getName() }, locale));
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { samples.get(2).getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_NoFastqFistAndSecondSample() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>(), new ArrayList<>(),
        secondPairedPaths);
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { samples.get(0).getName() }, locale));
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { samples.get(1).getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_NoFastqThirdSample() {
    when(sampleService.files(any())).thenReturn(pairedPaths, pairedPaths, new ArrayList<>());
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { sample2.getName() }, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_FirstPairedSecondUnpairedThirdNoFastq() {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondUnpairedPaths,
        new ArrayList<>());
    service.validateSamples(samples, locale, errorHandler);
    verify(errorHandler).accept(messageSource.getMessage(MESSAGE_PREFIX + "sample.noFastq",
        new Object[] { samples.get(2).getName() }, locale));
    verify(errorHandler)
        .accept(messageSource.getMessage(MESSAGE_PREFIX + "samples.pairedMissmatch", null, locale));
    verify(errorHandler)
        .accept(messageSource.getMessage(MESSAGE_PREFIX + "samples.pairedMissmatch", null, locale));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(samples), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_Null() {
    assertThrows(NullPointerException.class, () -> {
      service.validateSamples((Collection<Sample>) null, locale, errorHandler);
    });
    verify(errorHandler, never()).accept(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateSamples_Empty() {
    service.validateSamples(new ArrayList<>(), locale, errorHandler);
    verify(errorHandler, never()).accept(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_NoSample() throws Throwable {
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    assertThrows(IllegalArgumentException.class, () -> {
      service.copyDatasetsResources(datasets);
    });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_NoSampleName() throws Throwable {
    paired1 = temporaryFolder.resolve("R1.fastq");
    paired2 = temporaryFolder.resolve("R2.fastq");
    pairedPaths.clear();
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_Symlinks() throws Throwable {
    assumeFalse(SystemUtils.IS_OS_WINDOWS); // Symbolic links don't work on Windows.
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_Zip() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    final byte[] fastq1Content = writeRandom(pairedZip1);
    final byte[] fastq2Content = writeRandom(pairedZip2);
    final byte[] fastq3Content = writeRandom(secondPairedZip1);
    final byte[] fastq4Content = writeRandom(secondPairedZip2);
    final byte[] fastq5Content = writeRandom(thirdPairedZip1);
    final byte[] fastq6Content = writeRandom(thirdPairedZip2);
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_Unpaired() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    final byte[] fastq1Content = writeRandom(unpaired);
    final byte[] fastq3Content = writeRandom(secondUnpaired);
    final byte[] fastq5Content = writeRandom(thirdUnpaired);
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertFalse(Files.exists(fastq6));
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_PairedWithBams() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
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
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_SymlinksWithBams() throws Throwable {
    assumeFalse(SystemUtils.IS_OS_WINDOWS); // Symbolic links don't work on Windows.
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
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
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertTrue(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertTrue(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path bam1 = folder.resolve(sample.getName() + ".bam");
    assertTrue(Files.exists(bam1));
    assertTrue(Files.isSymbolicLink(bam1));
    assertArrayEquals(bamContent, Files.readAllBytes(bam1));
    Path bam2 = folder.resolve(sample.getName() + "-test.bam");
    assertTrue(Files.exists(bam2));
    assertTrue(Files.isSymbolicLink(bam2));
    assertArrayEquals(bam2Content, Files.readAllBytes(bam2));
    Path bam3 = folder.resolve(sample.getName() + "-raw.bam");
    assertTrue(Files.exists(bam3));
    assertTrue(Files.isSymbolicLink(bam3));
    assertArrayEquals(rawbamContent, Files.readAllBytes(bam3));
    Path bam4 = folder.resolve(sample2.getName() + ".bam");
    assertTrue(Files.exists(bam4));
    assertTrue(Files.isSymbolicLink(bam4));
    assertArrayEquals(secondBamContent, Files.readAllBytes(bam4));
    Path bam5 = folder.resolve(sample2.getName() + "-test.bam");
    assertTrue(Files.exists(bam5));
    assertTrue(Files.isSymbolicLink(bam5));
    assertArrayEquals(secondBam2Content, Files.readAllBytes(bam5));
    Path bam6 = folder.resolve(sample2.getName() + "-raw.bam");
    assertTrue(Files.exists(bam6));
    assertTrue(Files.isSymbolicLink(bam6));
    assertArrayEquals(secondRawbamContent, Files.readAllBytes(bam6));
    Path bam7 = folder.resolve(sample3.getName() + ".bam");
    assertTrue(Files.exists(bam7));
    assertTrue(Files.isSymbolicLink(bam7));
    assertArrayEquals(thirdBamContent, Files.readAllBytes(bam7));
    Path bam8 = folder.resolve(sample3.getName() + "-test.bam");
    assertTrue(Files.exists(bam8));
    assertTrue(Files.isSymbolicLink(bam8));
    assertArrayEquals(thirdBam2Content, Files.readAllBytes(bam8));
    Path bam9 = folder.resolve(sample3.getName() + "-raw.bam");
    assertTrue(Files.exists(bam9));
    assertTrue(Files.isSymbolicLink(bam9));
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_UnpairedWithBams() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    final byte[] fastq1Content = writeRandom(unpaired);
    final byte[] fastq3Content = writeRandom(secondUnpaired);
    final byte[] fastq5Content = writeRandom(thirdUnpaired);
    final byte[] bamContent = writeRandom(bam);
    final byte[] bam2Content = writeRandom(bam2);
    final byte[] rawbamContent = writeRandom(rawbam);
    unpairedPaths.add(bam);
    unpairedPaths.add(bam2);
    unpairedPaths.add(rawbam);
    final byte[] secondBamContent = writeRandom(secondBam);
    final byte[] secondBam2Content = writeRandom(secondBam2);
    final byte[] secondRawbamContent = writeRandom(secondRawbam);
    secondUnpairedPaths.add(secondBam);
    secondUnpairedPaths.add(secondBam2);
    secondUnpairedPaths.add(secondRawbam);
    final byte[] thirdBamContent = writeRandom(thirdBam);
    final byte[] thirdBam2Content = writeRandom(thirdBam2);
    final byte[] thirdRawbamContent = writeRandom(thirdRawbam);
    thirdUnpairedPaths.add(thirdBam);
    thirdUnpairedPaths.add(thirdBam2);
    thirdUnpairedPaths.add(thirdRawbam);
    Path folder = service.copyDatasetsResources(datasets);
    assertEquals(configuration.getAnalysis().folder(datasets), folder);
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
    Sample sample3 = datasets.get(0).getSamples().get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertFalse(Files.exists(fastq6));
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_AlreadyExists() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
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

    Path copyFolder = service.copyDatasetsResources(datasets);

    assertEquals(folder, copyFolder);
    assertFalse(Files.exists(folder.resolve(extraFilename)));
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
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_Null() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      service.copyDatasetsResources((Collection<Dataset>) null);
    });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyDatasetsResources_Empty() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      service.copyDatasetsResources(new ArrayList<>());
    });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copySamplesResources() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_NoSampleName() throws Throwable {
    paired1 = temporaryFolder.resolve("R1.fastq");
    paired2 = temporaryFolder.resolve("R2.fastq");
    pairedPaths.clear();
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_Symlinks() throws Throwable {
    assumeFalse(SystemUtils.IS_OS_WINDOWS); // Symbolic links don't work on Windows.
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_Zip() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    final byte[] fastq1Content = writeRandom(pairedZip1);
    final byte[] fastq2Content = writeRandom(pairedZip2);
    final byte[] fastq3Content = writeRandom(secondPairedZip1);
    final byte[] fastq4Content = writeRandom(secondPairedZip2);
    final byte[] fastq5Content = writeRandom(thirdPairedZip1);
    final byte[] fastq6Content = writeRandom(thirdPairedZip2);
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_Unpaired() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    final byte[] fastq1Content = writeRandom(unpaired);
    final byte[] fastq3Content = writeRandom(secondUnpaired);
    final byte[] fastq5Content = writeRandom(thirdUnpaired);
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertFalse(Files.exists(fastq6));
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_PairedWithBams() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
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
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_SymlinksWithBams() throws Throwable {
    assumeFalse(SystemUtils.IS_OS_WINDOWS); // Symbolic links don't work on Windows.
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
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
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertTrue(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertTrue(Files.exists(fastq6));
    assertTrue(Files.isSymbolicLink(fastq6));
    assertArrayEquals(fastq6Content, Files.readAllBytes(fastq6));
    Path bam1 = folder.resolve(sample.getName() + ".bam");
    assertTrue(Files.exists(bam1));
    assertTrue(Files.isSymbolicLink(bam1));
    assertArrayEquals(bamContent, Files.readAllBytes(bam1));
    Path bam2 = folder.resolve(sample.getName() + "-test.bam");
    assertTrue(Files.exists(bam2));
    assertTrue(Files.isSymbolicLink(bam2));
    assertArrayEquals(bam2Content, Files.readAllBytes(bam2));
    Path bam3 = folder.resolve(sample.getName() + "-raw.bam");
    assertTrue(Files.exists(bam3));
    assertTrue(Files.isSymbolicLink(bam3));
    assertArrayEquals(rawbamContent, Files.readAllBytes(bam3));
    Path bam4 = folder.resolve(sample2.getName() + ".bam");
    assertTrue(Files.exists(bam4));
    assertTrue(Files.isSymbolicLink(bam4));
    assertArrayEquals(secondBamContent, Files.readAllBytes(bam4));
    Path bam5 = folder.resolve(sample2.getName() + "-test.bam");
    assertTrue(Files.exists(bam5));
    assertTrue(Files.isSymbolicLink(bam5));
    assertArrayEquals(secondBam2Content, Files.readAllBytes(bam5));
    Path bam6 = folder.resolve(sample2.getName() + "-raw.bam");
    assertTrue(Files.exists(bam6));
    assertTrue(Files.isSymbolicLink(bam6));
    assertArrayEquals(secondRawbamContent, Files.readAllBytes(bam6));
    Path bam7 = folder.resolve(sample3.getName() + ".bam");
    assertTrue(Files.exists(bam7));
    assertTrue(Files.isSymbolicLink(bam7));
    assertArrayEquals(thirdBamContent, Files.readAllBytes(bam7));
    Path bam8 = folder.resolve(sample3.getName() + "-test.bam");
    assertTrue(Files.exists(bam8));
    assertTrue(Files.isSymbolicLink(bam8));
    assertArrayEquals(thirdBam2Content, Files.readAllBytes(bam8));
    Path bam9 = folder.resolve(sample3.getName() + "-raw.bam");
    assertTrue(Files.exists(bam9));
    assertTrue(Files.isSymbolicLink(bam9));
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_UnpairedWithBams() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    final byte[] fastq1Content = writeRandom(unpaired);
    final byte[] fastq3Content = writeRandom(secondUnpaired);
    final byte[] fastq5Content = writeRandom(thirdUnpaired);
    final byte[] bamContent = writeRandom(bam);
    final byte[] bam2Content = writeRandom(bam2);
    final byte[] rawbamContent = writeRandom(rawbam);
    unpairedPaths.add(bam);
    unpairedPaths.add(bam2);
    unpairedPaths.add(rawbam);
    final byte[] secondBamContent = writeRandom(secondBam);
    final byte[] secondBam2Content = writeRandom(secondBam2);
    final byte[] secondRawbamContent = writeRandom(secondRawbam);
    secondUnpairedPaths.add(secondBam);
    secondUnpairedPaths.add(secondBam2);
    secondUnpairedPaths.add(secondRawbam);
    final byte[] thirdBamContent = writeRandom(thirdBam);
    final byte[] thirdBam2Content = writeRandom(thirdBam2);
    final byte[] thirdRawbamContent = writeRandom(thirdRawbam);
    thirdUnpairedPaths.add(thirdBam);
    thirdUnpairedPaths.add(thirdBam2);
    thirdUnpairedPaths.add(thirdRawbam);
    Path folder = service.copySamplesResources(samples);
    assertEquals(configuration.getAnalysis().folder(samples), folder);
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
    Sample sample3 = samples.get(0);
    Path fastq5 = folder.resolve(sample3.getName() + "_R1.fastq");
    assertTrue(Files.exists(fastq5));
    assertFalse(Files.isSymbolicLink(fastq5));
    assertArrayEquals(fastq5Content, Files.readAllBytes(fastq5));
    Path fastq6 = folder.resolve(sample3.getName() + "_R2.fastq");
    assertFalse(Files.exists(fastq6));
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_AlreadyExists() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
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

    Path copyFolder = service.copySamplesResources(samples);

    assertEquals(folder, copyFolder);
    assertFalse(Files.exists(folder.resolve(extraFilename)));
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
  @SuppressWarnings("unchecked")
  public void copySamplesResources_Null() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      service.copySamplesResources((Collection<Sample>) null);
    });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copySamplesResources_Empty() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      service.copySamplesResources(new ArrayList<>());
    });
  }
}
