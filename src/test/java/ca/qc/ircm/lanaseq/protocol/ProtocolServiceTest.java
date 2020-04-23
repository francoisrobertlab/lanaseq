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

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.PermissionEvaluator;
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
  private UserRepository userRepository;
  @MockBean
  private AuthorizationService authorizationService;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  private Random random = new Random();

  @Before
  public void beforeTest() {
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
  }

  @Test
  @WithMockUser
  public void get() throws Throwable {
    Protocol protocol = service.get(1L);

    assertEquals((Long) 1L, protocol.getId());
    assertEquals("FLAG", protocol.getName());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getDate());
    assertEquals(1, protocol.getFiles().size());
    ProtocolFile file = protocol.getFiles().get(0);
    assertEquals((Long) 1L, file.getId());
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI())),
        file.getContent());
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
  public void all() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);

    List<Protocol> protocols = service.all();

    assertEquals(2, protocols.size());
    assertTrue(find(protocols, 1L).isPresent());
    assertTrue(find(protocols, 3L).isPresent());
    for (Protocol protocol : protocols) {
      verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void all_Manager() {
    User user = userRepository.findById(2L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    when(authorizationService.hasRole(MANAGER)).thenReturn(true);

    List<Protocol> protocols = service.all();

    assertEquals(2, protocols.size());
    assertTrue(find(protocols, 1L).isPresent());
    assertTrue(find(protocols, 3L).isPresent());
    for (Protocol protocol : protocols) {
      verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void all_Admin() {
    User user = userRepository.findById(1L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    when(authorizationService.hasRole(ADMIN)).thenReturn(true);

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
  public void save_New() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Protocol protocol = new Protocol();
    protocol.setName("New protocol");
    byte[] content = new byte[5120];
    random.nextBytes(content);
    ProtocolFile file = new ProtocolFile();
    file.setFilename("New protocol file.docx");
    file.setContent(content);
    protocol.setFiles(Arrays.asList(file));

    service.save(protocol);

    repository.flush();
    assertNotNull(protocol.getId());
    Protocol database = repository.findById(protocol.getId()).orElse(null);
    assertEquals(protocol.getName(), database.getName());
    assertEquals(user.getId(), database.getOwner().getId());
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(protocol.getDate()));
    assertTrue(LocalDateTime.now().plusSeconds(10).isAfter(protocol.getDate()));
    assertEquals(1, database.getFiles().size());
    file = database.getFiles().get(0);
    assertNotNull(file.getId());
    assertEquals("New protocol file.docx", file.getFilename());
    assertArrayEquals(content, file.getContent());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  public void save_New_NoFile() {
    User user = userRepository.findById(3L).orElse(null);
    when(authorizationService.getCurrentUser()).thenReturn(user);
    Protocol protocol = new Protocol();
    protocol.setName("New protocol");

    service.save(protocol);
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
    protocol.getFiles().remove(0);
    protocol.getFiles().add(file);

    service.save(protocol);

    repository.flush();
    protocol = repository.findById(1L).orElse(null);
    assertEquals("New name", protocol.getName());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getDate());
    assertEquals(1, protocol.getFiles().size());
    file = protocol.getFiles().get(0);
    assertNotNull(file.getId());
    assertEquals("New protocol file.docx", file.getFilename());
    assertArrayEquals(content, file.getContent());
    verify(permissionEvaluator).hasPermission(any(), eq(protocol), eq(WRITE));
  }
}
