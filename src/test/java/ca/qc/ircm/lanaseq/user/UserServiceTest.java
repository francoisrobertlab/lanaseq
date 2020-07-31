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

package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private UserService service;
  @Autowired
  private UserRepository repository;
  @MockBean
  private PasswordEncoder passwordEncoder;
  @MockBean
  private AuthorizationService authorizationService;
  @MockBean
  private PermissionEvaluator permissionEvaluator;
  private String hashedPassword = "4k7GCUVUzV5zL74V867q";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(passwordEncoder.encode(any())).thenReturn(hashedPassword);
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
  }

  @Test
  @WithMockUser
  public void get() {
    User user = service.get(1L);

    assertNotNull(user);
    assertEquals((Long) 1L, user.getId());
    assertEquals("LANAseq Administrator", user.getName());
    assertEquals("lanaseq@ircm.qc.ca", user.getEmail());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2, user.getHashedPassword());
    assertEquals(1, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(4, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minus(4, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals(true, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertNull(user.getLocale());
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 30, 0), user.getDate());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_Invalid() {
    User user = service.get(0L);

    assertNull(user);
  }

  @Test
  @WithMockUser
  public void get_Null() {
    User user = service.get(null);

    assertNull(user);
  }

  @Test
  @WithMockUser
  public void getByEmail() {
    User user = service.getByEmail("francois.robert@ircm.qc.ca");

    assertNotNull(user);
    assertEquals((Long) 2L, user.getId());
    assertEquals("Francois Robert", user.getName());
    assertEquals("francois.robert@ircm.qc.ca", user.getEmail());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(2, ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minus(2, ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(Locale.ENGLISH, user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(READ));
  }

  @Test
  @WithMockUser
  public void getByEmail_Invalid() {
    User user = service.getByEmail("a");

    assertNull(user);
  }

  @Test
  @WithMockUser
  public void getByEmail_Null() {
    User user = service.getByEmail(null);

    assertNull(user);
  }

  @Test
  public void exists_Email_True() throws Throwable {
    boolean exists = service.exists("christian.poitras@ircm.qc.ca");

    assertEquals(true, exists);

    verifyNoInteractions(authorizationService);
  }

  @Test
  public void exists_Email_False() throws Throwable {
    boolean exists = service.exists("abc@ircm.qc.ca");

    assertEquals(false, exists);

    verifyNoInteractions(authorizationService);
  }

  @Test
  public void exists_Email_Null() throws Throwable {
    boolean exists = service.exists(null);

    assertEquals(false, exists);
  }

  @Test
  @WithMockUser
  public void all() {
    when(authorizationService.getCurrentUser()).thenReturn(repository.findById(3L).get());

    List<User> users = service.all();

    assertEquals(9, users.size());
    assertTrue(find(users, 1L).isPresent());
    assertTrue(find(users, 2L).isPresent());
    assertTrue(find(users, 3L).isPresent());
    for (User user : users) {
      verify(permissionEvaluator).hasPermission(any(), eq(user), eq(READ));
    }
  }

  @Test
  @WithMockUser
  public void save_AddAdmin() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setAdmin(true);

    service.save(user, "password");

    assertNotNull(user.getId());
    user = repository.findById(user.getId()).get();
    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("password");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(true, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertNull(user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_AddUser() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.ENGLISH);

    service.save(user, "password");

    assertNotNull(user.getId());
    user = repository.findById(user.getId()).get();
    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("password");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(Locale.ENGLISH, user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_AddManager() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setManager(true);

    service.save(user, "password");

    assertNotNull(user.getId());
    user = repository.findById(user.getId()).get();
    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("password");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertNull(user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(WRITE));
  }

  @Test
  @WithMockUser
  public void save_AddNullPassword() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.ENGLISH);

    service.save(user, null);

    assertNotNull(user.getId());
    user = repository.findById(user.getId()).get();
    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertNull(user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(Locale.ENGLISH, user.getLocale());
  }

  @Test
  @WithMockUser
  public void save_Update() {
    User user = repository.findById(6L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    service.save(user, "newpassword");

    user = repository.findById(6L).get();
    assertEquals((Long) 6L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(3, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(20, ChronoUnit.MINUTES).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minus(20, ChronoUnit.MINUTES).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getDate());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(WRITE));
    verify(authorizationService).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_UpdateKeepPassword() {
    User user = repository.findById(6L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    service.save(user, null);

    user = repository.findById(6L).orElse(null);
    assertEquals((Long) 6L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getHashedPassword());
    assertEquals(3, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(20, ChronoUnit.MINUTES).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minus(20, ChronoUnit.MINUTES).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(true, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getDate());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(WRITE));
    verify(authorizationService, never()).reloadAuthorities();
  }

  @Test(expected = AccessDeniedException.class)
  @WithMockUser
  public void save_UpdateFirstUserRemoveAdmin() {
    User user = repository.findById(1L).get();
    user.setAdmin(false);

    service.save(user, "newpassword");
  }

  @Test(expected = AccessDeniedException.class)
  @WithMockUser
  public void save_UpdateFirstUserRemoveActive() {
    User user = repository.findById(1L).get();
    user.setActive(false);

    service.save(user, "newpassword");
  }

  @Test(expected = NullPointerException.class)
  @WithMockUser
  public void save_Null() {
    service.save(null, null);
  }

  @Test
  @WithMockUser
  public void save_Password() {
    User user = repository.findById(6L).get();
    when(authorizationService.getCurrentUser()).thenReturn(user);

    service.save("newpassword");

    user = repository.findById(6L).get();
    assertEquals("Christian Poitras", user.getName());
    assertEquals("christian.poitras@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(3, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(20, ChronoUnit.MINUTES).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minus(20, ChronoUnit.MINUTES).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getDate());
    assertNull(user.getLocale());
    verify(authorizationService).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_PasswordNoAuthorityChange() {
    User user = repository.findById(3L).get();
    when(authorizationService.getCurrentUser()).thenReturn(user);

    service.save("newpassword");

    user = repository.findById(3L).get();
    assertEquals("Jonh Smith", user.getName());
    assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(2, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minus(10, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 48, 47), user.getDate());
    assertNull(user.getLocale());
    verify(authorizationService, never()).reloadAuthorities();
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void save_PasswordAnonymousDenied() {
    User user = repository.findById(3L).get();
    when(authorizationService.getCurrentUser()).thenReturn(user);

    service.save("new password");
  }

  @Test(expected = NullPointerException.class)
  @WithMockUser
  public void save_PasswordNull() {
    User user = repository.findById(3L).get();
    when(authorizationService.getCurrentUser()).thenReturn(user);

    service.save(null);
  }
}
