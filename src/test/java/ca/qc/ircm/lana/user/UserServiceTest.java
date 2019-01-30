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

package ca.qc.ircm.lana.user;

import static ca.qc.ircm.lana.test.utils.SearchUtils.find;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserServiceTest {
  @Inject
  private UserService userService;
  @Inject
  private UserRepository userRepository;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  @MockBean
  private PasswordEncoder passwordEncoder;
  @MockBean
  private AuthorizationService authorizationService;
  private String hashedPassword = "4k7GCUVUzV5zL74V867q";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(passwordEncoder.encode(any())).thenReturn(hashedPassword);
    when(authorizationService.hasRole(any())).thenReturn(true);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
  }

  @Test
  @WithMockUser
  public void get() {
    User user = userService.get(1L);

    assertNotNull(user);
    assertEquals((Long) 1L, user.getId());
    assertEquals("Lana Administrator", user.getName());
    assertEquals("lana@ircm.qc.ca", user.getEmail());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2, user.getHashedPassword());
    assertEquals(1, user.getSignAttempts());
    assertTrue(Instant.now().minus(4, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(4, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals(true, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertNull(user.getLocale());
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 30, 0), user.getDate());
    verify(authorizationService).hasPermission(user, BasePermission.READ);
  }

  @Test
  @WithMockUser
  public void get_Invalid() {
    User user = userService.get(0L);

    assertNull(user);
  }

  @Test
  @WithMockUser
  public void get_Null() {
    User user = userService.get(null);

    assertNull(user);
  }

  @Test
  @WithMockUser
  public void getByEmail() {
    User user = userService.getByEmail("francois.robert@ircm.qc.ca");

    assertNotNull(user);
    assertEquals((Long) 2L, user.getId());
    assertEquals("Francois Robert", user.getName());
    assertEquals("francois.robert@ircm.qc.ca", user.getEmail());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minus(2, ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(2, ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals((Long) 2L, user.getLaboratory().getId());
    assertEquals(Locale.ENGLISH, user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.READ);
  }

  @Test
  @WithMockUser
  public void getByEmail_Invalid() {
    User user = userService.getByEmail("a");

    assertNull(user);
  }

  @Test
  @WithMockUser
  public void getByEmail_Null() {
    User user = userService.getByEmail(null);

    assertNull(user);
  }

  @Test
  @WithMockUser
  public void all() {
    when(authorizationService.currentUser()).thenReturn(userRepository.findById(3L).get());

    List<User> users = userService.all();

    assertEquals(9, users.size());
    assertTrue(find(users, 1L).isPresent());
    assertTrue(find(users, 2L).isPresent());
    assertTrue(find(users, 3L).isPresent());
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void all_AnonymousDenied() {
    userService.all();
  }

  @Test
  @WithMockUser
  public void all_Laboratory() {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);

    List<User> users = userService.all(laboratory);

    assertEquals(3, users.size());
    assertTrue(find(users, 2L).isPresent());
    assertTrue(find(users, 3L).isPresent());
    assertTrue(find(users, 9L).isPresent());
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void all_LaboratoryAnonymousDenied() {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    userService.all(laboratory);
  }

  @Test
  @WithMockUser
  public void manager() {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    User user = userService.manager(laboratory);
    assertEquals((Long) 2L, user.getId());
  }

  @Test
  @WithMockUser
  public void manager_OnlyActive() {
    Laboratory laboratory = laboratoryRepository.findById(3L).orElse(null);
    User user = userService.manager(laboratory);
    assertEquals((Long) 5L, user.getId());
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void manager_AnonymousDenied() {
    Laboratory laboratory = laboratoryRepository.findById(3L).orElse(null);
    userService.manager(laboratory);
  }

  @Test
  @WithMockUser
  public void save_AddAdmin() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setAdmin(true);
    user.setLaboratory(laboratoryRepository.findById(1L).get());

    userService.save(user, "password");

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
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
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertNull(user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  public void save_AddAdminNoLab() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setAdmin(true);

    userService.save(user, "password");
  }

  @Test
  @WithMockUser
  public void save_AddAdminManager() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setAdmin(true);
    user.setManager(true);
    user.setLaboratory(laboratoryRepository.findById(1L).get());

    userService.save(user, "password");

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
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
    assertEquals(true, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertNull(user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
  }

  @Test
  @WithMockUser
  public void save_AddUser() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLaboratory(laboratoryRepository.findById(2L).get());
    user.setLocale(Locale.ENGLISH);

    userService.save(user, "password");

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
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
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 2L, user.getLaboratory().getId());
    assertEquals(Locale.ENGLISH, user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  public void save_AddUserNoLab() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.ENGLISH);

    userService.save(user, "password");
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  public void save_AddUserLabIdNotExists() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.ENGLISH);
    user.setLaboratory(new Laboratory());
    user.getLaboratory().setId(0L);

    userService.save(user, "password");
  }

  @Test
  @WithMockUser
  public void save_AddManager() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setManager(true);
    user.setLaboratory(laboratoryRepository.findById(2L).get());

    userService.save(user, "password");

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
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
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 2L, user.getLaboratory().getId());
    assertNull(user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
  }

  @Test
  @WithMockUser
  public void save_AddManagerNewLab() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setManager(true);
    Laboratory laboratory = new Laboratory();
    laboratory.setName("Test Lab");
    user.setLaboratory(laboratory);

    userService.save(user, "password");

    assertNotNull(user.getId());
    assertNotNull(laboratory.getId());
    user = userRepository.findById(user.getId()).get();
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
    assertNotNull(user.getLaboratory());
    assertNotNull(user.getLaboratory().getId());
    assertEquals("Test Lab", user.getLaboratory().getName());
    assertNull(user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
  }

  @Test(expected = IllegalArgumentException.class)
  @WithMockUser
  public void save_AddManagerNoLab() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setManager(true);

    userService.save(user, "password");
  }

  @Test
  @WithMockUser
  public void save_AddNullPassword() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLaboratory(laboratoryRepository.findById(2L).get());
    user.setLocale(Locale.ENGLISH);

    userService.save(user, null);

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
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
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 2L, user.getLaboratory().getId());
    assertEquals(Locale.ENGLISH, user.getLocale());
  }

  @Test
  @WithMockUser
  public void save_Update() {
    User user = userRepository.findById(6L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    userService.save(user, "newpassword");

    user = userRepository.findById(6L).get();
    assertEquals((Long) 6L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(3, user.getSignAttempts());
    assertTrue(Instant.now().minus(20, ChronoUnit.MINUTES).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(20, ChronoUnit.MINUTES).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getDate());
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 3L, user.getLaboratory().getId());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
    verify(authorizationService).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_UpdateChangeLaboratory() {
    User user = userRepository.findById(3L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);
    user.setLaboratory(laboratoryRepository.findById(3L).get());

    userService.save(user, "newpassword");

    user = userRepository.findById(3L).get();
    assertEquals((Long) 3L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(2, user.getSignAttempts());
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 48, 47), user.getDate());
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 3L, user.getLaboratory().getId());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
    verify(authorizationService, never()).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_UpdateDeleteEmptyLaboratory() {
    User user = userRepository.findById(2L).get();
    user.setLaboratory(laboratoryRepository.findById(3L).get());
    userService.save(user, null);
    user = userRepository.findById(3L).get();
    user.setLaboratory(laboratoryRepository.findById(3L).get());
    userService.save(user, null);
    user = userRepository.findById(9L).get();
    user.setLaboratory(laboratoryRepository.findById(3L).get());
    userService.save(user, null);

    assertFalse(laboratoryRepository.findById(2L).isPresent());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
  }

  @Test
  @WithMockUser
  public void save_UpdateNewLaboratory() {
    User user = userRepository.findById(3L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setManager(true);
    user.setLocale(Locale.CHINESE);
    Laboratory laboratory = new Laboratory();
    laboratory.setName("Test Lab");
    user.setLaboratory(laboratory);

    userService.save(user, "newpassword");

    assertNotNull(laboratory.getId());
    user = userRepository.findById(3L).get();
    assertEquals((Long) 3L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(2, user.getSignAttempts());
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 48, 47), user.getDate());
    assertNotNull(user.getLaboratory());
    assertEquals("Test Lab", user.getLaboratory().getName());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
    verify(authorizationService, never()).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_UpdateKeepPassword() {
    User user = userRepository.findById(6L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    userService.save(user, null);

    user = userRepository.findById(6L).orElse(null);
    assertEquals((Long) 6L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getHashedPassword());
    assertEquals(3, user.getSignAttempts());
    assertTrue(Instant.now().minus(20, ChronoUnit.MINUTES).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(20, ChronoUnit.MINUTES).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(true, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getDate());
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 3L, user.getLaboratory().getId());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(authorizationService).hasPermission(user, BasePermission.WRITE);
    verify(authorizationService, never()).reloadAuthorities();
  }

  @Test(expected = AccessDeniedException.class)
  @WithMockUser
  public void save_UpdateFirstUserRemoveAdmin() {
    User user = userRepository.findById(1L).get();
    user.setAdmin(false);

    userService.save(user, "newpassword");
  }

  @Test(expected = AccessDeniedException.class)
  @WithMockUser
  public void save_UpdateFirstUserRemoveActive() {
    User user = userRepository.findById(1L).get();
    user.setActive(false);

    userService.save(user, "newpassword");
  }

  @Test(expected = NullPointerException.class)
  @WithMockUser
  public void save_Null() {
    userService.save(null, null);
  }

  @Test
  @WithMockUser
  public void save_Password() {
    User user = userRepository.findById(6L).get();
    when(authorizationService.currentUser()).thenReturn(user);

    userService.save("newpassword");

    user = userRepository.findById(6L).get();
    assertEquals("Christian Poitras", user.getName());
    assertEquals("christian.poitras@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(3, user.getSignAttempts());
    assertTrue(Instant.now().minus(20, ChronoUnit.MINUTES).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(20, ChronoUnit.MINUTES).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getDate());
    assertEquals((Long) 3L, user.getLaboratory().getId());
    assertNull(user.getLocale());
    verify(authorizationService).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_PasswordNoAuthorityChange() {
    User user = userRepository.findById(3L).get();
    when(authorizationService.currentUser()).thenReturn(user);

    userService.save("newpassword");

    user = userRepository.findById(3L).get();
    assertEquals("Jonh Smith", user.getName());
    assertEquals("jonh.smith@ircm.qc.ca", user.getEmail());
    verify(passwordEncoder).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(2, user.getSignAttempts());
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertEquals(false, user.isAdmin());
    assertEquals(false, user.isExpiredPassword());
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 48, 47), user.getDate());
    assertEquals((Long) 2L, user.getLaboratory().getId());
    assertNull(user.getLocale());
    verify(authorizationService, never()).reloadAuthorities();
  }

  @Test(expected = AccessDeniedException.class)
  @WithAnonymousUser
  public void save_PasswordAnonymousDenied() {
    User user = userRepository.findById(3L).get();
    when(authorizationService.currentUser()).thenReturn(user);

    userService.save("new password");
  }

  @Test(expected = NullPointerException.class)
  @WithMockUser
  public void save_PasswordNull() {
    User user = userRepository.findById(3L).get();
    when(authorizationService.currentUser()).thenReturn(user);

    userService.save(null);
  }
}
