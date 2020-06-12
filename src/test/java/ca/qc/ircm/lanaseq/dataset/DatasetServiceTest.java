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

package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
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
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
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
public class DatasetServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private DatasetService service;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private AppConfiguration configuration;
  @MockBean
  private AuthorizationService authorizationService;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(configuration.folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.getRoot().toPath().resolve(dataset.getName())
          : null;
    });
  }

  @Test
  @WithMockUser
  public void get() {
    Dataset dataset = service.get(1L);

    assertEquals((Long) 1L, dataset.getId());
    assertEquals("MNaseSeq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020", dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(3, dataset.getSamples().size());
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 2L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getSamples().get(2).getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Null() {
    Dataset dataset = service.get(null);
    assertNull(dataset);
  }

  @Test
  @WithMockUser
  public void all() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);

    List<Dataset> datasets = service.all();

    assertEquals(5, datasets.size());
    assertTrue(find(datasets, 1L).isPresent());
    assertTrue(find(datasets, 2L).isPresent());
    assertTrue(find(datasets, 3L).isPresent());
    assertTrue(find(datasets, 4L).isPresent());
    assertTrue(find(datasets, 5L).isPresent());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void files() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("dataset_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("dataset_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);

    List<Path> files = service.files(dataset);

    verify(configuration, times(2)).folder(dataset);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    assertTrue(files.contains(folder.resolve("dataset_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void files_FolderNotExists() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);

    List<Path> files = service.files(dataset);

    verify(configuration).folder(dataset);
    assertTrue(files.isEmpty());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void files_NullId() throws Throwable {
    List<Path> files = service.files(new Dataset());

    assertTrue(files.isEmpty());
  }

  @Test
  @WithMockUser
  public void files_Null() throws Throwable {
    List<Path> files = service.files(null);

    assertTrue(files.isEmpty());
  }

  @Test
  @WithMockUser
  public void topTags() {
    List<String> tags = service.topTags(3);
    assertEquals(3, tags.size());
    assertTrue(tags.contains("ip"));
    assertTrue(tags.contains("chipseq"));
    assertTrue(tags.contains("G24D"));
  }

  @Test
  @WithMockUser
  @Ignore("Never false for a database instance")
  public void isDeletable_False() {
    Dataset dataset = repository.findById(1L).get();
    assertFalse(service.isDeletable(dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void isDeletable_True() {
    Dataset dataset = repository.findById(5L).get();
    assertTrue(service.isDeletable(dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  @WithMockUser
  public void isDeletable_Null() {
    assertFalse(service.isDeletable(null));
  }

  @Test
  @WithMockUser
  public void isDeletable_NullId() {
    assertFalse(service.isDeletable(new Dataset()));
  }

  @Test
  @WithMockUser
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Dataset dataset = new Dataset();
    dataset.setTags(new HashSet<>());
    dataset.getTags().add("tag1");
    dataset.getTags().add("tag2");
    dataset.setSamples(new ArrayList<>());
    Sample sample1 = new Sample();
    sample1.setSampleId("sample1");
    sample1.setReplicate("r1");
    sample1.setAssay(Assay.CHIP_SEQ);
    sample1.setType(SampleType.IMMUNO_PRECIPITATION);
    sample1.setTarget("my target");
    sample1.setStrain("yFR213");
    sample1.setStrainDescription("F56G");
    sample1.setTreatment("37C");
    sample1.setProtocol(protocolRepository.findById(1L).get());
    dataset.getSamples().add(sample1);
    Sample sample2 = new Sample();
    sample2.setSampleId("sample2");
    sample2.setReplicate("r2");
    sample2.setAssay(Assay.CHIP_SEQ);
    sample2.setType(SampleType.IMMUNO_PRECIPITATION);
    sample2.setTarget("my target");
    sample2.setStrain("yFR213");
    sample2.setStrainDescription("F56G");
    sample2.setTreatment("37C");
    sample2.setProtocol(protocolRepository.findById(1L).get());
    dataset.getSamples().add(sample2);

    service.save(dataset);

    repository.flush();
    assertNotNull(dataset.getId());
    dataset = repository.findById(dataset.getId()).orElse(null);
    assertEquals("ChIPSeq_IP_mytarget_yFR213_F56G_37C_sample1-sample2_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(dataset.getDate()), dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("tag1"));
    assertTrue(dataset.getTags().contains("tag2"));
    assertEquals(user.getId(), dataset.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(dataset.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(dataset.getDate()));
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertNotNull(sample.getId());
    assertEquals("sample1", sample.getSampleId());
    assertEquals("r1", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    assertEquals("sample1_ChIPSeq_IP_mytarget_yFR213_F56G_37C_r1_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(sample.getDate()), sample.getName());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(user.getId(), sample.getOwner().getId());
    sample = dataset.getSamples().get(1);
    assertNotNull(sample.getId());
    assertEquals("sample2", sample.getSampleId());
    assertEquals("r2", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    assertEquals("sample2_ChIPSeq_IP_mytarget_yFR213_F56G_37C_r2_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(sample.getDate()), sample.getName());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(user.getId(), sample.getOwner().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_Update() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Dataset dataset = repository.findById(1L).orElse(null);
    dataset.getTags().remove("rappa");
    dataset.getTags().add("tag1");
    Sample sample1 = dataset.getSamples().get(0);
    sample1.setSampleId("sample1");
    sample1.setReplicate("r1");
    sample1.setAssay(Assay.CHIP_SEQ);
    sample1.setType(SampleType.INPUT);
    sample1.setTarget("my target");
    sample1.setStrain("yFR213");
    sample1.setStrainDescription("F56G");
    sample1.setTreatment("37C");
    sample1.setProtocol(protocolRepository.findById(3L).get());
    dataset.getSamples().remove(1);
    Sample sample3 = new Sample();
    sample3.setSampleId("sample4");
    sample3.setReplicate("r4");
    sample3.setAssay(Assay.CHIP_SEQ);
    sample3.setType(SampleType.INPUT);
    sample3.setTarget("my target");
    sample3.setStrain("yFR213");
    sample3.setStrainDescription("F56G");
    sample3.setTreatment("37C");
    sample3.setProtocol(protocolRepository.findById(3L).get());
    dataset.getSamples().add(sample3);

    service.save(dataset);

    repository.flush();
    dataset = repository.findById(1L).orElse(null);
    assertEquals("ChIPSeq_Input_mytarget_yFR213_F56G_37C_sample1-FR3-sample4_20181020",
        dataset.getName());
    assertEquals(3, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertTrue(dataset.getTags().contains("tag1"));
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getDate());
    assertEquals(3, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals((Long) 1L, sample.getId());
    assertEquals("sample1", sample.getSampleId());
    assertEquals("r1", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(SampleType.INPUT, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getDate());
    assertEquals("sample1_ChIPSeq_Input_mytarget_yFR213_F56G_37C_r1_20181020", sample.getName());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    sample = dataset.getSamples().get(1);
    assertEquals((Long) 3L, sample.getId());
    assertEquals("FR3", sample.getSampleId());
    assertEquals("R3", sample.getReplicate());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 30, 23), sample.getDate());
    assertEquals("FR3_MNaseSeq_IP_polr2a_yFR100_WT_Rappa_R3_20181020", sample.getName());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    sample = dataset.getSamples().get(2);
    assertNotNull(sample.getId());
    assertEquals("sample4", sample.getSampleId());
    assertEquals("r4", sample.getReplicate());
    assertEquals(Assay.CHIP_SEQ, sample.getAssay());
    assertEquals(SampleType.INPUT, sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    assertEquals("sample4_ChIPSeq_Input_mytarget_yFR213_F56G_37C_r4_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(sample.getDate()), sample.getName());
    assertEquals((Long) 3L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_UpdateMoveFiles() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Dataset dataset = repository.findById(1L).orElse(null);
    Sample sample1 = dataset.getSamples().get(0);
    sample1.setSampleId("sample1");
    sample1.setReplicate("r1");
    sample1.setAssay(Assay.CHIP_SEQ);
    sample1.setType(SampleType.INPUT);
    sample1.setTarget("my target");
    sample1.setStrain("yFR213");
    sample1.setStrainDescription("F56G");
    sample1.setTreatment("37C");
    sample1.setProtocol(protocolRepository.findById(3L).get());
    dataset.getSamples().remove(1);
    Sample sample3 = new Sample();
    sample3.setSampleId("sample4");
    sample3.setReplicate("r4");
    sample3.setAssay(Assay.CHIP_SEQ);
    sample3.setType(SampleType.INPUT);
    sample3.setTarget("my target");
    sample3.setStrain("yFR213");
    sample3.setStrainDescription("F56G");
    sample3.setTreatment("37C");
    sample3.setProtocol(protocolRepository.findById(3L).get());
    dataset.getSamples().add(sample3);
    Path beforeFolder = configuration.folder(dataset);
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("dataset_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("dataset_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);

    service.save(dataset);

    repository.flush();
    dataset = repository.findById(1L).orElse(null);
    assertEquals("ChIPSeq_Input_mytarget_yFR213_F56G_37C_sample1-FR3-sample4_20181020",
        dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getDate());
    assertEquals(3, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals((Long) 1L, sample.getId());
    assertEquals("sample1", sample.getSampleId());
    assertEquals("r1", sample.getReplicate());
    sample = dataset.getSamples().get(1);
    assertEquals((Long) 3L, sample.getId());
    assertEquals("FR3", sample.getSampleId());
    assertEquals("R3", sample.getReplicate());
    sample = dataset.getSamples().get(2);
    assertNotNull(sample.getId());
    assertEquals("sample4", sample.getSampleId());
    assertEquals("r4", sample.getReplicate());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
    Path folder = configuration.folder(dataset);
    assertTrue(Files.exists(folder.resolve("dataset_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("dataset_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R2.fastq")));
    assertFalse(Files.exists(beforeFolder));
  }

  @Test
  @WithMockUser
  public void saveFiles() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    List<Path> files = new ArrayList<>();
    Path file = temporaryFolder.newFile("dataset_R1.fastq").toPath();
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    files.add(file);
    file = temporaryFolder.newFile("dataset_R2.fastq").toPath();
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    files.add(file);

    service.saveFiles(dataset, files);

    verify(configuration).folder(dataset);
    Path folder = configuration.folder(dataset);
    assertTrue(Files.exists(folder.resolve("dataset_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("dataset_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void delete() throws Throwable {
    Dataset dataset = repository.findById(5L).get();
    Path folder = configuration.folder(dataset);
    Path file = folder.resolve("R1.fastq");
    Files.createDirectories(folder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);

    service.delete(dataset);

    repository.flush();
    assertFalse(repository.findById(5L).isPresent());
    assertFalse(Files.exists(folder));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  @Ignore("Never false for a database instance")
  public void delete_NotDeletable() {
    Dataset dataset = repository.findById(1L).get();
    service.delete(dataset);
  }
}
