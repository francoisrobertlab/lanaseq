package ca.qc.ircm.lanaseq.sample;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
  private UserRepository userRepository;
  @MockBean
  private AppConfiguration configuration;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  @MockBean
  private AuthorizationService authorizationService;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    when(configuration.folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return sample != null && sample.getName() != null
          ? temporaryFolder.getRoot().toPath().resolve(sample.getName())
          : null;
    });
  }

  @Test
  @WithMockUser
  public void get() {
    Sample sample = service.get(1L);

    assertEquals((Long) 1L, sample.getId());
    assertEquals("FR1_MNaseSeq_IP_polr2a_yFR100_WT_Rappa_R1_20181020", sample.getName());
    assertEquals("FR1", sample.getSampleId());
    assertEquals("R1", sample.getReplicate());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getDate());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals((Long) 2L, sample.getOwner().getId());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Null() {
    Sample sample = service.get(null);
    assertNull(sample);
  }

  @Test
  @WithMockUser
  public void all() {
    List<Sample> samples = service.all();

    assertEquals(9, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
    assertTrue(find(samples, 4L).isPresent());
    assertTrue(find(samples, 5L).isPresent());
    assertTrue(find(samples, 6L).isPresent());
    assertTrue(find(samples, 7L).isPresent());
    assertTrue(find(samples, 8L).isPresent());
    assertTrue(find(samples, 9L).isPresent());
    for (Sample sample : samples) {
      verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
    }
  }

  @Test
  @WithMockUser
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

    List<Path> files = service.files(sample);

    verify(configuration, times(2)).folder(sample);
    assertEquals(2, files.size());
    assertTrue(files.contains(folder.resolve("sample_R1.fastq")));
    assertTrue(files.contains(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  @WithMockUser
  public void files_FolderNotExists() throws Throwable {
    Sample sample = repository.findById(1L).orElse(null);

    List<Path> files = service.files(sample);

    verify(configuration).folder(sample);
    assertTrue(files.isEmpty());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  @WithMockUser
  public void files_NullId() throws Throwable {
    List<Path> files = service.files(new Sample());

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
  public void isDeletable_False() {
    Sample sample = repository.findById(1L).get();
    assertFalse(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  @WithMockUser
  public void isDeletable_True() {
    Sample sample = repository.findById(9L).get();
    assertTrue(service.isDeletable(sample));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(READ));
  }

  @Test
  @WithMockUser
  public void isDeletable_Null() {
    assertFalse(service.isDeletable(null));
  }

  @Test
  @WithMockUser
  public void isDeletable_NullId() {
    assertFalse(service.isDeletable(new Sample()));
  }

  @Test
  @WithMockUser
  public void isMergable_False() {
    List<Sample> samples = new ArrayList<>();
    samples.add(repository.findById(1L).get());
    samples.add(repository.findById(4L).get());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
  public void isMergable_True() {
    List<Sample> samples = new ArrayList<>();
    samples.add(repository.findById(1L).get());
    samples.add(repository.findById(2L).get());
    assertTrue(service.isMergable(samples));
  }

  @Test
  @WithMockUser
  public void isMergable_AllNull() {
    List<Sample> samples = new ArrayList<>();
    samples.add(new Sample());
    samples.add(new Sample());
    assertTrue(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
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
  @WithMockUser
  public void isMergable_ProtocolOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setProtocol(new Protocol(1L));
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
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
  @WithMockUser
  public void isMergable_AssayOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setAssay(Assay.CHIP_SEQ);
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
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
  @WithMockUser
  public void isMergable_TypeOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setType(SampleType.INPUT);
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
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
  @WithMockUser
  public void isMergable_TargetOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTarget("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
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
  @WithMockUser
  public void isMergable_StrainOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrain("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
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
  @WithMockUser
  public void isMergable_StrainDescriptionOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setStrainDescription("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
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
  @WithMockUser
  public void isMergable_TreatmentOneNull() {
    List<Sample> samples = new ArrayList<>();
    Sample sample = new Sample();
    sample.setTreatment("test");
    samples.add(sample);
    samples.add(new Sample());
    assertFalse(service.isMergable(samples));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
  public void isMergable_Empty() {
    assertFalse(service.isMergable(new ArrayList<>()));
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void isMergable_Anonymous() {
    assertFalse(service.isMergable(new ArrayList<>()));
  }

  @Test
  @WithMockUser
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
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(sample.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(sample.getDate()));
    assertEquals("mysample_ChIPSeq_IP_mytarget_yFR213_F56G_37C_myreplicate_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(sample.getDate()), sample.getName());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  @WithMockUser
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
    assertEquals(LocalDateTime.of(2018, 10, 20, 13, 29, 23), sample.getDate());
    assertEquals("mysample_ChIPSeq_Input_mytarget_yFR213_F56G_37C_myreplicate_20181020",
        sample.getName());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  @WithMockUser
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
  @WithMockUser
  public void saveFiles() throws Throwable {
    Sample sample = repository.findById(1L).orElse(null);
    List<Path> files = new ArrayList<>();
    Path file = temporaryFolder.newFile("sample_R1.fastq").toPath();
    Files.copy(Paths.get(getClass().getResource("/sample/R1.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    files.add(file);
    file = temporaryFolder.newFile("sample_R2.fastq").toPath();
    Files.copy(Paths.get(getClass().getResource("/sample/R2.fastq").toURI()), file,
        StandardCopyOption.REPLACE_EXISTING);
    files.add(file);

    service.saveFiles(sample, files);

    verify(configuration).folder(sample);
    Path folder = configuration.folder(sample);
    assertTrue(Files.exists(folder.resolve("sample_R1.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R1.fastq").toURI())),
        Files.readAllBytes(folder.resolve("sample_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("sample_R2.fastq")));
    assertArrayEquals(
        Files.readAllBytes(Paths.get(getClass().getResource("/sample/R2.fastq").toURI())),
        Files.readAllBytes(folder.resolve("sample_R2.fastq")));
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void delete() {
    Sample sample = repository.findById(9L).get();
    service.delete(sample);
    repository.flush();
    assertFalse(repository.findById(9L).isPresent());
    verify(permissionEvaluator).hasPermission(any(), eq(sample), eq(WRITE));
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  public void delete_NotDeletable() {
    Sample sample = repository.findById(1L).get();
    service.delete(sample);
  }
}
