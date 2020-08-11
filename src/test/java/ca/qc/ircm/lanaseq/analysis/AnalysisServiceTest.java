package ca.qc.ircm.lanaseq.analysis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
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
  private DatasetAnalysis datasetAnalysis = new DatasetAnalysis();
  private Random random = new Random();

  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    dataset = datasetRepository.findById(2L).get();
    sample = sampleRepository.findById(4L).get();
    sample2 = sampleRepository.findById(5L).get();
    paired1 = Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R1.fastq");
    paired2 = Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R2.fastq");
    pairedPaths.add(Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    pairedPaths.add(paired1);
    pairedPaths.add(paired2);
    pairedZip1 = Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R1.fastq.gz");
    pairedZip2 = Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022_R2.fastq.gz");
    pairedZipPaths.add(Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    pairedZipPaths.add(pairedZip1);
    pairedZipPaths.add(pairedZip2);
    unpaired = Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.fastq");
    unpairedPaths.add(Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    unpairedPaths.add(unpaired);
    unpairedZip = Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.fastq.gz");
    unpairedZipPaths.add(Paths.get("JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022.bed"));
    unpairedZipPaths.add(unpairedZip);
    secondPaired1 = Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R1.fastq");
    secondPaired2 = Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R2.fastq");
    secondPairedPaths.add(Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondPairedPaths.add(secondPaired1);
    secondPairedPaths.add(secondPaired2);
    secondPairedZip1 = Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R1.fastq.gz");
    secondPairedZip2 = Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022_R2.fastq.gz");
    secondPairedZipPaths.add(Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondPairedZipPaths.add(secondPairedZip1);
    secondPairedZipPaths.add(secondPairedZip2);
    secondUnpaired = Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.fastq");
    secondUnpairedPaths.add(Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondUnpairedPaths.add(secondUnpaired);
    secondUnpairedZip = Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.fastq.gz");
    secondUnpairedZipPaths.add(Paths.get("JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022.bed"));
    secondUnpairedZipPaths.add(secondUnpairedZip);
    when(configuration.analysis(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.getRoot().toPath().resolve(dataset.getName())
          : null;
    });
    Path folder = temporaryFolder.getRoot().toPath();
    datasetAnalysis.dataset = dataset;
    datasetAnalysis.samples = new ArrayList<>();
    SampleAnalysis sampleAnalysis = new SampleAnalysis();
    sampleAnalysis.sample = dataset.getSamples().get(0);
    sampleAnalysis.paired = true;
    sampleAnalysis.fastq1 = folder.resolve(paired1);
    sampleAnalysis.fastq2 = folder.resolve(paired2);
    datasetAnalysis.samples.add(sampleAnalysis);
    sampleAnalysis = new SampleAnalysis();
    sampleAnalysis.sample = dataset.getSamples().get(1);
    sampleAnalysis.paired = true;
    sampleAnalysis.fastq1 = folder.resolve(secondPaired1);
    sampleAnalysis.fastq2 = folder.resolve(secondPaired2);
    datasetAnalysis.samples.add(sampleAnalysis);
  }

  private byte[] writeRandom(Path file) throws IOException {
    byte[] content = new byte[2048];
    random.nextBytes(content);
    Files.write(file, content, StandardOpenOption.CREATE);
    return content;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPaired() {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(paired1, sampleAnalysis.fastq1);
    assertEquals(paired2, sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(secondPaired1, sampleAnalysis.fastq1);
    assertEquals(secondPaired2, sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedFirstMissing() {
    pairedPaths.remove(paired1);
    secondPairedPaths.remove(secondPaired1);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(paired2, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondPaired2, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedFirstMissingFirstSample() {
    pairedPaths.remove(paired1);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(paired2, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(secondPaired1, sampleAnalysis.fastq1);
    assertEquals(secondPaired2, sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedSecondMissing() {
    pairedPaths.remove(paired2);
    secondPairedPaths.remove(secondPaired2);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(paired1, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondPaired1, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedSecondMissingFirstSample() {
    pairedPaths.remove(paired2);
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(paired1, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(secondPaired1, sampleAnalysis.fastq1);
    assertEquals(secondPaired2, sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedZip() {
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(pairedZip1, sampleAnalysis.fastq1);
    assertEquals(pairedZip2, sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(secondPairedZip1, sampleAnalysis.fastq1);
    assertEquals(secondPairedZip2, sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedZipFirstMissing() {
    pairedZipPaths.remove(pairedZip1);
    secondPairedZipPaths.remove(secondPairedZip1);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(pairedZip2, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondPairedZip2, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedZipFirstMissingFirstSample() {
    pairedZipPaths.remove(pairedZip1);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(pairedZip2, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(secondPairedZip1, sampleAnalysis.fastq1);
    assertEquals(secondPairedZip2, sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedZipSecondMissing() {
    pairedZipPaths.remove(pairedZip2);
    secondPairedZipPaths.remove(secondPairedZip2);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(pairedZip1, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondPairedZip1, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedZipSecondMissingFirstSample() {
    pairedZipPaths.remove(pairedZip2);
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondPairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(pairedZip1, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(secondPairedZip1, sampleAnalysis.fastq1);
    assertEquals(secondPairedZip2, sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondPairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(paired1, sampleAnalysis.fastq1);
    assertEquals(paired2, sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(secondPairedZip1, sampleAnalysis.fastq1);
    assertEquals(secondPairedZip2, sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetUnpaired() {
    when(sampleService.files(any())).thenReturn(unpairedPaths, secondUnpairedPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(unpaired, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondUnpaired, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(pairedPaths, secondUnpairedPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(paired1, sampleAnalysis.fastq1);
    assertEquals(paired2, sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondUnpaired, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetUnpairedZip() {
    when(sampleService.files(any())).thenReturn(unpairedZipPaths, secondUnpairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(unpairedZip, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondUnpairedZip, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetPairedZipSecondSampleUnpaired() {
    when(sampleService.files(any())).thenReturn(pairedZipPaths, secondUnpairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertTrue(sampleAnalysis.paired);
    assertEquals(pairedZip1, sampleAnalysis.fastq1);
    assertEquals(pairedZip2, sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondUnpairedZip, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void metadata_DatasetUnpairedSecondSampleZip() {
    when(sampleService.files(any())).thenReturn(unpairedPaths, secondUnpairedZipPaths);
    DatasetAnalysis analysis = service.metadata(dataset);
    assertEquals(dataset, analysis.dataset);
    assertEquals(2, analysis.samples.size());
    SampleAnalysis sampleAnalysis = analysis.samples.get(0);
    assertEquals(sample, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(unpaired, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    sampleAnalysis = analysis.samples.get(1);
    assertEquals(sample2, sampleAnalysis.sample);
    assertFalse(sampleAnalysis.paired);
    assertEquals(secondUnpairedZip, sampleAnalysis.fastq1);
    assertNull(sampleAnalysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test(expected = IllegalArgumentException.class)
  public void metadata_DatasetNoFastq() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>());
    service.metadata(dataset);
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("unchecked")
  public void metadata_DatasetSecondSampleNoFastq() {
    when(sampleService.files(any())).thenReturn(pairedPaths, new ArrayList<>());
    service.metadata(dataset);
  }

  @Test
  public void metadata_SamplePaired() {
    when(sampleService.files(any())).thenReturn(pairedPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertTrue(analysis.paired);
    assertEquals(paired1, analysis.fastq1);
    assertEquals(paired2, analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void metadata_SamplePairedFirstMissing() {
    pairedPaths.remove(paired1);
    when(sampleService.files(any())).thenReturn(pairedPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertFalse(analysis.paired);
    assertEquals(paired2, analysis.fastq1);
    assertNull(analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void metadata_SamplePairedSecondMissing() {
    pairedPaths.remove(paired2);
    when(sampleService.files(any())).thenReturn(pairedPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertFalse(analysis.paired);
    assertEquals(paired1, analysis.fastq1);
    assertNull(analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void metadata_SamplePairedZip() {
    when(sampleService.files(any())).thenReturn(pairedZipPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertTrue(analysis.paired);
    assertEquals(pairedZip1, analysis.fastq1);
    assertEquals(pairedZip2, analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void metadata_SamplePairedZipFirstMissing() {
    pairedZipPaths.remove(pairedZip1);
    when(sampleService.files(any())).thenReturn(pairedZipPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertFalse(analysis.paired);
    assertEquals(pairedZip2, analysis.fastq1);
    assertNull(analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void metadata_SamplePairedZipSecondMissing() {
    pairedZipPaths.remove(pairedZip2);
    when(sampleService.files(any())).thenReturn(pairedZipPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertFalse(analysis.paired);
    assertEquals(pairedZip1, analysis.fastq1);
    assertNull(analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void metadata_SampleUnpaired() {
    when(sampleService.files(any())).thenReturn(unpairedPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertFalse(analysis.paired);
    assertEquals(unpaired, analysis.fastq1);
    assertNull(analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void metadata_SampleUnpairedZip() {
    when(sampleService.files(any())).thenReturn(unpairedZipPaths);
    SampleAnalysis analysis = service.metadata(sample);
    assertEquals(sample, analysis.sample);
    assertFalse(analysis.paired);
    assertEquals(unpairedZip, analysis.fastq1);
    assertNull(analysis.fastq2);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test(expected = IllegalArgumentException.class)
  public void metadata_SampleNoFastq() {
    when(sampleService.files(any())).thenReturn(new ArrayList<>());
    service.metadata(sample);
  }

  @Test
  public void copyResources_Dataset() throws Throwable {
    byte[] fastq1Content = writeRandom(datasetAnalysis.samples.get(0).fastq1);
    byte[] fastq2Content = writeRandom(datasetAnalysis.samples.get(0).fastq2);
    byte[] fastq3Content = writeRandom(datasetAnalysis.samples.get(1).fastq1);
    byte[] fastq4Content = writeRandom(datasetAnalysis.samples.get(1).fastq2);
    Path folder = service.copyResources(datasetAnalysis);
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
  public void copyResources_DatasetSymlinks() throws Throwable {
    when(configuration.isAnalysisSymlinks()).thenReturn(true);
    byte[] fastq1Content = writeRandom(datasetAnalysis.samples.get(0).fastq1);
    byte[] fastq2Content = writeRandom(datasetAnalysis.samples.get(0).fastq2);
    byte[] fastq3Content = writeRandom(datasetAnalysis.samples.get(1).fastq1);
    byte[] fastq4Content = writeRandom(datasetAnalysis.samples.get(1).fastq2);
    Path folder = service.copyResources(datasetAnalysis);
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
  public void copyResources_DatasetZip() throws Throwable {
    datasetAnalysis.samples.get(0).fastq1 = pairedZip1;
    datasetAnalysis.samples.get(0).fastq2 = pairedZip2;
    datasetAnalysis.samples.get(1).fastq1 = secondPairedZip1;
    datasetAnalysis.samples.get(1).fastq2 = secondPairedZip2;
    byte[] fastq1Content = writeRandom(datasetAnalysis.samples.get(0).fastq1);
    byte[] fastq2Content = writeRandom(datasetAnalysis.samples.get(0).fastq2);
    byte[] fastq3Content = writeRandom(datasetAnalysis.samples.get(1).fastq1);
    byte[] fastq4Content = writeRandom(datasetAnalysis.samples.get(1).fastq2);
    Path folder = service.copyResources(datasetAnalysis);
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
  public void copyResources_DatasetUnpaired() throws Throwable {
    datasetAnalysis.samples.get(0).paired = false;
    datasetAnalysis.samples.get(0).fastq2 = null;
    datasetAnalysis.samples.get(1).paired = false;
    datasetAnalysis.samples.get(1).fastq2 = null;
    byte[] fastq1Content = writeRandom(datasetAnalysis.samples.get(0).fastq1);
    byte[] fastq3Content = writeRandom(datasetAnalysis.samples.get(1).fastq1);
    Path folder = service.copyResources(datasetAnalysis);
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
