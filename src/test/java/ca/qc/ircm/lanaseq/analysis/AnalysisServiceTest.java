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

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
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
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link AnalysisService}.
 */
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
  private PermissionEvaluatorDelegator permissionEvaluator;
  @Mock
  private Consumer<String> errorHandler;
  @TempDir
  Path temporaryFolder;
  private List<Dataset> datasets = new ArrayList<>();
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
  private AppResources resources = new AppResources(AnalysisService.class, locale);

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
    when(configuration.analysis(any(Collection.class))).then(i -> {
      Collection<Dataset> datasets = i.getArgument(0);
      return datasets != null && !datasets.isEmpty() && datasets.iterator().next().getName() != null
          ? temporaryFolder.resolve(datasets.iterator().next().getName())
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
  public void validate_DatasetsNoSample() {
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    service.validate(datasets, locale, errorHandler);
    datasets.stream().forEach(
        ds -> verify(errorHandler).accept(resources.message("dataset.noSample", ds.getName())));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedMissmatch() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, pairedPaths, secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("datasets.pairedMissmatch"));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPaired() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedNoSampleName() {
    paired1 = temporaryFolder.resolve("R1.fastq");
    paired2 = temporaryFolder.resolve("R2.fastq");
    pairedPaths.clear();
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedFirstMissing() {
    pairedPaths.remove(paired1);
    secondPairedPaths.remove(secondPaired1);
    thirdPairedPaths.remove(thirdPaired1);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedFirstMissingFirstSample() {
    pairedPaths.remove(paired1);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedSecondMissing() {
    pairedPaths.remove(paired2);
    secondPairedPaths.remove(secondPaired2);
    thirdPairedPaths.remove(thirdPaired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedSecondMissingFirstSample() {
    pairedPaths.remove(paired2);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedZipMissmatch() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedZipPaths, pairedZipPaths,
        pairedZipPaths, secondPairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("datasets.pairedMissmatch"));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedZip() {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedZipFirstMissing() {
    pairedZipPaths.remove(pairedZip1);
    secondPairedZipPaths.remove(secondPairedZip1);
    thirdPairedZipPaths.remove(thirdPairedZip1);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedZipFirstMissingFirstSample() {
    pairedZipPaths.remove(pairedZip1);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedZipSecondMissing() {
    pairedZipPaths.remove(pairedZip2);
    secondPairedZipPaths.remove(secondPairedZip2);
    thirdPairedZipPaths.remove(thirdPairedZip2);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedZipSecondMissingFirstSample() {
    pairedZipPaths.remove(pairedZip2);
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths,
        secondPairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsUnpaired() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondUnpairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsUnpairedZip() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedZipPaths, unpairedZipPaths,
        secondUnpairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsPairedZipSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondUnpairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("dataset.pairedMissmatch", dataset.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsUnpairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedZipPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler, never()).accept(any());
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  public void validate_DatasetsNoFastq() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>());
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", datasets.get(0).getSamples().get(0).getName()));
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", datasets.get(1).getSamples().get(0).getName()));
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", datasets.get(1).getSamples().get(1).getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsNoFastqFistSample() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>(), new ArrayList<>(),
        secondPairedPaths);
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", datasets.get(0).getSamples().get(0).getName()));
    verify(errorHandler)
        .accept(resources.message("sample.noFastq", datasets.get(1).getSamples().get(0).getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsNoFastqSecondSample() {
    when(sampleService.files(any())).thenReturn(pairedPaths, pairedPaths, new ArrayList<>());
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("sample.noFastq", sample2.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_DatasetsFirstPairedSecondUnpairedThirdNoFastq() {
    Sample sample = sampleRepository.findById(1L).get();
    dataset.getSamples().add(sample);
    when(sampleService.files(any())).thenReturn(pairedPaths, pairedPaths, secondUnpairedPaths,
        new ArrayList<>());
    service.validate(datasets, locale, errorHandler);
    verify(errorHandler).accept(resources.message("sample.noFastq", sample.getName()));
    verifyNoMoreInteractions(errorHandler);
    verify(permissionEvaluator).hasCollectionPermission(any(), eq(datasets), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_NullDatasets() {
    assertThrows(NullPointerException.class, () -> {
      service.validate((Collection<Dataset>) null, locale, errorHandler);
    });
    verify(errorHandler, never()).accept(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_EmptyDatasets() {
    service.validate(new ArrayList<>(), locale, errorHandler);
    verify(errorHandler, never()).accept(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_DatasetsNoSample() throws Throwable {
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    assertThrows(IllegalArgumentException.class, () -> {
      service.copyResources(datasets);
    });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_Datasets() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsNoSampleName() throws Throwable {
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
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsSymlinks() throws Throwable {
    assumeFalse(SystemUtils.IS_OS_WINDOWS); // Symbolic links don't work on Windows.
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsZip() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedZipPaths, pairedZipPaths,
        secondPairedZipPaths);
    final byte[] fastq1Content = writeRandom(pairedZip1);
    final byte[] fastq2Content = writeRandom(pairedZip2);
    final byte[] fastq3Content = writeRandom(secondPairedZip1);
    final byte[] fastq4Content = writeRandom(secondPairedZip2);
    final byte[] fastq5Content = writeRandom(thirdPairedZip1);
    final byte[] fastq6Content = writeRandom(thirdPairedZip2);
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsUnpaired() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdUnpairedPaths, unpairedPaths,
        secondUnpairedPaths);
    final byte[] fastq1Content = writeRandom(unpaired);
    final byte[] fastq3Content = writeRandom(secondUnpaired);
    final byte[] fastq5Content = writeRandom(thirdUnpaired);
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsPairedWithBams() throws Throwable {
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
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsSymlinksWithBams() throws Throwable {
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
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsUnpairedWithBams() throws Throwable {
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
    Path folder = service.copyResources(datasets);
    assertEquals(configuration.analysis(datasets), folder);
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
  public void copyResources_DatasetsAlreadyExists() throws Throwable {
    when(sampleService.files(any())).thenReturn(thirdPairedPaths, pairedPaths, secondPairedPaths);
    final byte[] fastq1Content = writeRandom(paired1);
    final byte[] fastq2Content = writeRandom(paired2);
    final byte[] fastq3Content = writeRandom(secondPaired1);
    final byte[] fastq4Content = writeRandom(secondPaired2);
    final byte[] fastq5Content = writeRandom(thirdPaired1);
    final byte[] fastq6Content = writeRandom(thirdPaired2);
    Path folder = configuration.analysis(datasets);
    String extraFilename = "test.bam";
    Files.createDirectories(folder);
    Files.write(folder.resolve(paired1.getFileName()), fastq2Content);
    Files.write(folder.resolve(extraFilename), fastq1Content);

    Path copyFolder = service.copyResources(datasets);

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
  public void copyResources_NullDatasets() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      service.copyResources((Collection<Dataset>) null);
    });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void copyResources_EmptyDatasets() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      service.copyResources(new ArrayList<>());
    });
  }
}
