package ca.qc.ircm.lanaseq.sample;

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.UserRepository;
import jakarta.persistence.EntityManager;
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
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link SampleService}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleServiceTest {

  private static final String READ = "read";
  private static final String WRITE = "write";
  @TempDir
  Path temporaryFolder;
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
  @MockitoBean
  private DatasetService datasetService;
  @MockitoBean
  private AppConfiguration configuration;
  @MockitoBean
  private PermissionEvaluator permissionEvaluator;
  @Autowired
  private AuthenticatedUser authenticatedUser;

  /**
   * Before test.
   */
  @BeforeEach
  @SuppressWarnings("unchecked")
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(configuration.getHome()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getHome().getFolder()).thenReturn(temporaryFolder.resolve("home"));
    when(configuration.getHome().folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null ? temporaryFolder.resolve("home").resolve(sample.getName()) : null;
    });
    when(configuration.getHome().label(any(Sample.class), anyBoolean())).then(i -> {
      Sample sample = i.getArgument(0);
      boolean unix = i.getArgument(1);
      String label = "\\\\lanaseq01\\home\\" + (sample != null ? sample.getName() : "");
      return unix ? FilenameUtils.separatorsToUnix(label) : label;
    });
    List<AppConfiguration.NetworkDrive<DataWithFiles>> archives = new ArrayList<>();
    archives.add(mock(AppConfiguration.NetworkDrive.class));
    archives.add(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getArchives()).thenReturn(archives);
    when(configuration.getArchives().get(0).getFolder()).thenReturn(
        temporaryFolder.resolve("archives"));
    when(configuration.getArchives().get(0).folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null ? temporaryFolder.resolve("archives").resolve(sample.getName()) : null;
    });
    when(configuration.getArchives().get(0).label(any(Sample.class), anyBoolean())).then(i -> {
      Sample sample = i.getArgument(0);
      boolean unix = i.getArgument(1);
      String label = "\\\\lanaseq01\\archives\\" + (sample != null ? sample.getName() : "");
      return unix ? FilenameUtils.separatorsToUnix(label) : label;
    });
    when(configuration.getArchives().get(1).getFolder()).thenReturn(
        temporaryFolder.resolve("archives2"));
    when(configuration.getArchives().get(1).folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null ? temporaryFolder.resolve("archives2").resolve(sample.getName()) : null;
    });
    when(configuration.getArchives().get(1).label(any(Sample.class), anyBoolean())).then(i -> {
      Sample sample = i.getArgument(0);
      boolean unix = i.getArgument(1);
      String label = "\\\\lanaseq02\\archives2\\" + (sample != null ? sample.getName() : "");
      return unix ? FilenameUtils.separatorsToUnix(label) : label;
    });
    when(configuration.getUpload()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    when(configuration.getUpload().folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null ? temporaryFolder.resolve(sample.getName()) : null;
    });
    when(configuration.getUpload().getFolder()).then(i -> temporaryFolder.resolve("upload"));
  }

  private void detach(Sample sample) {
    entityManager.detach(sample);
  }

  @Test
  public void get() {
    Sample sample = service.get(1L).orElseThrow();

    assertEquals((Long) 1L, sample.getId());
    assertEquals("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020", sample.getName());
    assertEquals("FR1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals("MNase-seq", sample.getAssay());
    assertEquals("IP", sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    Set<String> keywords = sample.getKeywords();
    assertEquals(2, keywords.size());
    assertTrue(keywords.contains("mnase"));
    assertTrue(keywords.contains("ip"));
    assertTrue(sample.isEditable());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getCreationDate());
    assertEquals(LocalDate.of(2018, 10, 20), sample.getDate());
    assertEquals("robtools version 2", sample.getNote());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  private Sample sample() {
    Sample sample = new Sample();
    sample.setSampleId("JS");
    sample.setReplicate("1");
    sample.setAssay("ChIP-seq");
    sample.setType("IP");
    sample.setStrain("yFR101");
    sample.setStrainDescription("WT");
    sample.setTarget("polr2a");
    sample.setTreatment("dmso");
    sample.generateName();
    sample.setOwner(authenticatedUser.getUser().orElseThrow());
    sample.setProtocol(protocolRepository.findById(1L).orElseThrow());
    return sample;
  }

  @Test
  public void get_invalid() {
    assertFalse(service.get(0).isPresent());
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
    SampleFilter filter = new SampleFilter();

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

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

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

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
  public void all_FilterKeywords() {
    SampleFilter filter = new SampleFilter();
    filter.keywordsContains = "IP";

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(8, samples.size());
    assertEquals(1L, samples.get(0).getId());
    assertEquals(2L, samples.get(1).getId());
    assertEquals(3L, samples.get(2).getId());
    assertEquals(4L, samples.get(3).getId());
    assertEquals(5L, samples.get(4).getId());
    assertEquals(6L, samples.get(5).getId());
    assertEquals(7L, samples.get(6).getId());
    assertEquals(8L, samples.get(7).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterProtocol() {
    SampleFilter filter = new SampleFilter();
    filter.protocolContains = "tone";

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(4, samples.size());
    assertEquals(4L, samples.get(0).getId());
    assertEquals(5L, samples.get(1).getId());
    assertEquals(10L, samples.get(2).getId());
    assertEquals(11L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterOwner() {
    SampleFilter filter = new SampleFilter();
    filter.ownerContains = "smith";

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(4, samples.size());
    assertEquals(4L, samples.get(0).getId());
    assertEquals(5L, samples.get(1).getId());
    assertEquals(10L, samples.get(2).getId());
    assertEquals(11L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterOwner_Email() {
    SampleFilter filter = new SampleFilter();
    filter.ownerContains = "ombe@i";

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(4, samples.size());
    assertEquals(6L, samples.get(0).getId());
    assertEquals(7L, samples.get(1).getId());
    assertEquals(8L, samples.get(2).getId());
    assertEquals(9L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterOwner_Name() {
    SampleFilter filter = new SampleFilter();
    filter.ownerContains = "nh S";

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(4, samples.size());
    assertEquals(4L, samples.get(0).getId());
    assertEquals(5L, samples.get(1).getId());
    assertEquals(10L, samples.get(2).getId());
    assertEquals(11L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterDate() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.closed(LocalDate.of(2018, 12, 1), LocalDate.of(2019, 1, 1));

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(4, samples.size());
    assertEquals(8L, samples.get(0).getId());
    assertEquals(9L, samples.get(1).getId());
    assertEquals(10L, samples.get(2).getId());
    assertEquals(11L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterDate_Closed() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.closed(LocalDate.of(2018, 12, 5), LocalDate.of(2018, 12, 11));

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(4, samples.size());
    assertEquals(8L, samples.get(0).getId());
    assertEquals(9L, samples.get(1).getId());
    assertEquals(10L, samples.get(2).getId());
    assertEquals(11L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterDate_Open() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.open(LocalDate.of(2018, 12, 5), LocalDate.of(2018, 12, 11));

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(2, samples.size());
    assertEquals(9L, samples.get(0).getId());
    assertEquals(10L, samples.get(1).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterDate_LeftOnly_Inclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.rightUnbounded(Bound.inclusive(LocalDate.of(2018, 12, 5)));

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(4, samples.size());
    assertEquals(8L, samples.get(0).getId());
    assertEquals(9L, samples.get(1).getId());
    assertEquals(10L, samples.get(2).getId());
    assertEquals(11L, samples.get(3).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterDate_LeftOnly_Exclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.rightUnbounded(Bound.exclusive(LocalDate.of(2018, 12, 5)));

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(3, samples.size());
    assertEquals(9L, samples.get(0).getId());
    assertEquals(10L, samples.get(1).getId());
    assertEquals(11L, samples.get(2).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterDate_RightOnly_Inclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.leftUnbounded(Bound.inclusive(LocalDate.of(2018, 10, 22)));

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(5, samples.size());
    assertEquals(1L, samples.get(0).getId());
    assertEquals(2L, samples.get(1).getId());
    assertEquals(3L, samples.get(2).getId());
    assertEquals(4L, samples.get(3).getId());
    assertEquals(5L, samples.get(4).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterDate_RightOnly_Exclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.leftUnbounded(Bound.exclusive(LocalDate.of(2018, 10, 22)));

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(3, samples.size());
    assertEquals(1L, samples.get(0).getId());
    assertEquals(2L, samples.get(1).getId());
    assertEquals(3L, samples.get(2).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterNameAndKeywords() {
    SampleFilter filter = new SampleFilter();
    filter.nameContains = "js";
    filter.keywordsContains = "ip";

    List<Sample> samples = service.all(filter, Pageable.unpaged()).toList();

    assertEquals(2, samples.size());
    assertEquals((Long) 4L, samples.get(0).getId());
    assertEquals((Long) 5L, samples.get(1).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterPage() {
    SampleFilter filter = new SampleFilter();

    List<Sample> samples = service.all(filter, PageRequest.of(1, 5)).toList();

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
  public void all_FilterPageSort() {
    SampleFilter filter = new SampleFilter();

    List<Sample> samples = service.all(filter, PageRequest.of(1, 5, Sort.by(Direction.ASC, NAME)))
        .toList();

    assertEquals(5, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 3L, samples.get(1).getId());
    assertEquals((Long) 4L, samples.get(2).getId());
    assertEquals((Long) 10L, samples.get(3).getId());
    assertEquals((Long) 5L, samples.get(4).getId());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  public void all_FilterSortName() {
    SampleFilter filter = new SampleFilter();
    filter.nameContains = "BC";

    List<Sample> samples = service.all(filter, Pageable.unpaged(Sort.by(Direction.ASC, NAME)))
        .toList();

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

    List<Sample> samples = service.all(filter,
            Pageable.unpaged(Sort.by(Order.asc(OWNER + "." + EMAIL), Order.desc(DATE), Order.asc(ID))))
        .toList();

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
  public void count_Filter() {
    SampleFilter filter = new SampleFilter();

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
  public void count_FilterKeywords() {
    SampleFilter filter = new SampleFilter();
    filter.keywordsContains = "IP";

    long count = service.count(filter);

    assertEquals(8, count);
  }

  @Test
  public void count_FilterProtocol() {
    SampleFilter filter = new SampleFilter();
    filter.protocolContains = "tone";

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_FilterOwner() {
    SampleFilter filter = new SampleFilter();
    filter.ownerContains = "smith";

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_FilterOwner_Email() {
    SampleFilter filter = new SampleFilter();
    filter.ownerContains = "ombe@i";

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_FilterOwner_Name() {
    SampleFilter filter = new SampleFilter();
    filter.ownerContains = "nh S";

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_FilterDate() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.closed(LocalDate.of(2018, 12, 1), LocalDate.of(2019, 1, 1));

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_FilterDate_Closed() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.closed(LocalDate.of(2018, 12, 5), LocalDate.of(2018, 12, 11));

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_FilterDate_Open() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.open(LocalDate.of(2018, 12, 5), LocalDate.of(2018, 12, 11));

    long count = service.count(filter);

    assertEquals(2, count);
  }

  @Test
  public void count_FilterDate_LeftOnly_Inclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.rightUnbounded(Bound.inclusive(LocalDate.of(2018, 12, 5)));

    long count = service.count(filter);

    assertEquals(4, count);
  }

  @Test
  public void count_FilterDate_LeftOnly_Exclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.rightUnbounded(Bound.exclusive(LocalDate.of(2018, 12, 5)));

    long count = service.count(filter);

    assertEquals(3, count);
  }

  @Test
  public void count_FilterDate_RightOnly_Inclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.leftUnbounded(Bound.inclusive(LocalDate.of(2018, 10, 22)));

    long count = service.count(filter);

    assertEquals(5, count);
  }

  @Test
  public void count_FilterDate_RightOnly_Exclusive() {
    SampleFilter filter = new SampleFilter();
    filter.dateRange = Range.leftUnbounded(Bound.exclusive(LocalDate.of(2018, 10, 22)));

    long count = service.count(filter);

    assertEquals(3, count);
  }

  @Test
  public void count_FilterNameAndKeywords() {
    SampleFilter filter = new SampleFilter();
    filter.nameContains = "js";
    filter.keywordsContains = "ip";

    long count = service.count(filter);

    assertEquals(2, count);
  }

  @Test
  public void files() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("sample_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("sample_R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".deleted");
    Files.createFile(file);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }

    List<Path> files = service.files(sample);

    verify(configuration.getHome(), times(2)).folder(sample);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_Filenames() throws Throwable {
    Sample sample = repository.findById(4L).orElseThrow();
    Path folder = configuration.getHome().getFolder();
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("OF_20241118_ROB_01.raw"));
    Files.createDirectory(folder.resolve("otherdirectory"));
    Files.createFile(folder.resolve("otherdirectory/A_OF_20241118_ROB_01_0.raw"));
    Files.createFile(folder.resolve(".OF_20241118_ROB_01.raw"));
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(folder.resolve(".OF_20241118_ROB_01.raw"), "dos:hidden", Boolean.TRUE);
    }

    List<Path> files = service.files(sample);

    verify(configuration.getHome(), times(2)).getFolder();
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("OF_20241118_ROB_01.raw")));
    assertTrue(files.contains(folder.resolve("otherdirectory/A_OF_20241118_ROB_01_0.raw")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_FolderNotExists() {
    Sample sample = repository.findById(1L).orElseThrow();

    List<Path> files = service.files(sample);

    verify(configuration.getHome()).folder(sample);
    assertTrue(files.isEmpty());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_Archives() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("sample_h_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    folder = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(folder);
    file = folder.resolve("sample_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("sample_R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".deleted");
    Files.createFile(file);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    folder = configuration.getArchives().get(1).folder(sample);
    Files.createDirectories(folder);
    file = folder.resolve("sample_a2_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);

    List<Path> files = service.files(sample);

    verify(configuration.getHome(), times(2)).folder(sample);
    verify(configuration.getArchives().get(0), times(2)).folder(sample);
    verify(configuration.getArchives().get(1), times(2)).folder(sample);
    assertEquals(4, files.size());
    folder = configuration.getHome().folder(sample);
    assertTrue(files.contains(folder.resolve("sample_h_R1.fastq")));
    folder = configuration.getArchives().get(0).folder(sample);
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    folder = configuration.getArchives().get(1).folder(sample);
    assertTrue(files.contains(folder.resolve("sample_a2_R1.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_ArchivesSameFilename() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("sample_R1.fastq"));
    folder = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("sample_R1.fastq"));
    folder = configuration.getArchives().get(1).folder(sample);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("sample_R1.fastq"));

    List<Path> files = service.files(sample);

    verify(configuration.getHome(), times(2)).folder(sample);
    assertEquals(3, files.size());
    folder = configuration.getHome().folder(sample);
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    folder = configuration.getArchives().get(0).folder(sample);
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    folder = configuration.getArchives().get(1).folder(sample);
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_Archives_Filenames() throws Throwable {
    Sample sample = repository.findById(4L).orElseThrow();
    Path folder = configuration.getArchives().get(0).getFolder().resolve("otherdirectory");
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("OF_20241118_ROB_01.raw"));
    Files.createFile(folder.resolve(".OF_20241118_ROB_01.raw"));
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(folder.resolve(".OF_20241118_ROB_01.raw"), "dos:hidden", Boolean.TRUE);
    }
    folder = configuration.getArchives().get(1).getFolder();
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("A_OF_20241118_ROB_01_0.raw"));

    List<Path> files = service.files(sample);

    verify(configuration.getHome()).getFolder();
    verify(configuration.getArchives().get(0), times(2)).getFolder();
    verify(configuration.getArchives().get(1), times(2)).getFolder();
    System.out.println(files);
    assertEquals(2, files.size());
    folder = configuration.getArchives().get(0).getFolder().resolve("otherdirectory");
    assertTrue(files.contains(folder.resolve("OF_20241118_ROB_01.raw")));
    folder = configuration.getArchives().get(1).getFolder();
    assertTrue(files.contains(folder.resolve("A_OF_20241118_ROB_01_0.raw")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_ArchivesNotExists() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("sample_R1.fastq"));

    List<Path> files = service.files(sample);

    verify(configuration.getHome(), times(2)).folder(sample);
    assertEquals(1, files.size());
    folder = configuration.getHome().folder(sample);
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void files_NewSample() {
    List<Path> files = service.files(new Sample());

    assertTrue(files.isEmpty());
  }

  @Test
  public void folderLabels() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(sample, false);

    assertEquals(1, labels.size());
    assertEquals("\\\\lanaseq01\\home\\" + sample.getName(), labels.get(0));
  }

  @Test
  public void folderLabels_Unix() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(sample, true);

    assertEquals(1, labels.size());
    assertEquals("//lanaseq01/home/" + sample.getName(), labels.get(0));
  }

  @Test
  public void folderLabels_Archives() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(1).folder(sample);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(sample, false);

    assertEquals(3, labels.size());
    assertEquals("\\\\lanaseq01\\home\\" + sample.getName(), labels.get(0));
    assertEquals("\\\\lanaseq01\\archives\\" + sample.getName(), labels.get(1));
    assertEquals("\\\\lanaseq02\\archives2\\" + sample.getName(), labels.get(2));
  }

  @Test
  public void folderLabels_Archives_Unix() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(folder);
    folder = configuration.getArchives().get(1).folder(sample);
    Files.createDirectories(folder);

    List<String> labels = service.folderLabels(sample, true);

    assertEquals(3, labels.size());
    assertEquals("//lanaseq01/home/" + sample.getName(), labels.get(0));
    assertEquals("//lanaseq01/archives/" + sample.getName(), labels.get(1));
    assertEquals("//lanaseq02/archives2/" + sample.getName(), labels.get(2));
  }

  @Test
  public void folderLabels_FoldersNotExists() {
    Sample sample = repository.findById(1L).orElseThrow();

    List<String> labels = service.folderLabels(sample, false);

    assertTrue(labels.isEmpty());
  }

  @Test
  public void folderLabels_NullId() {
    List<String> labels = service.folderLabels(new Sample(), false);

    assertTrue(labels.isEmpty());
  }

  @Test
  public void uploadFiles() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path upload = configuration.getUpload().getFolder();
    Files.createDirectories(upload);
    Path uploadFile = upload.resolve(sample.getName() + ".fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        uploadFile, StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        uploadFile, StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("." + sample.getName() + ".bed");
    Files.createFile(uploadFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(uploadFile, "dos:hidden", Boolean.TRUE);
    }
    uploadFile = upload.resolve(sample.getName());
    Files.createDirectory(uploadFile);
    Path folder = configuration.getUpload().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("sample_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("sample_R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    file = folder.resolve("folder");
    Files.createDirectory(file);

    List<Path> files = service.uploadFiles(sample);

    verify(configuration.getUpload(), times(2)).getFolder();
    verify(configuration.getUpload(), times(2)).folder(sample);
    verify(configuration.getArchives().get(0), never()).folder(sample);
    verify(configuration.getArchives().get(1), never()).folder(sample);
    assertEquals(3, files.size());
    assertTrue(files.contains(upload.resolve(sample.getName() + ".fastq")));
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void uploadFiles_SampleFolderNotExists() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path upload = configuration.getUpload().getFolder();
    Files.createDirectories(upload);
    Path uploadFile = upload.resolve(sample.getName() + ".fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        uploadFile, StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        uploadFile, StandardCopyOption.REPLACE_EXISTING);
    uploadFile = upload.resolve("." + sample.getName() + ".bed");
    Files.createFile(uploadFile);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(uploadFile, "dos:hidden", Boolean.TRUE);
    }
    uploadFile = upload.resolve(sample.getName());
    Files.createDirectory(uploadFile);

    List<Path> files = service.uploadFiles(sample);

    verify(configuration.getUpload(), times(2)).getFolder();
    verify(configuration.getUpload()).folder(sample);
    verify(configuration.getArchives().get(0), never()).folder(sample);
    verify(configuration.getArchives().get(1), never()).folder(sample);
    assertEquals(1, files.size());
    assertTrue(files.contains(upload.resolve(sample.getName() + ".fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void uploadFiles_UploadFolderNotExists() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    Path folder = configuration.getUpload().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("sample_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve("sample_R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    file = folder.resolve(".hiddenFile");
    Files.createFile(file);
    if (SystemUtils.IS_OS_WINDOWS) {
      Files.setAttribute(file, "dos:hidden", Boolean.TRUE);
    }
    file = folder.resolve("folder");
    Files.createDirectory(file);

    List<Path> files = service.uploadFiles(sample);

    verify(configuration.getUpload()).getFolder();
    verify(configuration.getUpload(), times(2)).folder(sample);
    verify(configuration.getArchives().get(0), never()).folder(sample);
    verify(configuration.getArchives().get(1), never()).folder(sample);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void uploadFiles_NewSample() {
    List<Path> files = service.uploadFiles(new Sample());

    assertTrue(files.isEmpty());
  }

  @Test
  public void topKeywords() {
    List<String> keywords = service.topKeywords(4);
    assertEquals(4, keywords.size());
    assertTrue(keywords.contains("ip"));
    assertTrue(keywords.contains("chipseq"));
    assertTrue(keywords.contains("G24D"));
    assertTrue(keywords.contains("Spt16"));
  }

  @Test
  public void topAssays() {
    List<String> assays = service.topAssays(2);
    assertEquals(2, assays.size());
    assertTrue(assays.contains("MNase-seq"));
    assertTrue(assays.contains("ChIP-seq"));
  }

  @Test
  public void topAssays_limit() {
    List<String> assays = service.topAssays(1);
    assertEquals(1, assays.size());
    assertTrue(assays.contains("ChIP-seq"));
  }

  @Test
  public void topTypes() {
    List<String> types = service.topTypes(2);
    assertEquals(2, types.size());
    assertTrue(types.contains("IP"));
    assertTrue(types.contains("Input"));
  }

  @Test
  public void topTypes_limit() {
    List<String> types = service.topTypes(1);
    assertEquals(1, types.size());
    assertTrue(types.contains("Input"));
  }

  @Test
  public void isDeletable_FalseNotEditable() {
    Sample sample = repository.findById(9L).orElseThrow();
    sample.setEditable(false);
    assertFalse(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void isDeletable_FalseLinkedToDataset() {
    Sample sample = repository.findById(1L).orElseThrow();
    assertFalse(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void isDeletable_True() {
    Sample sample = repository.findById(9L).orElseThrow();
    assertTrue(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  public void isDeletable_NewSample() {
    assertFalse(service.isDeletable(new Sample()));
  }

  @Test
  public void isMergable_False() {
    List<Sample> samples = new ArrayList<>();
    samples.add(repository.findById(1L).orElseThrow());
    samples.add(repository.findById(4L).orElseThrow());
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_True() {
    List<Sample> samples = new ArrayList<>();
    samples.add(repository.findById(1L).orElseThrow());
    samples.add(repository.findById(2L).orElseThrow());
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_SameProperties() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    samples.add(sample());
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_DifferentProtocols() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setProtocol(protocolRepository.findById(2L).orElseThrow());
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_DifferentAssays() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setAssay("ChIP-exo");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_DifferentTypes() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setType("Input");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_OneTypeNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setType(null);
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_AllTypesNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    samples.add(sample());
    samples.forEach(s -> s.setType(null));
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_DifferentTargets() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setTarget("spt16");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_OneTargetNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setTarget(null);
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_AllTargetsNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    samples.add(sample());
    samples.forEach(s -> s.setTarget(null));
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_DifferentStrains() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setStrain("yFR513");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_DifferentStrainDescriptions() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setStrainDescription("R103S");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_OneStrainDescriptionNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setStrainDescription(null);
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_AllStrainDescriptionsNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    samples.add(sample());
    samples.forEach(s -> s.setStrainDescription(null));
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_DifferentTreatments() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setTreatment("rappa");
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_OneTreatmentNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    Sample sample = sample();
    sample.setTreatment(null);
    samples.add(sample);
    assertFalse(service.isMergable(samples));
  }

  @Test
  public void isMergable_AllTreatmentsNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(sample());
    samples.add(sample());
    samples.forEach(s -> s.setTreatment(null));
    assertTrue(service.isMergable(samples));
  }

  @Test
  public void isMergable_Empty() {
    assertFalse(service.isMergable(new ArrayList<>()));
  }

  @Test
  @WithAnonymousUser
  public void isMergable_Anonymous() {
    assertThrows(AccessDeniedException.class,
        () -> assertFalse(service.isMergable(new ArrayList<>())));
  }

  @Test
  public void save_New() {
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay("ChIP-seq");
    sample.setType("IP");
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setProtocol(protocolRepository.findById(1L).orElseThrow());
    sample.setDate(LocalDate.of(2020, 7, 21));
    sample.setNote("test note");
    sample.setKeywords(new HashSet<>());
    sample.getKeywords().add("keyword1");
    sample.getKeywords().add("keyword2");
    sample.generateName();

    service.save(sample);

    repository.flush();
    assertNotEquals(0, sample.getId());
    sample = repository.findById(sample.getId()).orElseThrow();
    assertEquals("my sample", sample.getSampleId());
    assertEquals("my replicate", sample.getReplicate());
    assertEquals("ChIP-seq", sample.getAssay());
    assertEquals("IP", sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals("test note", sample.getNote());
    assertEquals(2, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains("keyword1"));
    assertTrue(sample.getKeywords().contains("keyword2"));
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(3, sample.getOwner().getId());
    assertTrue(sample.isEditable());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getCreationDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getCreationDate()));
    assertEquals(LocalDate.of(2020, 7, 21), sample.getDate());
    assertEquals("mysample_ChIPseq_IP_mytarget_yFR213_F56G_37C_myreplicate_20200721",
        sample.getName());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  public void save_NewNoName() {
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay("ChIP-seq");
    sample.setType("IP");
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setProtocol(protocolRepository.findById(1L).orElseThrow());
    sample.setDate(LocalDate.of(2020, 7, 21));
    sample.setNote("test note");

    assertThrows(NullPointerException.class, () -> service.save(sample));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void save_Update() {
    Sample sample = repository.findById(1L).orElseThrow();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay("ChIP-seq");
    sample.setType("Input");
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setProtocol(protocolRepository.findById(3L).orElseThrow());
    sample.setDate(LocalDate.of(2020, 7, 21));
    sample.setNote("test note");
    sample.getKeywords().remove("ip");
    sample.getKeywords().add("keyword1");
    sample.getKeywords().add("keyword2");
    sample.generateName();

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElseThrow();
    assertEquals("my sample", sample.getSampleId());
    assertEquals("my replicate", sample.getReplicate());
    assertEquals("ChIP-seq", sample.getAssay());
    assertEquals("Input", sample.getType());
    assertEquals("my target", sample.getTarget());
    assertEquals("yFR213", sample.getStrain());
    assertEquals("F56G", sample.getStrainDescription());
    assertEquals("37C", sample.getTreatment());
    assertEquals("test note", sample.getNote());
    assertEquals(3, sample.getKeywords().size());
    assertTrue(sample.getKeywords().contains("mnase"));
    assertTrue(sample.getKeywords().contains("keyword1"));
    assertTrue(sample.getKeywords().contains("keyword2"));
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
  public void save_DontRenameDatasets() {
    Sample sample = repository.findById(4L).orElseThrow();
    sample.setSampleId("sample1");
    sample.setReplicate("r1");
    sample.generateName();

    service.save(sample);

    repository.flush();
    Dataset dataset = datasetRepository.findById(2L).orElseThrow();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022", dataset.getName());
    assertEquals(2, dataset.getSamples().size());
    assertEquals((Long) 4L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 5L, dataset.getSamples().get(1).getId());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), dataset.getCreationDate());
    dataset = datasetRepository.findById(6L).orElseThrow();
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1_20181208", dataset.getName());
    assertEquals(1, dataset.getSamples().size());
    assertEquals((Long) 4L, dataset.getSamples().get(0).getId());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 12, 8, 10, 28, 23), dataset.getCreationDate());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void save_UpdateNotEditable() {
    assertThrows(IllegalArgumentException.class, () -> {
      Sample sample = repository.findById(8L).orElseThrow();
      service.save(sample);
    });
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void save_UpdateMoveFiles() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    detach(sample);
    Path beforeFolder = configuration.getHome().folder(sample);
    Path beforeArchive1 = configuration.getArchives().get(0).folder(sample);
    Path beforeArchive2 = configuration.getArchives().get(1).folder(sample);
    sample.setName("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020");
    Files.createDirectories(beforeFolder);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeFolder.resolve("sample_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        beforeFolder.resolve("sample_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.createDirectories(beforeArchive1);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeArchive1.resolve("sample_a1_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.createDirectories(beforeArchive2);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeArchive2.resolve("sample_a2_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElseThrow();
    assertEquals("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020",
        sample.getName());
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder.resolve("sample_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve("sample_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("sample_R2.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve("sample_R2.fastq")));
    assertFalse(Files.exists(beforeFolder));
    Path archive1 = configuration.getArchives().get(0).folder(sample);
    assertTrue(Files.exists(archive1.resolve("sample_a1_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(archive1.resolve("sample_a1_R1.fastq")));
    assertFalse(Files.exists(beforeArchive1));
    Path archive2 = configuration.getArchives().get(1).folder(sample);
    assertTrue(Files.exists(archive2.resolve("sample_a2_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(archive2.resolve("sample_a2_R1.fastq")));
    assertFalse(Files.exists(beforeArchive2));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void save_UpdateMoveFilesParentNotExists() throws Throwable {
    when(configuration.getHome().folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null ? temporaryFolder.resolve(String.valueOf(sample.getDate().getYear()))
          .resolve(sample.getName()) : null;
    });
    Sample sample = repository.findById(1L).orElseThrow();
    detach(sample);
    Path beforeFolder = configuration.getHome().folder(sample);
    Path beforeArchive1 = configuration.getArchives().get(0).folder(sample);
    Path beforeArchive2 = configuration.getArchives().get(1).folder(sample);
    sample.setName("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020");
    Files.createDirectories(beforeFolder);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeFolder.resolve("sample_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        beforeFolder.resolve("sample_R2.fastq"), StandardCopyOption.REPLACE_EXISTING);
    sample.setDate(LocalDate.of(2020, 1, 12));
    Files.createDirectories(beforeArchive1);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeArchive1.resolve("sample_a1_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);
    Files.createDirectories(beforeArchive2);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeArchive2.resolve("sample_a2_R1.fastq"), StandardCopyOption.REPLACE_EXISTING);

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElseThrow();
    assertEquals("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020",
        sample.getName());
    assertEquals(LocalDate.of(2020, 1, 12), sample.getDate());
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder.resolve("sample_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve("sample_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("sample_R2.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve("sample_R2.fastq")));
    assertFalse(Files.exists(beforeFolder));
    Path archive1 = configuration.getArchives().get(0).folder(sample);
    assertTrue(Files.exists(archive1.resolve("sample_a1_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(archive1.resolve("sample_a1_R1.fastq")));
    assertFalse(Files.exists(beforeArchive1));
    Path archive2 = configuration.getArchives().get(1).folder(sample);
    assertTrue(Files.exists(archive2.resolve("sample_a2_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(archive2.resolve("sample_a2_R1.fastq")));
    assertFalse(Files.exists(beforeArchive2));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void save_RenameFiles() throws Throwable {
    Sample sample = repository.findById(1L).orElseThrow();
    detach(sample);
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    Path beforeFolder = configuration.getHome().folder(sample);
    Path beforeArchive1 = configuration.getArchives().get(0).folder(sample);
    Path beforeArchive2 = configuration.getArchives().get(1).folder(sample);
    sample.setName("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020");
    Files.createDirectories(beforeFolder);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.writeString(
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq.md5"),
        "e254a11d5102c5555232c3d7d0a53a0b  "
            + "FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R2.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.writeString(
        beforeFolder.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R2.fastq.md5"),
        "c0f5c3b76104640e306fce3c669f300e  "
            + "FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R2.fastq");
    Files.createDirectories(beforeArchive1);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeArchive1.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.writeString(
        beforeArchive1.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq.md5"),
        "e254a11d5102c5555232c3d7d0a53a0b  "
            + "FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq");
    Files.createDirectories(beforeArchive2);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        beforeArchive2.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.writeString(
        beforeArchive2.resolve("FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq.md5"),
        "e254a11d5102c5555232c3d7d0a53a0b  "
            + "FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020_R1.fastq");

    service.save(sample);

    repository.flush();
    sample = repository.findById(1L).orElseThrow();
    assertEquals("mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020",
        sample.getName());
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(
            "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5")));
    List<String> md5Lines = Files.readAllLines(folder.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals("e254a11d5102c5555232c3d7d0a53a0b  "
            + "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq",
        md5Lines.get(0));
    assertTrue(Files.exists(folder.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve(
            "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq")));
    assertTrue(Files.exists(folder.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq.md5")));
    md5Lines = Files.readAllLines(folder.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals("c0f5c3b76104640e306fce3c669f300e  "
            + "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R2.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeFolder));
    Path archive1 = configuration.getArchives().get(0).folder(sample);
    assertTrue(Files.exists(archive1.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(archive1.resolve(
            "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertTrue(Files.exists(archive1.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5")));
    md5Lines = Files.readAllLines(archive1.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals("e254a11d5102c5555232c3d7d0a53a0b  "
            + "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeArchive1));
    Path archive2 = configuration.getArchives().get(1).folder(sample);
    assertTrue(Files.exists(archive2.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(archive2.resolve(
            "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq")));
    assertTrue(Files.exists(archive2.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5")));
    md5Lines = Files.readAllLines(archive2.resolve(
        "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq.md5"));
    assertEquals(1, md5Lines.size());
    assertEquals("e254a11d5102c5555232c3d7d0a53a0b  "
            + "mysample_MNaseseq_IP_polr2a_yFR100_WT_Rappa_myreplicate_20181020_R1.fastq",
        md5Lines.get(0));
    assertFalse(Files.exists(beforeArchive2));
  }

  @Test
  public void saveFiles() throws Throwable {
    final Sample sample = repository.findById(1L).orElseThrow();
    List<Path> files = new ArrayList<>();
    Path file = temporaryFolder.resolve("sample_R1.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        file);
    files.add(file);
    file = temporaryFolder.resolve("sample_R2.fastq");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI()),
        file);
    files.add(file);

    service.saveFiles(sample, files);

    verify(configuration.getHome()).folder(sample);
    verify(configuration.getArchives().get(0), never()).folder(sample);
    verify(configuration.getArchives().get(1), never()).folder(sample);
    Path folder = configuration.getHome().folder(sample);
    assertTrue(Files.exists(folder.resolve("sample_R1.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI())),
        Files.readAllBytes(folder.resolve("sample_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("sample_R2.fastq")));
    assertArrayEquals(Files.readAllBytes(
            Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R2.fastq")).toURI())),
        Files.readAllBytes(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  public void delete() throws Throwable {
    Sample sample = repository.findById(9L).get();
    Path folder = configuration.getHome().folder(sample);
    Path file = folder.resolve("R1.fastq");
    Files.createDirectories(folder);
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
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
      Sample sample = repository.findById(1L).orElseThrow();
      service.delete(sample);
    });
  }

  @Test
  public void delete_NotEditable() {
    assertThrows(IllegalArgumentException.class, () -> {
      Sample sample = repository.findById(9L).orElseThrow();
      sample.setEditable(false);
      service.delete(sample);
    });
  }

  @Test
  public void deleteFile() throws Throwable {
    Sample sample = repository.findById(9L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = folder.resolve("test.txt");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(file, FileTime.from(toInstant(modifiedTime)));

    service.deleteFile(sample, file);

    verify(configuration.getHome(), times(2)).folder(sample);
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
    Sample sample = repository.findById(9L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = Paths.get("test.txt");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        folder.resolve(file), StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(folder.resolve(file), FileTime.from(toInstant(modifiedTime)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(sample, file));
  }

  @Test
  public void deleteFile_Archives() throws Throwable {
    Sample sample = repository.findById(9L).orElseThrow();
    Path folder = configuration.getArchives().get(0).folder(sample);
    Files.createDirectories(folder);
    Path file = Paths.get("test.txt");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()),
        folder.resolve(file), StandardCopyOption.REPLACE_EXISTING);
    LocalDateTime modifiedTime = LocalDateTime.now().minusDays(2).withNano(0);
    Files.setLastModifiedTime(folder.resolve(file), FileTime.from(toInstant(modifiedTime)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(sample, file));
  }

  @Test
  public void deleteFile_NotInSampleFolder() throws Throwable {
    Sample sample = repository.findById(9L).orElseThrow();
    Path file = temporaryFolder.resolve("test.txt");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    Files.setLastModifiedTime(file,
        FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(sample, file));
  }

  @Test
  public void deleteFile_RelativePathNotInSampleFolder() throws Throwable {
    Sample sample = repository.findById(9L).orElseThrow();
    Path folder = configuration.getHome().folder(sample);
    Files.createDirectories(folder);
    Path file = Paths.get("../test.txt");
    Files.copy(
        Paths.get(Objects.requireNonNull(getClass().getResource("/sample/R1.fastq")).toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    Files.setLastModifiedTime(file,
        FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

    assertThrows(IllegalArgumentException.class, () -> service.deleteFile(sample, file));
  }
}
