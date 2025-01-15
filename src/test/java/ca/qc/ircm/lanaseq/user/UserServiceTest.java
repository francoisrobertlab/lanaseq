package ca.qc.ircm.lanaseq.user;

import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link UserService}.
 */
@ServiceTestAnnotations
public class UserServiceTest {
  private static final String READ = "read";
  private static final String WRITE = "write";
  @Autowired
  private UserService service;
  @Autowired
  private UserRepository repository;
  @MockitoBean
  private PasswordEncoder passwordEncoder;
  @MockitoBean
  private AuthenticatedUser authenticatedUser;
  @MockitoBean
  private PermissionEvaluator permissionEvaluator;
  private String hashedPassword = "4k7GCUVUzV5zL74V867q";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(passwordEncoder.encode(any())).thenReturn(hashedPassword);
    when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
  }

  @Test
  @WithMockUser
  public void get() {
    User user = service.get(1L).orElseThrow();

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
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 30, 0), user.getCreationDate());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(READ));
  }

  @Test
  @WithMockUser
  public void get_0() {
    assertFalse(service.get(0).isPresent());
  }

  @Test
  @WithMockUser
  public void getByEmail() {
    User user = service.getByEmail("francois.robert@ircm.qc.ca").orElseThrow();

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
    assertFalse(service.getByEmail("a").isPresent());
  }

  @Test
  public void exists_Email_True() throws Throwable {
    boolean exists = service.exists("christian.poitras@ircm.qc.ca");

    assertEquals(true, exists);

    verifyNoInteractions(authenticatedUser);
  }

  @Test
  public void exists_Email_False() throws Throwable {
    boolean exists = service.exists("abc@ircm.qc.ca");

    assertEquals(false, exists);

    verifyNoInteractions(authenticatedUser);
  }

  @Test
  @WithMockUser
  public void all() {
    when(authenticatedUser.getUser()).thenReturn(repository.findById(3L));

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

    assertNotEquals(0, user.getId());
    user = repository.findById(user.getId()).orElseThrow();
    assertNotNull(user);
    assertNotEquals(0, user.getId());
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

    assertNotEquals(0, user.getId());
    user = repository.findById(user.getId()).orElseThrow();
    assertNotNull(user);
    assertNotEquals(0, user.getId());
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

    assertNotEquals(0, user.getId());
    user = repository.findById(user.getId()).orElseThrow();
    assertNotNull(user);
    assertNotEquals(0, user.getId());
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

    assertNotEquals(0, user.getId());
    user = repository.findById(user.getId()).orElseThrow();
    assertNotNull(user);
    assertNotEquals(0, user.getId());
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
    User user = repository.findById(6L).orElseThrow();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    service.save(user, "newpassword");

    user = repository.findById(6L).orElseThrow();
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
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getCreationDate());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(WRITE));
    verify(authenticatedUser).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_UpdateKeepPassword() {
    User user = repository.findById(6L).orElseThrow();
    user.setName("Test User");
    user.setEmail("test.user@ircm.qc.ca");
    user.setLocale(Locale.CHINESE);

    service.save(user, null);

    user = repository.findById(6L).orElseThrow();
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
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getCreationDate());
    assertEquals(Locale.CHINESE, user.getLocale());
    verify(permissionEvaluator).hasPermission(any(), eq(user), eq(WRITE));
    verify(authenticatedUser, never()).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_UpdateFirstUserRemoveAdmin() {
    assertThrows(AccessDeniedException.class, () -> {
      User user = repository.findById(1L).orElseThrow();
      user.setAdmin(false);

      service.save(user, "newpassword");
    });
  }

  @Test
  @WithMockUser
  public void save_UpdateFirstUserRemoveActive() {
    assertThrows(AccessDeniedException.class, () -> {
      User user = repository.findById(1L).orElseThrow();
      user.setActive(false);

      service.save(user, "newpassword");
    });
  }

  @Test
  @WithMockUser
  public void save_Password() {
    User user = repository.findById(6L).orElseThrow();
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));

    service.save("newpassword");

    user = repository.findById(6L).orElseThrow();
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
    assertEquals(LocalDateTime.of(2018, 11, 21, 10, 14, 53), user.getCreationDate());
    assertNull(user.getLocale());
    verify(authenticatedUser).reloadAuthorities();
  }

  @Test
  @WithMockUser
  public void save_PasswordNoAuthorityChange() {
    User user = repository.findById(3L).orElseThrow();
    when(authenticatedUser.getUser()).thenReturn(Optional.of(user));

    service.save("newpassword");

    user = repository.findById(3L).orElseThrow();
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
    assertEquals(LocalDateTime.of(2018, 11, 20, 9, 48, 47), user.getCreationDate());
    assertNull(user.getLocale());
    verify(authenticatedUser, never()).reloadAuthorities();
  }

  @Test
  @WithAnonymousUser
  public void save_PasswordAnonymousDenied() {
    assertThrows(AccessDeniedException.class, () -> {
      User user = repository.findById(3L).orElseThrow();
      when(authenticatedUser.getUser()).thenReturn(Optional.of(user));

      service.save("new password");
    });
  }
}
