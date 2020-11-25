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

package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class SampleServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private SampleService service;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private DatasetService datasetService;
  @MockBean
  private AppConfiguration configuration;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @MockBean
  private AuthorizationService authorizationService;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(configuration.folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null
          ? temporaryFolder.getRoot().toPath().resolve(sample.getName())
          : null;
    });
    when(configuration.folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.getRoot().toPath().resolve(dataset.getName())
          : null;
    });
  }

  @Test
  public void get() {
    Sample sample = service.get(1L);

    assertEquals((Long) 1L, sample.getId());
    assertEquals("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020", sample.getName());
    assertEquals("FR1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    assertTrue(sample.isEditable());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getCreationDate());
    assertEquals(LocalDate.of(2018, 10, 20), sample.getDate());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void get_Null() {
    Sample sample = service.get(null);
    assertNull(sample);
  }

  @Test
  public void exists_True() {
    assertTrue(service.exists("FR1_MNaseSeq_IP_polr2a_yFR100_WT_Rappa_R1_20181020"));
  }

  @Test
  public void exists_False() {
    assertFalse(service.exists("FR1_MNaseSeq_IP_polr2a_yFR100_WT_Rappa"));
  }

  @Test
  public void exists_Null() {
    assertFalse(service.exists(null));
  }

  @Test
  public void all() {
    List<Sample> samples = service.all();

    assertEquals(11, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
    assertTrue(find(samples, 4L).isPresent());
    assertTrue(find(samples, 5L).isPresent());
    assertTrue(find(samples, 6L).isPresent());
    assertTrue(find(samples, 7L).isPresent());
    assertTrue(find(samples, 8L).isPresent());
    assertTrue(find(samples, 9L).isPresent());
    assertTrue(find(samples, 10L).isPresent());
    assertTrue(find(samples, 11L).isPresent());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void files() throws Throwable {
    Sample sample = repository.findById(1L).orElse(null);
    Path folder = configuration.folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("sample_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("sample_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".deleted");
    Files.createFile(file);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }

    List<Path> files = service.files(sample);

    verify(configuration, times(2)).folder(sample);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_FolderNotExists() throws Throwable {
    Sample sample = repository.findById(1L).orElse(null);

    List<Path> files = service.files(sample);

    verify(configuration).folder(sample);
    assertTrue(files.isEmpty());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_NullId() throws Throwable {
    List<Path> files = service.files(new Sample());

    assertTrue(files.isEmpty());
  }

  @Test
  public void files_Null() throws Throwable {
    List<Path> files = service.files(null);

    assertTrue(files.isEmpty());
  }

  @Test
  public void isDeletable_FalseNotEditable() {
    Sample sample = repository.findById(9L).get();
    sample.setEditable(false);
    assertFalse(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void isDeletable_FalseLinkedToDataset() {
    Sample sample = repository.findById(1L).get();
    assertFalse(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void isDeletable_True() {
    Sample sample = repository.findById(9L).get();
    assertTrue(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void isDeletable_Null() {
    assertFalse(service.isDeletable(null));
  }

  @Test
  public void isDeletable_NullId() {
    assertFalse(service.isDeletable(new Sample()));
  }

  @Test
  public void isMergable_False() {
    List<Sample> samples = new ArrayList<>();
    samples.add(repository.findById(1L).get());
    samples.add(repository.findById(4L).get());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_True() {
    List<Sample> samples = new ArrayList<>();
    samples.add(repository.findById(1L).get());
    samples.add(repository.findById(2L).get());
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_AllNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(new Sample());
    samples.add(new Sample());
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_ProtocolTrue() {
    List<Sample> samples = new ArrayList<>();
    Protocol protocol = new Protocol(1L);
    Sample sample = new Sample();
    sample.setProtocol(protocol);
    samples.add(sample);
    sample = new Sample();
    sample.setProtocol(protocol);
    samples.add(sample);
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_ProtocolFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setProtocol(new Protocol(1L));
    samples.add(sample);
    sample = new Sample();
    sample.setProtocol(new Protocol(2L));
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_ProtocolOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setProtocol(new Protocol(1L));
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_AssayTrue() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setAssay(Assay.CHIP_SEQ);
    samples.add(sample);
    sample = new Sample();
    sample.setAssay(Assay.CHIP_SEQ);
    samples.add(sample);
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_AssayFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setAssay(Assay.CHIP_SEQ);
    samples.add(sample);
    sample = new Sample();
    sample.setAssay(Assay.CHIP_EXO);
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_AssayOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setAssay(Assay.CHIP_SEQ);
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_TypeTrue() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setType(SampleType.INPUT);
    samples.add(sample);
    sample = new Sample();
    sample.setType(SampleType.INPUT);
    samples.add(sample);
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_TypeFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setType(SampleType.INPUT);
    samples.add(sample);
    sample = new Sample();
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_TypeOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setType(SampleType.INPUT);
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_TargetTrue() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTarget("test");
    samples.add(sample);
    sample = new Sample();
    sample.setTarget("test");
    samples.add(sample);
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_TargetFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTarget("test");
    samples.add(sample);
    sample = new Sample();
    sample.setTarget("test2");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_TargetOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTarget("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_StrainTrue() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrain("test");
    samples.add(sample);
    sample = new Sample();
    sample.setStrain("test");
    samples.add(sample);
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_StrainFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrain("test");
    samples.add(sample);
    sample = new Sample();
    sample.setStrain("test2");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_StrainOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrain("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_StrainDescriptionTrue() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrainDescription("test");
    samples.add(sample);
    sample = new Sample();
    sample.setStrainDescription("test");
    samples.add(sample);
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_StrainDescriptionFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrainDescription("test");
    samples.add(sample);
    sample = new Sample();
    sample.setStrainDescription("test2");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_StrainDescriptionOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrainDescription("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_TreatmentTrue() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTreatment("test");
    samples.add(sample);
    sample = new Sample();
    sample.setTreatment("test");
    samples.add(sample);
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_TreatmentFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTreatment("test");
    samples.add(sample);
    sample = new Sample();
    sample.setTreatment("test2");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_TreatmentOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTreatment("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_TargetTrueTreatmentFalse() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTarget("test");
    sample.setTreatment("test");
    samples.add(sample);
    sample = new Sample();
    sample.setTarget("test");
    sample.setTreatment("test2");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_Empty() {
    assertFalse(service.isMergable(new ArrayList<>()));
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void isMergable_Anonymous() {
    assertFalse(service.isMergable(new ArrayList<>()));
  }

  @Test
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setProtocol(protocolRepository.findById(1L).get());
    sample.setDate(LocalDate.of(2020, 7, 21));

    service.save(sample);

    repository.flush();
    assertNotNull(sample.getId());
    sample = repository.findById(sample.getId()).orElse(null);
    assertEquals("my sample", sample.getSampleId());
    assertEquals("my replicate", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(user.getId(), sample.getOwner().getId());
    assertTrue(sample.isEditable());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getCreationDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getCreationDate()));
    assertEquals(LocalDate.of(2020, 7, 21), sample.getDate());
    assertEquals("mysample_ChIPseq_IP_mytarget_yFR213_F56G_37C_myreplicate_20200721",
        sample.getName());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  public void save_Update() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Sample sample = repository.findById(1L).orElse(null);
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(SampleType.INPUT);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setProtocol(protocolRepository.findById(3L).get());
    sample.setDate(LocalDate.of(2020, 7, 21));

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElse(null);
    assertEquals("my sample", sample.getSampleId());
    assertEquals("my replicate", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(SampleType.INPUT, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    assertTrue(sample.isEditable());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getCreationDate());
    assertEquals(LocalDate.of(2020, 7, 21), sample.getDate());
    assertEquals("mysample_ChIPseq_Input_mytarget_yFR213_F56G_37C_myreplicate_20200721",
        sample.getName());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  public void save_RenameDatasets() throws Throwable {
    Sample sample = repository.findById(4L).get();
    sample.setSampleId("sample1");
    sample.setReplicate("r1");
    Dataset dataset1 = datasetRepository.findById(2L).get();
    Path beforeFolder1 = configuration.folder(dataset1);
    Files.createDirectories(beforeFolder1);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder1.resolve("dataset_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Dataset dataset2 = datasetRepository.findById(6L).get();
    Path beforeFolder2 = configuration.folder(dataset2);
    Files.createDirectories(beforeFolder2);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder2.resolve("dataset_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);

    service.save(sample);

    repository.flush();
    Dataset dataset = datasetRepository.findById(2L).get();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_sample1-JS2_20181022", dataset.getName());
    assertEquals(2, dataset.getSamples().size());
    assertEquals((Long) 4L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    dataset = datasetRepository.findById(6L).get();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_sample1_20181208", dataset.getName());
    assertEquals(1, dataset.getSamples().size());
    assertEquals((Long) 4L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 12, 8, 10, 28, 23), dataset.getCreationDate());
    Path folder = configuration.folder(dataset1);
    assertTrue(Files.exists(folder.resolve("dataset_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R1.fastq")));
    assertFalse(Files.exists(beforeFolder1));
    folder = configuration.folder(dataset2);
    assertTrue(Files.exists(folder.resolve("dataset_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R2.fastq")));
    assertFalse(Files.exists(beforeFolder2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void save_UpdateNotEditable() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Sample sample = repository.findById(8L).orElse(null);
    service.save(sample);
  }

  @Test
  public void save_UpdateMoveFiles() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Sample sample = repository.findById(1L).orElse(null);
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    Path beforeFolder = configuration.folder(sample);
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("sample_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("sample_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElse(null);
    assertEquals("my sample", sample.getSampleId());
    assertEquals("my replicate", sample.getReplicate());
    Path folder = configuration.folder(sample);
    assertTrue(Files.exists(folder.resolve("sample_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("sample_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("sample_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("sample_R2.fastq")));
    assertFalse(Files.exists(beforeFolder));
  }

  @Test
  public void saveFiles() throws Throwable {
    final Sample sample = repository.findById(1L).orElse(null);
    List<Path> files = new ArrayList<>();
    Path file = temporaryFolder.newFile("sample_R1.fastq").toPath();
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    final FileTime filetime1 = Files.getLastModifiedTime(file);
    files.add(file);
    file = temporaryFolder.newFile("sample_R2.fastq").toPath();
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    final FileTime filetime2 = Files.getLastModifiedTime(file);
    files.add(file);
    Thread.sleep(1000); // Allows to test file modification time.

    service.saveFiles(sample, files);

    verify(configuration).folder(sample);
    Path folder = configuration.folder(sample);
    assertTrue(Files.exists(folder.resolve("sample_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("sample_R1.fastq")));
    assertTrue(
        filetime1.compareTo(Files.getLastModifiedTime(folder.resolve("sample_R1.fastq"))) < 0);
    assertTrue(Files.exists(folder.resolve("sample_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("sample_R2.fastq")));
    assertTrue(
        filetime2.compareTo(Files.getLastModifiedTime(folder.resolve("sample_R2.fastq"))) < 0);
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  public void delete() throws Throwable {
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.folder(sample);
    Path file = folder.resolve("R1.fastq");
    Files.createDirectories(folder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);

    service.delete(sample);

    repository.flush();
    assertFalse(repository.findById(9L).isPresent());
    assertFalse(Files.exists(folder));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test(expected = IllegalArgumentException.class)
  public void delete_LinkedToDataset() {
    Sample sample = repository.findById(1L).get();
    service.delete(sample);
  }

  @Test(expected = IllegalArgumentException.class)
  public void delete_NotEditable() {
    Sample sample = repository.findById(9L).get();
    sample.setEditable(false);
    service.delete(sample);
  }

  @Test
  public void deleteFile_FullPath() throws Throwable {
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));

    service.deleteFile(sample, file);

    verify(configuration, times(2)).folder(sample);
    assertFalse(Files.exists(file));
    Path deleted = folder.resolve(".deleted");
    List<String> deletedLines = Files.readAllLines(deleted);
    String[] deletedFileColumns = deletedLines.get(deletedLines.size() - 1).split("\t", -1);
    assertEquals(3, deletedFileColumns.length);
    assertEquals("test.txt", deletedFileColumns[0]);
    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    assertEquals(modifiedTime, LocalDateTime.from(formatter.parse(deletedFileColumns[1])));
    LocalDateTime deletedTime = LocalDateTime.from(formatter.parse(deletedFileColumns[2]));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(deletedTime));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(deletedTime));
  }

  @Test
  public void deleteFile_RelativePath() throws Throwable {
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.folder(sample);
    Files.createDirectories(folder);
    Path file = Paths.get("test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), folder.resolve(file),
        StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(folder.resolve(file), FileTime.from(toInstant(modifiedTime)));

    service.deleteFile(sample, file);

    verify(configuration, times(2)).folder(sample);
    assertFalse(Files.exists(file));
    Path deleted = folder.resolve(".deleted");
    List<String> deletedLines = Files.readAllLines(deleted);
    String[] deletedFileColumns = deletedLines.get(deletedLines.size() - 1).split("\t", -1);
    assertEquals(3, deletedFileColumns.length);
    assertEquals("test.txt", deletedFileColumns[0]);
    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    assertEquals(modifiedTime, LocalDateTime.from(formatter.parse(deletedFileColumns[1])));
    LocalDateTime deletedTime = LocalDateTime.from(formatter.parse(deletedFileColumns[2]));
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(deletedTime));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(deletedTime));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteFile_FullPathNotInSampleFolder() throws Throwable {
    Sample sample = repository.findById(9L).get();
    Path file = temporaryFolder.getRoot().toPath().resolve("test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    Files.setLastModifiedTime(file,
        FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

    service.deleteFile(sample, file);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteFile_RelativePathNotInSampleFolder() throws Throwable {
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.folder(sample);
    Files.createDirectories(folder);
    Path file = Paths.get("../test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    Files.setLastModifiedTime(file,
        FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

    service.deleteFile(sample, file);
  }
}
