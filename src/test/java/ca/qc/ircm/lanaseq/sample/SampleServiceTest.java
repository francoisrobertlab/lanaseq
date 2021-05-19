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

import static ca.qc.ircm.lanaseq.sample.QSample.sample;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ID;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.time.TimeConverter.toInstant;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import java.nio.charset.StandardCharsets;
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
import java.util.Optional;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link SampleService}.
 */
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
  @Autowired
  private EntityManager entityManager;
  @MockBean
  private DatasetService datasetService;
  @MockBean
  private AppConfiguration configuration;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @MockBean
  private AuthorizationService authorizationService;
  @TempDir
  Path temporaryFolder;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(configuration.folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null ? temporaryFolder.resolve(sample.getName())
          : null;
    });
    when(configuration.upload(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null ? temporaryFolder.resolve(sample.getName())
          : null;
    });
    when(configuration.getUpload()).then(i -> {
      return temporaryFolder.resolve("upload");
    });
    when(configuration.folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve(dataset.getName())
          : null;
    });
  }

  private void detach(Sample sample) {
    entityManager.detach(sample);
  }

  @Test
  public void get() {
    Sample sample = service.get(1L).orElse(null);

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
    Sample sample = service.get(null).orElse(null);
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
  public void all_Filter() {
    SampleFilter filter = mock(SampleFilter.class);
    when(filter.predicate()).thenReturn(sample.isNotNull());
    when(filter.pageable()).thenReturn(PageRequest.of(0, 100));

    List<Sample> samples = service.all(filter);

    assertEquals(11, samples.size());
    assertEquals((Long) 1L, samples.get(0).getId());
    assertEquals((Long) 2L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());
    assertEquals((Long) 4L, samples.get(3).getId());
    assertEquals((Long) 5L, samples.get(4).getId());
    assertEquals((Long) 6L, samples.get(5).getId());
    assertEquals((Long) 7L, samples.get(6).getId());
    assertEquals((Long) 8L, samples.get(7).getId());
    assertEquals((Long) 9L, samples.get(8).getId());
    assertEquals((Long) 10L, samples.get(9).getId());
    assertEquals((Long) 11L, samples.get(10).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterName() {
    SampleFilter filter = new SampleFilter();
    filter.nameContains = "BC";

    List<Sample> samples = service.all(filter);

    assertEquals(4, samples.size());
    assertEquals((Long) 6L, samples.get(0).getId());
    assertEquals((Long) 7L, samples.get(1).getId());
    assertEquals((Long) 8L, samples.get(2).getId());
    assertEquals((Long) 9L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterPage() {
    SampleFilter filter = new SampleFilter();
    filter.page = 1;
    filter.size = 5;

    List<Sample> samples = service.all(filter);

    assertEquals(5, samples.size());
    assertEquals((Long) 6L, samples.get(0).getId());
    assertEquals((Long) 7L, samples.get(1).getId());
    assertEquals((Long) 8L, samples.get(2).getId());
    assertEquals((Long) 9L, samples.get(3).getId());
    assertEquals((Long) 10L, samples.get(4).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterSortName() {
    SampleFilter filter = new SampleFilter();
    filter.nameContains = "BC";
    filter.sort = Sort.by(Direction.ASC, NAME);

    List<Sample> samples = service.all(filter);

    assertEquals(4, samples.size());
    assertEquals((Long) 9L, samples.get(0).getId());
    assertEquals((Long) 8L, samples.get(1).getId());
    assertEquals((Long) 6L, samples.get(2).getId());
    assertEquals((Long) 7L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterSortOwnerAndDate() {
    SampleFilter filter = new SampleFilter();
    filter.sort = Sort.by(Order.asc(OWNER + "." + EMAIL), Order.desc(DATE), Order.asc(ID));

    List<Sample> samples = service.all(filter);

    assertEquals(11, samples.size());
    assertEquals((Long) 9L, samples.get(0).getId());
    assertEquals((Long) 8L, samples.get(1).getId());
    assertEquals((Long) 6L, samples.get(2).getId());
    assertEquals((Long) 7L, samples.get(3).getId());
    assertEquals((Long) 1L, samples.get(4).getId());
    assertEquals((Long) 2L, samples.get(5).getId());
    assertEquals((Long) 3L, samples.get(6).getId());
    assertEquals((Long) 11L, samples.get(7).getId());
    assertEquals((Long) 10L, samples.get(8).getId());
    assertEquals((Long) 4L, samples.get(9).getId());
    assertEquals((Long) 5L, samples.get(10).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_NullFilter() {
    List<Sample> samples = service.all(null);

    assertEquals(11, samples.size());
    assertEquals((Long) 1L, samples.get(0).getId());
    assertEquals((Long) 2L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());
    assertEquals((Long) 4L, samples.get(3).getId());
    assertEquals((Long) 5L, samples.get(4).getId());
    assertEquals((Long) 6L, samples.get(5).getId());
    assertEquals((Long) 7L, samples.get(6).getId());
    assertEquals((Long) 8L, samples.get(7).getId());
    assertEquals((Long) 9L, samples.get(8).getId());
    assertEquals((Long) 10L, samples.get(9).getId());
    assertEquals((Long) 11L, samples.get(10).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void count_Filter() {
    SampleFilter filter = mock(SampleFilter.class);
    when(filter.predicate()).thenReturn(sample.isNotNull());
    when(filter.pageable()).thenReturn(PageRequest.of(0, 100));

    long count = service.count(filter);

    assertEquals(11, count);
  }

  @Test
  public void count_FilterName() {
    SampleFilter filter = new SampleFilter();
    filter.nameContains = "BC";

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_NullFilter() {
    long count = service.count(null);

    assertEquals(11, count);
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
  public void uploadFiles() throws Throwable {
    Sample sample = repository.findById(1L).orElse(null);
    Path upload = configuration.getUpload();
    Files.createDirectories(upload);
    Path uploadFile = upload.resolve(sample.getName() + ".fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("." + sample.getName() + ".bed");
    Files.createFile(uploadFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(uploadFile, "dos:hidden", Boolean.TRUE);
    }
    uploadFile = upload.resolve(sample.getName());
    Files.createDirectory(uploadFile);
    Path folder = configuration.upload(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("sample_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("sample_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    file = folder.resolve("folder");
    Files.createDirectory(file);

    List<Path> files = service.uploadFiles(sample);

    verify(configuration, times(2)).getUpload();
    verify(configuration, times(2)).upload(sample);
    assertEquals(3, files.size());
    assertTrue(files.contains(upload.resolve(sample.getName() + ".fastq")));
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void uploadFiles_SampleFolderNotExists() throws Throwable {
    Sample sample = repository.findById(1L).orElse(null);
    Path upload = configuration.getUpload();
    Files.createDirectories(upload);
    Path uploadFile = upload.resolve(sample.getName() + ".fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("." + sample.getName() + ".bed");
    Files.createFile(uploadFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(uploadFile, "dos:hidden", Boolean.TRUE);
    }
    uploadFile = upload.resolve(sample.getName());
    Files.createDirectory(uploadFile);

    List<Path> files = service.uploadFiles(sample);

    verify(configuration, times(2)).getUpload();
    verify(configuration).upload(sample);
    assertEquals(1, files.size());
    assertTrue(files.contains(upload.resolve(sample.getName() + ".fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void uploadFiles_UploadFolderNotExists() throws Throwable {
    Sample sample = repository.findById(1L).orElse(null);
    Path folder = configuration.upload(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("sample_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("sample_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    file = folder.resolve("folder");
    Files.createDirectory(file);

    List<Path> files = service.uploadFiles(sample);

    verify(configuration).getUpload();
    verify(configuration, times(2)).upload(sample);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void uploadFiles_NullId() throws Throwable {
    List<Path> files = service.uploadFiles(new Sample());

    assertTrue(files.isEmpty());
  }

  @Test
  public void uploadFiles_Null() throws Throwable {
    List<Path> files = service.uploadFiles(null);

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

  @Test
  @WithAnonymousUser
  public void isMergable_Anonymous() {
    assertThrows(AccessDeniedException.class, () -> {
      assertFalse(service.isMergable(new ArrayList<>()));
    });
  }

  @Test
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
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
    when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
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

  @Test
  public void save_UpdateNotEditable() {
    assertThrows(IllegalArgumentException.class, () -> {
      User user = userRepository.findById(2L).orElse(null);
      when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
      Sample sample = repository.findById(8L).orElse(null);
      service.save(sample);
    });
  }

  @Test
  public void save_UpdateMoveFiles() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
    Sample sample = repository.findById(1L).orElse(null);
    detach(sample);
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
  public void save_UpdateMoveFilesAlreadyRenamed() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
    Sample sample = repository.findById(1L).orElse(null);
    detach(sample);
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    Path beforeFolder = configuration.folder(sample);
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("sample_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("sample_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);
    sample.generateName();

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
  public void save_UpdateMoveFilesParentNotExists() throws Throwable {
    when(configuration.folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null ? temporaryFolder
          .resolve(String.valueOf(sample.getDate().getYear())).resolve(sample.getName()) : null;
    });
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
    Sample sample = repository.findById(1L).orElse(null);
    detach(sample);
    Path beforeFolder = configuration.folder(sample);
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("sample_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("sample_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setDate(LocalDate.of(2020, 01, 12));

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElse(null);
    assertEquals("my sample", sample.getSampleId());
    assertEquals("my replicate", sample.getReplicate());
    assertEquals(LocalDate.of(2020, 01, 12), sample.getDate());
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
  public void save_RenameFiles() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
    Sample sample = repository.findById(1L).orElse(null);
    detach(sample);
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    Path beforeFolder = configuration.folder(sample);
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.write(
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq.md5"),
        ("e254a11d5102c5555232c3d7d0a53a0b  "
            + "FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq")
                .getBytes(StandardCharsets.UTF_8));
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R2.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.write(
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R2.fastq.md5"),
        ("c0f5c3b76104640e306fce3c669f300e  "
            + "FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R2.fastq")
                .getBytes(StandardCharsets.UTF_8));
    Dataset dataset = datasetRepository.findById(1L).get();
    Path beforeDatasetFolder = configuration.folder(dataset);
    Files.createDirectories(beforeDatasetFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeDatasetFolder
            .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R2.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.write(
        beforeDatasetFolder
            .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R2.fastq.md5"),
        ("c0f5c3b76104640e306fce3c669f300e  "
            + "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R2.fastq")
                .getBytes(StandardCharsets.UTF_8));

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElse(null);
    assertEquals("my sample", sample.getSampleId());
    assertEquals("my replicate", sample.getReplicate());
    Path folder = configuration.folder(sample);
    assertTrue(Files.exists(folder
        .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder
            .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertTrue(Files.exists(folder
        .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5")));
    List<String> md5Lines = Files.readAllLines(folder
        .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals(
        "e254a11d5102c5555232c3d7d0a53a0b  "
            + "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq",
        md5Lines.get(0));
    assertTrue(Files.exists(folder
        .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder
            .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq")));
    assertTrue(Files.exists(folder
        .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq.md5")));
    md5Lines = Files.readAllLines(folder
        .resolve("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals(
        "c0f5c3b76104640e306fce3c669f300e  "
            + "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeFolder));
    dataset = datasetRepository.findById(1L).get();
    Path datasetFolder = configuration.folder(dataset);
    assertTrue(Files.exists(datasetFolder
        .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_mysample-FR2-FR3_20181020_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(datasetFolder
            .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_mysample-FR2-FR3_20181020_R2.fastq")));
    assertTrue(Files.exists(datasetFolder
        .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_mysample-FR2-FR3_20181020_R2.fastq.md5")));
    md5Lines = Files.readAllLines(datasetFolder
        .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_mysample-FR2-FR3_20181020_R2.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals(
        "c0f5c3b76104640e306fce3c669f300e  "
            + "MNaseseq_IP_polr2a_yFR100_WT_Rappa_mysample-FR2-FR3_20181020_R2.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeDatasetFolder));
  }

  @Test
  public void saveFiles() throws Throwable {
    final Sample sample = repository.findById(1L).orElse(null);
    List<Path> files = new ArrayList<>();
    Path file = temporaryFolder.resolve("sample_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file);
    final FileTime filetime1 = Files.getLastModifiedTime(file);
    files.add(file);
    file = temporaryFolder.resolve("sample_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file);
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

  @Test
  public void delete_LinkedToDataset() {
    assertThrows(IllegalArgumentException.class, () -> {
      Sample sample = repository.findById(1L).get();
      service.delete(sample);
    });
  }

  @Test
  public void delete_NotEditable() {
    assertThrows(IllegalArgumentException.class, () -> {
      Sample sample = repository.findById(9L).get();
      sample.setEditable(false);
      service.delete(sample);
    });
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

  @Test
  public void deleteFile_FullPathNotInSampleFolder() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      Sample sample = repository.findById(9L).get();
      Path file = temporaryFolder.resolve("test.txt");
      Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
          StandardCopyOption.REPLACE_EXISTING);
      Files.setLastModifiedTime(file,
          FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

      service.deleteFile(sample, file);
    });
  }

  @Test
  public void deleteFile_RelativePathNotInSampleFolder() throws Throwable {
    assertThrows(IllegalArgumentException.class, () -> {
      Sample sample = repository.findById(9L).get();
      Path folder = configuration.folder(sample);
      Files.createDirectories(folder);
      Path file = Paths.get("../test.txt");
      Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
          StandardCopyOption.REPLACE_EXISTING);
      Files.setLastModifiedTime(file,
          FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

      service.deleteFile(sample, file);
    });
  }
}
