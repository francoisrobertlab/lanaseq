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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.security.AuthenticationService;
import ca.qc.ircm.lana.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class UserServiceTest {
  private UserService userService;
  @Inject
  private UserRepository userRepository;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  @Mock
  private AuthenticationService authenticationService;
  private String hashedPassword = "hashedPassword for tests";

  @Before
  public void beforeTest() {
    userService = new UserService(userRepository, laboratoryRepository, authenticationService);
    when(authenticationService.encode(any())).thenReturn(hashedPassword);
  }

  @Test
  public void get() {
    User user = userService.get(1L);

    assertNotNull(user);
    assertEquals((Long) 1L, user.getId());
    assertEquals("Lana Administrator", user.getName());
    assertEquals("lana@ircm.qc.ca", user.getEmail());
    assertEquals(UserRole.ADMIN, user.getRole());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS2, user.getHashedPassword());
    assertEquals(1, user.getSignAttempts());
    assertTrue(Instant.now().minus(4, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(4, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertNull(user.getLaboratory());
    assertNull(user.getLocale());
  }

  @Test
  public void get_Invalid() {
    User user = userService.get(0L);

    assertNull(user);
  }

  @Test
  public void get_Null() {
    User user = userService.get(null);

    assertNull(user);
  }

  @Test
  public void getByEmail() {
    User user = userService.getByEmail("francois.robert@ircm.qc.ca");

    assertNotNull(user);
    assertEquals((Long) 2L, user.getId());
    assertEquals("Francois Robert", user.getName());
    assertEquals("francois.robert@ircm.qc.ca", user.getEmail());
    assertEquals(UserRole.BIOLOGIST, user.getRole());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().minus(2, ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(2, ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertEquals(Locale.ENGLISH, user.getLocale());
  }

  @Test
  public void getByEmail_Invalid() {
    User user = userService.getByEmail("a");

    assertNull(user);
  }

  @Test
  public void getByEmail_Null() {
    User user = userService.getByEmail(null);

    assertNull(user);
  }

  @Test
  public void all() {
    List<User> users = userService.all();

    assertEquals(6, users.size());
    assertTrue(find(users, 1L).isPresent());
    assertTrue(find(users, 2L).isPresent());
    assertTrue(find(users, 3L).isPresent());
    assertTrue(find(users, 4L).isPresent());
    assertTrue(find(users, 5L).isPresent());
    assertTrue(find(users, 6L).isPresent());
  }

  @Test
  public void save_AddAdmin() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setRole(UserRole.ADMIN);

    userService.save(user, "password");

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertEquals(UserRole.ADMIN, user.getRole());
    verify(authenticationService).encode("password");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertNull(user.getLaboratory());
    assertNull(user.getLocale());
  }

  @Test(expected = IllegalArgumentException.class)
  public void save_AddAdminWithLab() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setRole(UserRole.ADMIN);
    user.setLaboratory(laboratoryRepository.findById(1L).get());

    userService.save(user, "password");
  }

  @Test(expected = IllegalArgumentException.class)
  public void save_AddAdminManager() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setRole(UserRole.ADMIN);
    user.setManager(true);

    userService.save(user, "password");
  }

  @Test
  public void save_AddBiologist() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setLaboratory(laboratoryRepository.findById(1L).get());
    user.setLocale(Locale.ENGLISH);

    userService.save(user, "password");

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertEquals(UserRole.BIOLOGIST, user.getRole());
    verify(authenticationService).encode("password");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertEquals(Locale.ENGLISH, user.getLocale());
  }

  @Test(expected = IllegalArgumentException.class)
  public void save_AddBiologistNoLab() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setLocale(Locale.ENGLISH);

    userService.save(user, "password");
  }

  @Test
  public void save_AddBiologistManager() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
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
    assertEquals(UserRole.BIOLOGIST, user.getRole());
    verify(authenticationService).encode("password");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(true, user.isManager());
    assertNotNull(user.getLaboratory());
    assertNotNull(user.getLaboratory().getId());
    assertEquals("Test Lab", user.getLaboratory().getName());
    assertNull(user.getLocale());
  }

  @Test
  public void save_AddNullPassword() {
    User user = new User();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setRole(UserRole.BIOLOGIST);
    user.setLaboratory(laboratoryRepository.findById(1L).get());
    user.setLocale(Locale.ENGLISH);

    userService.save(user, null);

    assertNotNull(user.getId());
    user = userRepository.findById(user.getId()).get();
    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertEquals(UserRole.BIOLOGIST, user.getRole());
    assertNull(user.getHashedPassword());
    assertEquals(0, user.getSignAttempts());
    assertNull(user.getLastSignAttempt());
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertEquals(Locale.ENGLISH, user.getLocale());
  }

  @Test
  public void save_Update() {
    User user = userRepository.findById(3L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    userService.save(user, "newpassword");

    user = userRepository.findById(3L).get();
    assertEquals((Long) 3L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertEquals(UserRole.BIOLOGIST, user.getRole());
    verify(authenticationService).encode("newpassword");
    assertEquals(hashedPassword, user.getHashedPassword());
    assertEquals(2, user.getSignAttempts());
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertEquals(Locale.CHINESE, user.getLocale());
  }

  @Test
  public void save_UpdateKeepPassword() {
    User user = userRepository.findById(3L).get();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    userService.save(user, null);

    assertEquals((Long) 3L, user.getId());
    assertEquals("Test User", user.getName());
    assertEquals("test.user@ircm.qc.ca", user.getEmail());
    assertEquals(UserRole.BIOLOGIST, user.getRole());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, user.getHashedPassword());
    assertEquals(2, user.getSignAttempts());
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
        .isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(10, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
        .isBefore(user.getLastSignAttempt()));
    assertEquals(true, user.isActive());
    assertEquals(false, user.isManager());
    assertNotNull(user.getLaboratory());
    assertEquals((Long) 1L, user.getLaboratory().getId());
    assertEquals(Locale.CHINESE, user.getLocale());
  }

  @Test(expected = NullPointerException.class)
  public void save_Null() {
    userService.save(null, null);
  }
}
