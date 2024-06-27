package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.dataset.QDataset.dataset;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import jakarta.persistence.EntityManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link DatasetService}.
 */
@ServiceTestAnnotations
@WithMockUser
public class DatasetServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private DatasetService service;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private EntityManager entityManager;
  @MockBean
  private SampleService sampleService;
  @MockBean
  private AppConfiguration configuration;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @TempDir
  Path temporaryFolder;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(configuration.getHome()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getHome().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve(dataset.getName())
          : null;
    });
    when(configuration.getHome().label(any(Dataset.class), anyBoolean())).then(i -> {
      Dataset dataset = i.getArgument(0);
      boolean unix = i.getArgument(1);
      String label = "\\\\lanaseq01\\"
          + (dataset != null && dataset.getName() != null ? dataset.getName() : "");
      return unix ? FilenameUtils.separatorsToUnix(label) : label;
    });
    List archives = new ArrayList();
    archives.add(mock(AppConfiguration.NetworkDrive.class));
    archives.add(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getArchives()).thenReturn(archives);
    when(configuration.getArchives().get(0).folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve("archives").resolve(dataset.getName())
          : null;
    });
    when(configuration.getArchives().get(0).label(any(Dataset.class), anyBoolean())).then(i -> {
      Dataset dataset = i.getArgument(0);
      boolean unix = i.getArgument(1);
      String label = "\\\\lanaseq01\\archives\\"
          + (dataset != null && dataset.getName() != null ? dataset.getName() : "");
      return unix ? FilenameUtils.separatorsToUnix(label) : label;
    });
    when(configuration.getArchives().get(1).folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve("archives2").resolve(dataset.getName())
          : null;
    });
    when(configuration.getArchives().get(1).label(any(Dataset.class), anyBoolean())).then(i -> {
      Dataset dataset = i.getArgument(0);
      boolean unix = i.getArgument(1);
      String label = "\\\\lanaseq02\\archives2\\"
          + (dataset != null && dataset.getName() != null ? dataset.getName() : "");
      return unix ? FilenameUtils.separatorsToUnix(label) : label;
    });
    when(configuration.getUpload()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getUpload().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null
          ? temporaryFolder.resolve(dataset.getName())
          : null;
    });
    when(configuration.getUpload().getFolder()).then(i -> {
      return temporaryFolder.resolve("upload");
    });
  }

  private void detach(Dataset dataset) {
    dataset.getTags().size();
    dataset.getSamples().forEach(sample -> entityManager.detach(sample));
    entityManager.detach(dataset);
  }

  @Test
  public void get() {
    Dataset dataset = service.get(1L).orElse(null);

    assertEquals((Long) 1L, dataset.getId());
    assertEquals("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020", dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertEquals("robtools version 2", dataset.getNote());
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertTrue(dataset.isEditable());
    assertEquals(3, dataset.getSamples().size());
    assertEquals((Long) 1L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 2L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getSamples().get(2).getId());
    assertEquals(LocalDate.of(2018, 10, 20), dataset.getDate());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void get_Null() {
    Dataset dataset = service.get(null).orElse(null);
    assertNull(dataset);
  }

  @Test
  public void exists_True() {
    assertTrue(service.exists("ChIPSeq_Spt16_yFR101_G24D_JS3_20181211"));
  }

  @Test
  public void exists_False() {
    assertFalse(service.exists("ChIPSeq_Spt16_yFR101_G24D"));
  }

  @Test
  public void exists_Null() {
    assertFalse(service.exists(null));
  }

  @Test
  public void all() {
    User user = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));

    List<Dataset> datasets = service.all();

    assertEquals(8, datasets.size());
    assertTrue(find(datasets, 1L).isPresent());
    assertTrue(find(datasets, 2L).isPresent());
    assertTrue(find(datasets, 3L).isPresent());
    assertTrue(find(datasets, 4L).isPresent());
    assertTrue(find(datasets, 5L).isPresent());
    assertTrue(find(datasets, 6L).isPresent());
    assertTrue(find(datasets, 7L).isPresent());
    assertTrue(find(datasets, 8L).isPresent());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void all_Filter() {
    DatasetFilter filter = mock(DatasetFilter.class);
    when(filter.predicate()).thenReturn(dataset.isNotNull());
    when(filter.pageable()).thenReturn(PageRequest.of(0, 100));

    List<Dataset> datasets = service.all(filter);

    datasets.stream().forEach(dataset -> dataset.getTags().size());
    datasets.stream().forEach(
        dataset -> dataset.getSamples().stream().forEach(sample -> sample.getProtocol().getName()));
    assertEquals(8, datasets.size());
    assertEquals((Long) 1L, datasets.get(0).getId());
    assertEquals((Long) 2L, datasets.get(1).getId());
    assertEquals((Long) 3L, datasets.get(2).getId());
    assertEquals((Long) 4L, datasets.get(3).getId());
    assertEquals((Long) 5L, datasets.get(4).getId());
    assertEquals((Long) 6L, datasets.get(5).getId());
    assertEquals((Long) 7L, datasets.get(6).getId());
    assertEquals((Long) 8L, datasets.get(7).getId());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void all_FilterName() {
    DatasetFilter filter = new DatasetFilter();
    filter.nameContains = "JS";

    List<Dataset> datasets = service.all(filter);

    assertEquals(3, datasets.size());
    assertEquals((Long) 2L, datasets.get(0).getId());
    assertEquals((Long) 6L, datasets.get(1).getId());
    assertEquals((Long) 7L, datasets.get(2).getId());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void all_FilterPage() {
    DatasetFilter filter = new DatasetFilter();
    filter.page = 1;
    filter.size = 3;

    List<Dataset> datasets = service.all(filter);

    assertEquals(3, datasets.size());
    assertEquals((Long) 4L, datasets.get(0).getId());
    assertEquals((Long) 5L, datasets.get(1).getId());
    assertEquals((Long) 6L, datasets.get(2).getId());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void all_FilterPageSort() {
    DatasetFilter filter = new DatasetFilter();
    filter.page = 1;
    filter.size = 3;
    filter.sort = Sort.by(Direction.ASC, NAME);

    List<Dataset> datasets = service.all(filter);

    assertEquals(3, datasets.size());
    assertEquals((Long) 4L, datasets.get(0).getId());
    assertEquals((Long) 2L, datasets.get(1).getId());
    assertEquals((Long) 6L, datasets.get(2).getId());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void all_FilterSortName() {
    DatasetFilter filter = new DatasetFilter();
    filter.nameContains = "JS";
    filter.sort = Sort.by(Direction.ASC, NAME);

    List<Dataset> datasets = service.all(filter);

    assertEquals(3, datasets.size());
    assertEquals((Long) 2L, datasets.get(0).getId());
    assertEquals((Long) 6L, datasets.get(1).getId());
    assertEquals((Long) 7L, datasets.get(2).getId());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void all_FilterSortOwnerAndDate() {
    DatasetFilter filter = new DatasetFilter();
    filter.sort = Sort.by(Order.asc(OWNER + "." + EMAIL), Order.desc(DATE), Order.asc(ID));

    List<Dataset> datasets = service.all(filter);

    assertEquals(8, datasets.size());
    assertEquals((Long) 5L, datasets.get(0).getId());
    assertEquals((Long) 4L, datasets.get(1).getId());
    assertEquals((Long) 1L, datasets.get(2).getId());
    assertEquals((Long) 7L, datasets.get(3).getId());
    assertEquals((Long) 6L, datasets.get(4).getId());
    assertEquals((Long) 3L, datasets.get(5).getId());
    assertEquals((Long) 2L, datasets.get(6).getId());
    assertEquals((Long) 8L, datasets.get(7).getId());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void all_NullFilter() {
    List<Dataset> datasets = service.all(null);

    assertEquals(8, datasets.size());
    assertEquals((Long) 1L, datasets.get(0).getId());
    assertEquals((Long) 2L, datasets.get(1).getId());
    assertEquals((Long) 3L, datasets.get(2).getId());
    assertEquals((Long) 4L, datasets.get(3).getId());
    assertEquals((Long) 5L, datasets.get(4).getId());
    assertEquals((Long) 6L, datasets.get(5).getId());
    assertEquals((Long) 7L, datasets.get(6).getId());
    assertEquals((Long) 8L, datasets.get(7).getId());
    for (Dataset dataset : datasets) {
      verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
    }
  }

  @Test
  public void count_Filter() {
    DatasetFilter filter = mock(DatasetFilter.class);
    when(filter.predicate()).thenReturn(dataset.isNotNull());
    when(filter.pageable()).thenReturn(PageRequest.of(0, 100));

    long count = service.count(filter);

    assertEquals(8, count);
  }

  @Test
  public void count_FilterName() {
    DatasetFilter filter = new DatasetFilter();
    filter.nameContains = "JS";

    long count = service.count(filter);

    assertEquals(3, count);
  }

  @Test
  public void count_NullFilter() {
    long count = service.count(null);

    assertEquals(8, count);
  }

  @Test
  public void files() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("dataset_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("dataset_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".deleted");
    Files.createFile(file);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }

    List<Path> files = service.files(dataset);

    verify(configuration.getHome(), times(2)).folder(dataset);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    assertTrue(files.contains(folder.resolve("dataset_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void files_FolderNotExists() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);

    List<Path> files = service.files(dataset);

    verify(configuration.getHome()).folder(dataset);
    assertTrue(files.isEmpty());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void files_Archives() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("dataset_h_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    folder = configuration.getArchives().get(0).folder(dataset);
    Files.createDirectories(folder);
    file = folder.resolve("dataset_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("dataset_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".deleted");
    Files.createFile(file);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    folder = configuration.getArchives().get(1).folder(dataset);
    Files.createDirectories(folder);
    file = folder.resolve("dataset_a2_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);

    List<Path> files = service.files(dataset);

    verify(configuration.getHome(), times(2)).folder(dataset);
    verify(configuration.getArchives().get(0), times(2)).folder(dataset);
    verify(configuration.getArchives().get(1), times(2)).folder(dataset);
    assertEquals(4, files.size());
    folder = configuration.getHome().folder(dataset);
    assertTrue(files.contains(folder.resolve("dataset_h_R1.fastq")));
    folder = configuration.getArchives().get(0).folder(dataset);
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    assertTrue(files.contains(folder.resolve("dataset_R2.fastq")));
    folder = configuration.getArchives().get(1).folder(dataset);
    assertTrue(files.contains(folder.resolve("dataset_a2_R1.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void files_ArchivesSameFilename() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("dataset_R1.fastq"));
    folder = configuration.getArchives().get(0).folder(dataset);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("dataset_R1.fastq"));
    folder = configuration.getArchives().get(1).folder(dataset);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("dataset_R1.fastq"));

    List<Path> files = service.files(dataset);

    verify(configuration.getHome(), times(2)).folder(dataset);
    assertEquals(3, files.size());
    folder = configuration.getHome().folder(dataset);
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    folder = configuration.getArchives().get(0).folder(dataset);
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    folder = configuration.getArchives().get(1).folder(dataset);
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void files_ArchivesNotExists() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("dataset_R1.fastq"));

    List<Path> files = service.files(dataset);

    verify(configuration.getHome(), times(2)).folder(dataset);
    assertEquals(1, files.size());
    folder = configuration.getHome().folder(dataset);
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void files_NullId() throws Throwable {
    List<Path> files = service.files(new Dataset());

    assertTrue(files.isEmpty());
  }

  @Test
  public void files_Null() throws Throwable {
    List<Path> files = service.files(null);

    assertTrue(files.isEmpty());
  }

  @Test
  public void folderLabels() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(dataset, false);

    assertEquals(1, labels.size());
    assertEquals("\\\\lanaseq01\\" + dataset.getName(), labels.get(0));
  }

  @Test
  public void folderLabels_Unix() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(dataset, true);

    assertEquals(1, labels.size());
    assertEquals("//lanaseq01/" + dataset.getName(), labels.get(0));
  }

  @Test
  public void folderLabels_Archives() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(0).folder(dataset);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(1).folder(dataset);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(dataset, false);

    assertEquals(3, labels.size());
    assertEquals("\\\\lanaseq01\\" + dataset.getName(), labels.get(0));
    assertEquals("\\\\lanaseq01\\archives\\" + dataset.getName(), labels.get(1));
    assertEquals("\\\\lanaseq02\\archives2\\" + dataset.getName(), labels.get(2));
  }

  @Test
  public void folderLabels_Archives_Unix() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(0).folder(dataset);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(1).folder(dataset);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(dataset, true);

    assertEquals(3, labels.size());
    assertEquals("//lanaseq01/" + dataset.getName(), labels.get(0));
    assertEquals("//lanaseq01/archives/" + dataset.getName(), labels.get(1));
    assertEquals("//lanaseq02/archives2/" + dataset.getName(), labels.get(2));
  }

  @Test
  public void folderLabels_FoldersNotExists() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);

    List<String> labels = service.folderLabels(dataset, false);

    assertTrue(labels.isEmpty());
  }

  @Test
  public void folderLabels_NullId() throws Throwable {
    List<String> labels = service.folderLabels(new Dataset(), false);

    assertTrue(labels.isEmpty());
  }

  @Test
  public void folderLabels_Null() throws Throwable {
    List<String> labels = service.folderLabels(null, false);

    assertTrue(labels.isEmpty());
  }

  @Test
  public void uploadFiles() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path upload = configuration.getUpload().getFolder();
    Files.createDirectories(upload);
    Path uploadFile = upload.resolve(dataset.getName() + ".fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("." + dataset.getName() + ".bed");
    Files.createFile(uploadFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(uploadFile, "dos:hidden", Boolean.TRUE);
    }
    uploadFile = upload.resolve(dataset.getName());
    Files.createDirectory(uploadFile);
    Path folder = configuration.getUpload().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("dataset_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("dataset_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    file = folder.resolve("folder");
    Files.createDirectory(file);

    List<Path> files = service.uploadFiles(dataset);

    verify(configuration.getUpload(), times(2)).getFolder();
    verify(configuration.getUpload(), times(2)).folder(dataset);
    verify(configuration.getArchives().get(0), never()).folder(dataset);
    verify(configuration.getArchives().get(1), never()).folder(dataset);
    assertEquals(3, files.size());
    assertTrue(files.contains(upload.resolve(dataset.getName() + ".fastq")));
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    assertTrue(files.contains(folder.resolve("dataset_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void uploadFiles_DatasetFolderNotExists() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path upload = configuration.getUpload().getFolder();
    Files.createDirectories(upload);
    Path uploadFile = upload.resolve(dataset.getName() + ".fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), uploadFile,
        StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("." + dataset.getName() + ".bed");
    Files.createFile(uploadFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(uploadFile, "dos:hidden", Boolean.TRUE);
    }
    uploadFile = upload.resolve(dataset.getName());
    Files.createDirectory(uploadFile);

    List<Path> files = service.uploadFiles(dataset);

    verify(configuration.getUpload(), times(2)).getFolder();
    verify(configuration.getUpload()).folder(dataset);
    verify(configuration.getArchives().get(0), never()).folder(dataset);
    verify(configuration.getArchives().get(1), never()).folder(dataset);
    assertEquals(1, files.size());
    assertTrue(files.contains(upload.resolve(dataset.getName() + ".fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void uploadFiles_UploadFolderNotExists() throws Throwable {
    Dataset dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getUpload().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("dataset_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("dataset_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    file = folder.resolve("folder");
    Files.createDirectory(file);

    List<Path> files = service.uploadFiles(dataset);

    verify(configuration.getUpload()).getFolder();
    verify(configuration.getUpload(), times(2)).folder(dataset);
    verify(configuration.getArchives().get(0), never()).folder(dataset);
    verify(configuration.getArchives().get(1), never()).folder(dataset);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("dataset_R1.fastq")));
    assertTrue(files.contains(folder.resolve("dataset_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void uploadFiles_NullId() throws Throwable {
    List<Path> files = service.uploadFiles(new Dataset());

    assertTrue(files.isEmpty());
  }

  @Test
  public void uploadFiles_Null() throws Throwable {
    List<Path> files = service.uploadFiles(null);

    assertTrue(files.isEmpty());
  }

  @Test
  public void topTags() {
    List<String> tags = service.topTags(4);
    assertEquals(4, tags.size());
    assertTrue(tags.contains("ip"));
    assertTrue(tags.contains("chipseq"));
    assertTrue(tags.contains("G24D"));
    assertTrue(tags.contains("Spt16"));
  }

  @Test
  public void isDeletable_False() {
    Dataset dataset = repository.findById(5L).get();
    assertFalse(service.isDeletable(dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void isDeletable_True() {
    Dataset dataset = repository.findById(1L).get();
    assertTrue(service.isDeletable(dataset));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(READ));
  }

  @Test
  public void isDeletable_Null() {
    assertFalse(service.isDeletable(null));
  }

  @Test
  public void isDeletable_NullId() {
    assertFalse(service.isDeletable(new Dataset()));
  }

  @Test
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = new Dataset();
    dataset.setTags(new HashSet<>());
    dataset.getTags().add("tag1");
    dataset.getTags().add("tag2");
    dataset.setDate(LocalDate.of(2020, 7, 21));
    dataset.setSamples(new ArrayList<>());
    dataset.setNote("test note");
    dataset.getSamples().add(sampleRepository.findById(1L).get());
    dataset.getSamples().add(sampleRepository.findById(2L).get());
    dataset.generateName();

    service.save(dataset);

    repository.flush();
    assertNotNull(dataset.getId());
    dataset = repository.findById(dataset.getId()).orElse(null);
    assertEquals("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2_20200721", dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("tag1"));
    assertTrue(dataset.getTags().contains("tag2"));
    assertEquals("test note", dataset.getNote());
    assertEquals(user.getId(), dataset.getOwner().getId());
    assertEquals(LocalDate.of(2020, 7, 21), dataset.getDate());
    assertTrue(dataset.isEditable());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(dataset.getCreationDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(dataset.getCreationDate()));
    assertEquals(2, dataset.getSamples().size());
    for (Sample sample : dataset.getSamples()) {
      verify(sampleService, never()).save(sample);
    }
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  public void save_NewWithNewSample() {
    User user = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = new Dataset();
    dataset.setTags(new HashSet<>());
    dataset.getTags().add("tag1");
    dataset.getTags().add("tag2");
    dataset.setDate(LocalDate.of(2020, 7, 21));
    dataset.setSamples(new ArrayList<>());
    dataset.setNote("test note");
    Sample sample1 = new Sample();
    sample1.setSampleId("sample1");
    sample1.setReplicate("r1");
    sample1.setAssay("ChIP-seq");
    sample1.setType("IP");
    sample1.setTarget("my target");
    sample1.setStrain("yFR213");
    sample1.setStrainDescription("F56G");
    sample1.setTreatment("37C");
    sample1.setProtocol(protocolRepository.findById(1L).get());
    sample1.setDate(LocalDate.of(2020, 7, 21));
    sample1.setNote("test note");
    dataset.getSamples().add(sample1);
    dataset.generateName();

    assertThrows(IllegalArgumentException.class, () -> {
      service.save(dataset);
    });
  }

  @Test
  public void save_NewNoName() {
    User user = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = new Dataset();
    dataset.setTags(new HashSet<>());
    dataset.getTags().add("tag1");
    dataset.getTags().add("tag2");
    dataset.setDate(LocalDate.of(2020, 7, 21));
    dataset.setSamples(new ArrayList<>());
    dataset.setNote("test note");
    dataset.getSamples().add(sampleRepository.findById(1L).get());

    assertThrows(NullPointerException.class, () -> {
      service.save(dataset);
    });
  }

  @Test
  public void save_Update() {
    User user = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = repository.findById(1L).orElse(null);
    detach(dataset);
    dataset.getTags().remove("rappa");
    dataset.getTags().add("tag1");
    dataset.setNote("test note");
    dataset.setDate(LocalDate.of(2020, 7, 21));
    Sample sample1 = dataset.getSamples().get(0);
    sample1.setSampleId("sample1");
    sample1.setReplicate("r1");
    sample1.setAssay("ChIP-seq");
    sample1.setType("Input");
    sample1.setTarget("my target");
    sample1.setStrain("yFR213");
    sample1.setStrainDescription("F56G");
    sample1.setTreatment("37C");
    sample1.setProtocol(protocolRepository.findById(3L).get());
    sample1.setDate(LocalDate.of(2020, 7, 21));
    sample1.setNote("test note");
    final Sample removed = dataset.getSamples().remove(1);
    dataset.getSamples().add(sampleRepository.findById(4L).get());
    dataset.generateName();

    service.save(dataset);

    repository.flush();
    for (Sample sample : dataset.getSamples()) {
      verify(sampleService, never()).save(sample);
    }
    verify(sampleService, never()).save(removed);
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
    dataset = repository.findById(1L).orElse(null);
    assertEquals("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR3-JS1_20200721",
        dataset.getName());
    assertEquals(3, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertTrue(dataset.getTags().contains("tag1"));
    assertEquals("test note", dataset.getNote());
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(LocalDate.of(2020, 7, 21), dataset.getDate());
    assertTrue(dataset.isEditable());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getCreationDate());
    assertEquals(3, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 1L).isPresent());
    assertTrue(find(dataset.getSamples(), 3L).isPresent());
    assertTrue(find(dataset.getSamples(), 4L).isPresent());
  }

  @Test
  public void save_UpdateWithNewSample() {
    User user = userRepository.findById(3L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = repository.findById(1L).orElse(null);
    detach(dataset);
    dataset.getTags().remove("rappa");
    dataset.getTags().add("tag1");
    dataset.setNote("test note");
    dataset.setDate(LocalDate.of(2020, 7, 21));
    Sample newSample = new Sample();
    newSample.setSampleId("new_sample");
    newSample.setReplicate("new_r");
    newSample.setAssay("ChIP-seq");
    newSample.setType("IP");
    newSample.setTarget("my target");
    newSample.setStrain("yFR213");
    newSample.setStrainDescription("F56G");
    newSample.setTreatment("37C");
    newSample.setProtocol(protocolRepository.findById(1L).get());
    newSample.setDate(LocalDate.of(2020, 7, 21));
    newSample.setNote("test note");
    dataset.getSamples().add(newSample);

    assertThrows(IllegalArgumentException.class, () -> {
      service.save(dataset);
    });
  }

  @Test
  public void save_UpdateMoveFiles() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = repository.findById(1L).orElse(null);
    detach(dataset);
    Path beforeFolder = configuration.getHome().folder(dataset);
    Path beforeArchive1 = configuration.getArchives().get(0).folder(dataset);
    Path beforeArchive2 = configuration.getArchives().get(1).folder(dataset);
    dataset.setName("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020");
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("dataset_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("dataset_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.createDirectories(beforeArchive1);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeArchive1.resolve("dataset_a1_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.createDirectories(beforeArchive2);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeArchive2.resolve("dataset_a2_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);

    service.save(dataset);

    repository.flush();
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
    dataset = repository.findById(1L).orElse(null);
    assertEquals("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020",
        dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertEquals("robtools version 2", dataset.getNote());
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertTrue(dataset.isEditable());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getCreationDate());
    assertEquals(3, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 1L).isPresent());
    assertTrue(find(dataset.getSamples(), 2L).isPresent());
    assertTrue(find(dataset.getSamples(), 3L).isPresent());
    Path folder = configuration.getHome().folder(dataset);
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(folder.resolve("dataset_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("dataset_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R2.fastq")));
    assertFalse(Files.exists(beforeFolder));
    Path archive1 = configuration.getArchives().get(0).folder(dataset);
    assertTrue(Files.exists(archive1.resolve("dataset_a1_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(archive1.resolve("dataset_a1_R1.fastq")));
    assertFalse(Files.exists(beforeArchive1));
    Path archive2 = configuration.getArchives().get(1).folder(dataset);
    assertTrue(Files.exists(archive2.resolve("dataset_a2_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(archive2.resolve("dataset_a2_R1.fastq")));
    assertFalse(Files.exists(beforeArchive2));
  }

  @Test
  public void save_UpdateMoveFilesParentNotExists() throws Throwable {
    when(configuration.getHome().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return dataset != null && dataset.getName() != null ? temporaryFolder
          .resolve(String.valueOf(dataset.getDate().getYear())).resolve(dataset.getName()) : null;
    });
    User user = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = repository.findById(1L).orElse(null);
    detach(dataset);
    Path beforeFolder = configuration.getHome().folder(dataset);
    Path beforeArchive1 = configuration.getArchives().get(0).folder(dataset);
    Path beforeArchive2 = configuration.getArchives().get(1).folder(dataset);
    dataset.setName("MNaseseq_IP_polr2a_yFR100_WT_Rappa_sample1-FR2-FR3_20200112");
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("dataset_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("dataset_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);
    dataset.setDate(LocalDate.of(2020, 01, 12));
    Files.createDirectories(beforeArchive1);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeArchive1.resolve("dataset_a1_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.createDirectories(beforeArchive2);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeArchive2.resolve("dataset_a2_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);

    service.save(dataset);

    repository.flush();
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
    dataset = repository.findById(1L).orElse(null);
    Path folder = configuration.getHome().folder(dataset);
    assertTrue(Files.exists(folder.resolve("dataset_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("dataset_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("dataset_R2.fastq")));
    assertFalse(Files.exists(beforeFolder));
    Path archive1 = configuration.getArchives().get(0).folder(dataset);
    assertTrue(Files.exists(archive1.resolve("dataset_a1_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(archive1.resolve("dataset_a1_R1.fastq")));
    assertFalse(Files.exists(beforeArchive1));
    Path archive2 = configuration.getArchives().get(1).folder(dataset);
    assertTrue(Files.exists(archive2.resolve("dataset_a2_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(archive2.resolve("dataset_a2_R1.fastq")));
    assertFalse(Files.exists(beforeArchive2));
  }

  @Test
  public void save_RenameFiles() throws Throwable {
    User user = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = repository.findById(1L).orElse(null);
    detach(dataset);
    Path beforeFolder = configuration.getHome().folder(dataset);
    Path beforeArchive1 = configuration.getArchives().get(0).folder(dataset);
    Path beforeArchive2 = configuration.getArchives().get(1).folder(dataset);
    dataset.setName("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020");
    Files.createDirectories(beforeFolder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeFolder.resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.write(
        beforeFolder
            .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq.md5"),
        ("e254a11d5102c5555232c3d7d0a53a0b  "
            + "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq")
                .getBytes(StandardCharsets.UTF_8));
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()),
        beforeFolder.resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R2.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.write(
        beforeFolder
            .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R2.fastq.md5"),
        ("c0f5c3b76104640e306fce3c669f300e  "
            + "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R2.fastq")
                .getBytes(StandardCharsets.UTF_8));
    Files.createDirectories(beforeArchive1);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeArchive1.resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.write(
        beforeArchive1
            .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq.md5"),
        ("e254a11d5102c5555232c3d7d0a53a0b  "
            + "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq")
                .getBytes(StandardCharsets.UTF_8));
    Files.createDirectories(beforeArchive2);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()),
        beforeArchive2.resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.write(
        beforeArchive2
            .resolve("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq.md5"),
        ("e254a11d5102c5555232c3d7d0a53a0b  "
            + "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020_R1.fastq")
                .getBytes(StandardCharsets.UTF_8));

    service.save(dataset);

    repository.flush();
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
    dataset = repository.findById(1L).orElse(null);
    assertEquals("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020",
        dataset.getName());
    Path folder = configuration.getHome().folder(dataset);
    assertTrue(Files.exists(folder
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder
            .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq")));
    assertTrue(Files.exists(folder
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq.md5")));
    List<String> md5Lines = Files.readAllLines(folder
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals(
        "e254a11d5102c5555232c3d7d0a53a0b  "
            + "ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq",
        md5Lines.get(0));
    assertTrue(Files.exists(folder
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder
            .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R2.fastq")));
    assertTrue(Files.exists(folder
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R2.fastq.md5")));
    md5Lines = Files.readAllLines(folder
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R2.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals(
        "c0f5c3b76104640e306fce3c669f300e  "
            + "ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R2.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeFolder));
    Path archive1 = configuration.getArchives().get(0).folder(dataset);
    assertTrue(Files.exists(archive1
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(archive1
            .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq")));
    assertTrue(Files.exists(archive1
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq.md5")));
    md5Lines = Files.readAllLines(archive1
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals(
        "e254a11d5102c5555232c3d7d0a53a0b  "
            + "ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeArchive1));
    Path archive2 = configuration.getArchives().get(1).folder(dataset);
    assertTrue(Files.exists(archive2
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(archive2
            .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq")));
    assertTrue(Files.exists(archive2
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq.md5")));
    md5Lines = Files.readAllLines(archive2
        .resolve("ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals(
        "e254a11d5102c5555232c3d7d0a53a0b  "
            + "ChIPseq_Input_mytarget_yFR213_F56G_37C_sample1-FR2-FR3_20181020_R1.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeArchive2));
  }

  @Test
  public void save_UpdateNotEditable() {
    assertThrows(IllegalArgumentException.class, () -> {
      User user = userRepository.findById(2L).orElse(null);
      when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
      Dataset dataset = repository.findById(5L).orElse(null);
      service.save(dataset);
    });
  }

  @Test
  public void save_NotEditableSample() {
    User user = userRepository.findById(2L).orElse(null);
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));
    Dataset dataset = repository.findById(1L).orElse(null);
    dataset.setName("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020");
    dataset.getTags().remove("rappa");
    dataset.getTags().add("tag1");
    dataset.setNote("test note");
    Sample sample1 = dataset.getSamples().get(0);
    sample1.setEditable(false);

    service.save(dataset);

    repository.flush();
    dataset = repository.findById(1L).orElse(null);
    assertEquals("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020", dataset.getName());
    assertEquals(3, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("mnase"));
    assertTrue(dataset.getTags().contains("ip"));
    assertTrue(dataset.getTags().contains("tag1"));
    assertEquals("test note", dataset.getNote());
    assertEquals((Long) 2L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 28, 12), dataset.getCreationDate());
    assertEquals(3, dataset.getSamples().size());
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  public void saveFiles() throws Throwable {
    final Dataset dataset = repository.findById(1L).orElse(null);
    List<Path> files = new ArrayList<>();
    Path file = temporaryFolder.resolve("dataset_R1.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file);
    files.add(file);
    file = temporaryFolder.resolve("dataset_R2.fastq");
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file);
    files.add(file);

    service.saveFiles(dataset, files);

    verify(configuration.getHome()).folder(dataset);
    verify(configuration.getArchives().get(0), never()).folder(dataset);
    verify(configuration.getArchives().get(1), never()).folder(dataset);
    Path folder = configuration.getHome().folder(dataset);
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
  public void delete() throws Throwable {
    Dataset dataset = repository.findById(1L).get();
    Path folder = configuration.getHome().folder(dataset);
    Path file = folder.resolve("R1.fastq");
    Files.createDirectories(folder);
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);

    service.delete(dataset);

    repository.flush();
    assertFalse(repository.findById(1L).isPresent());
    assertFalse(Files.exists(folder));
    verify(permissionEvaluator).hasPermission(any(), eq(dataset), eq(WRITE));
  }

  @Test
  public void delete_NotEditable() {
    assertThrows(IllegalArgumentException.class, () -> {
      Dataset dataset = repository.findById(5L).get();
      service.delete(dataset);
    });
  }

  @Test
  public void deleteFile() throws Throwable {
    Dataset dataset = repository.findById(4L).get();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = folder.resolve("test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));

    service.deleteFile(dataset, file);

    verify(configuration.getHome(), times(2)).folder(dataset);
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
    Dataset dataset = repository.findById(4L).get();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = Paths.get("test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), folder.resolve(file),
        StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(folder.resolve(file), FileTime.from(toInstant(modifiedTime)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(dataset, file));
  }

  @Test
  public void deleteFile_Archives() throws Throwable {
    Dataset dataset = repository.findById(4L).get();
    Path folder = configuration.getArchives().get(0).folder(dataset);
    Files.createDirectories(folder);
    Path file = Paths.get("test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), folder.resolve(file),
        StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(folder.resolve(file), FileTime.from(toInstant(modifiedTime)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(dataset, file));
  }

  @Test
  public void deleteFile_NotInSampleFolder() throws Throwable {
    Dataset dataset = repository.findById(4L).get();
    Path file = temporaryFolder.resolve("test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    Files.setLastModifiedTime(file,
        FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(dataset, file));
  }

  @Test
  public void deleteFile_RelativePathNotInSampleFolder() throws Throwable {
    Dataset dataset = repository.findById(4L).get();
    Path folder = configuration.getHome().folder(dataset);
    Files.createDirectories(folder);
    Path file = Paths.get("../test.txt");
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    Files.setLastModifiedTime(file,
        FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(dataset, file));
  }
}
