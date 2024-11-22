package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.findD;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link ProtocolService}.
 */
@ServiceTestAnnotations
public class ProtocolServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private ProtocolService service;
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  private Random random = new Random();
  private User currentUser;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    currentUser = userRepository.findById(3L).get();
    when(authenticatedUser.getUser()).thenReturn(Optional.of(currentUser));
  }

  @Test
  @WithMockUser
  public void get() throws Throwable {
    Protocol protocol = service.get(1L).orElse(null);

    assertEquals((Long) 1L, protocol.getId());
    assertEquals("FLAG", protocol.getName());
    assertEquals("First FLAG protocol", protocol.getNote());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Null() {
    Protocol protocol = service.get(null).orElse(null);
    assertNull(protocol);
  }

  @Test
  @WithMockUser
  public void nameExists_False() {
    assertFalse(service.nameExists("test protocol"));
  }

  @Test
  @WithMockUser
  public void nameExists_True() {
    assertTrue(service.nameExists("FLAG"));
  }

  @Test
  @WithMockUser
  public void nameExists_Null() {
    assertFalse(service.nameExists(null));
  }

  @Test
  @WithAnonymousUser
  public void nameExists_Anonymous() {
    assertThrows(AccessDeniedException.class, () -> {
      service.nameExists("test protocol");
    });
  }

  @Test
  @WithMockUser
  public void all() {
    List<Protocol> protocols = service.all();

    assertEquals(4, protocols.size());
    assertTrue(findD(protocols, 1L).isPresent());
    assertTrue(findD(protocols, 2L).isPresent());
    assertTrue(findD(protocols, 3L).isPresent());
    assertTrue(findD(protocols, 4L).isPresent());
    for (Protocol protocol : protocols) {
      verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void files() throws Throwable {
    Protocol protocol = repository.findById(1L).get();
    List<ProtocolFile> files = service.files(protocol);

    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals((Long) 1L, file.getId());
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI())),
        file.getContent());
    assertFalse(file.isDeleted());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), file.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser
  public void files_WithDeleted() throws Throwable {
    Protocol protocol = repository.findById(3L).get();
    List<ProtocolFile> files = service.files(protocol);

    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals((Long) 4L, file.getId());
    assertEquals("Histone Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx").toURI())),
        file.getContent());
    assertFalse(file.isDeleted());
    assertEquals(LocalDateTime.of(2018, 10, 21, 9, 58, 12), file.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser
  public void files_NoId() throws Throwable {
    List<ProtocolFile> files = service.files(new Protocol());

    assertTrue(files.isEmpty());
  }

  @Test
  @WithMockUser
  public void files_Null() throws Throwable {
    List<ProtocolFile> files = service.files(null);

    assertTrue(files.isEmpty());
  }

  @Test
  @WithMockUser(authorities = { UserRole.USER, UserRole.ADMIN })
  public void deletedFiles_Admin() throws Throwable {
    Protocol protocol = repository.findById(3L).get();
    List<ProtocolFile> files = service.deletedFiles(protocol);

    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals((Long) 3L, file.getId());
    assertEquals("Histone FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx").toURI())),
        file.getContent());
    assertTrue(file.isDeleted());
    assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser(authorities = { UserRole.USER, UserRole.MANAGER })
  public void deletedFiles_Manager() throws Throwable {
    Protocol protocol = repository.findById(3L).get();
    List<ProtocolFile> files = service.deletedFiles(protocol);

    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals((Long) 3L, file.getId());
    assertEquals("Histone FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx").toURI())),
        file.getContent());
    assertTrue(file.isDeleted());
    assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser
  public void deletedFiles_User() throws Throwable {
    assertThrows(AccessDeniedException.class, () -> {
      Protocol protocol = repository.findById(3L).get();
      service.deletedFiles(protocol);
    });
  }

  @Test
  @WithMockUser(authorities = { UserRole.USER, UserRole.MANAGER })
  public void deletedFiles_Null() throws Throwable {
    List<ProtocolFile> files = service.deletedFiles(null);
    assertTrue(files.isEmpty());
  }

  @Test
  @WithMockUser
  public void isDeletable_False() {
    Protocol protocol = repository.findById(1L).get();
    assertFalse(service.isDeletable(protocol));
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser
  public void isDeletable_True() {
    Protocol protocol = repository.findById(4L).get();
    assertTrue(service.isDeletable(protocol));
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser
  public void save_New() {
    Protocol protocol = new Protocol();
    protocol.setName("New protocol");
    protocol.setNote("test note");
    byte[] content = new byte[5120];
    random.nextBytes(content);
    ProtocolFile file = new ProtocolFile();
    file.setFilename("New protocol file.docx");
    file.setContent(content);

    service.save(protocol, Collections.nCopies(1, file));

    repository.flush();
    assertNotEquals(0, protocol.getId());
    Protocol database = repository.findById(protocol.getId()).orElse(null);
    assertEquals(protocol.getName(), database.getName());
    assertEquals(protocol.getNote(), database.getNote());
    assertEquals(currentUser.getId(), database.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(protocol.getCreationDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(protocol.getCreationDate()));
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(1, files.size());
    file = files.get(0);
    assertNotEquals(0, file.getId());
    assertEquals("New protocol file.docx", file.getFilename());
    assertArrayEquals(content, file.getContent());
    assertFalse(file.isDeleted());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(file.getCreationDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(file.getCreationDate()));
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_New_NoFile() {
    assertThrows(IllegalArgumentException.class, () -> {
      Protocol protocol = new Protocol();
      protocol.setName("New protocol");

      service.save(protocol, Collections.emptyList());
    });
  }

  @Test
  @WithMockUser
  public void save_Update() throws Throwable {
    Protocol protocol = repository.findById(1L).orElse(null);
    protocol.setName("New name");
    protocol.setNote("test note");
    byte[] content = new byte[5120];
    random.nextBytes(content);
    ProtocolFile file = new ProtocolFile();
    file.setFilename("New protocol file.docx");
    file.setContent(content);

    service.save(protocol, Collections.nCopies(1, file));

    repository.flush();
    protocol = repository.findById(1L).orElse(null);
    assertEquals("New name", protocol.getName());
    assertEquals("test note", protocol.getNote());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(2, files.size());
    file = files.get(0);
    assertEquals((Long) 1L, file.getId());
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI())),
        file.getContent());
    assertTrue(file.isDeleted());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), file.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
    file = files.get(1);
    assertNotEquals(0, file.getId());
    assertEquals("New protocol file.docx", file.getFilename());
    assertArrayEquals(content, file.getContent());
    assertFalse(file.isDeleted());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(file.getCreationDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(file.getCreationDate()));
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void recover() throws Throwable {
    Protocol protocol = repository.findById(3L).get();
    ProtocolFile file = fileRepository.findById(3L).get();

    service.recover(file);

    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
    assertEquals((Long) 3L, file.getId());
    assertEquals("Histone FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx").toURI())),
        file.getContent());
    assertFalse(file.isDeleted());
    assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getCreationDate());
  }

  @Test
  @WithMockUser
  public void delete() {
    Protocol protocol = repository.findById(4L).orElse(null);

    service.delete(protocol);

    repository.flush();
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
    assertFalse(repository.findById(4L).isPresent());
  }

  @Test
  @WithMockUser
  public void delete_LinkedToSample() {
    assertThrows(IllegalArgumentException.class, () -> {
      Protocol protocol = repository.findById(1L).orElse(null);
      service.delete(protocol);
    });
  }
}
