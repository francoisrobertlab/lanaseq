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

package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
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
  private AuthorizationService authorizationService;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  private Random random = new Random();
  private User currentUser;

  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    currentUser = userRepository.getOne(3L);
    when(authorizationService.getCurrentUser()).thenReturn(currentUser);
  }

  @Test
  @WithMockUser
  public void get() throws Throwable {
    Protocol protocol = service.get(1L);

    assertEquals((Long) 1L, protocol.getId());
    assertEquals("FLAG", protocol.getName());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Null() {
    Protocol protocol = service.get(null);
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

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void nameExists_Anonymous() {
    service.nameExists("test protocol");
  }

  @Test
  @WithMockUser
  public void all() {
    List<Protocol> protocols = service.all();

    assertEquals(3, protocols.size());
    assertTrue(find(protocols, 1L).isPresent());
    assertTrue(find(protocols, 2L).isPresent());
    assertTrue(find(protocols, 3L).isPresent());
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
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), file.getDate());
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
    assertEquals(LocalDateTime.of(2018, 10, 21, 9, 58, 12), file.getDate());
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
    assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getDate());
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
    assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
  }

  @Test(expected = AccessDeniedException.class)
  @WithMockUser
  public void deletedFiles_User() throws Throwable {
    Protocol protocol = repository.findById(3L).get();
    service.deletedFiles(protocol);
  }

  @Test
  @WithMockUser(authorities = { UserRole.USER, UserRole.MANAGER })
  public void deletedFiles_Null() throws Throwable {
    List<ProtocolFile> files = service.deletedFiles(null);
    assertTrue(files.isEmpty());
  }

  @Test
  @WithMockUser
  public void save_New() {
    Protocol protocol = new Protocol();
    protocol.setName("New protocol");
    byte[] content = new byte[5120];
    random.nextBytes(content);
    ProtocolFile file = new ProtocolFile();
    file.setFilename("New protocol file.docx");
    file.setContent(content);

    service.save(protocol, Collections.nCopies(1, file));

    repository.flush();
    assertNotNull(protocol.getId());
    Protocol database = repository.findById(protocol.getId()).orElse(null);
    assertEquals(protocol.getName(), database.getName());
    assertEquals(currentUser.getId(), database.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(protocol.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(protocol.getDate()));
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(1, files.size());
    file = files.get(0);
    assertNotNull(file.getId());
    assertEquals("New protocol file.docx", file.getFilename());
    assertArrayEquals(content, file.getContent());
    assertFalse(file.isDeleted());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(file.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(file.getDate()));
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  public void save_New_NoFile() {
    Protocol protocol = new Protocol();
    protocol.setName("New protocol");

    service.save(protocol, Collections.emptyList());
  }

  @Test
  @WithMockUser
  public void save_Update() throws Throwable {
    Protocol protocol = repository.findById(1L).orElse(null);
    protocol.setName("New name");
    byte[] content = new byte[5120];
    random.nextBytes(content);
    ProtocolFile file = new ProtocolFile();
    file.setFilename("New protocol file.docx");
    file.setContent(content);

    service.save(protocol, Collections.nCopies(1, file));

    repository.flush();
    protocol = repository.findById(1L).orElse(null);
    assertEquals("New name", protocol.getName());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getDate());
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
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), file.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
    file = files.get(1);
    assertNotNull(file.getId());
    assertEquals("New protocol file.docx", file.getFilename());
    assertArrayEquals(content, file.getContent());
    assertFalse(file.isDeleted());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(file.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(file.getDate()));
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
    assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getDate());
  }
}
