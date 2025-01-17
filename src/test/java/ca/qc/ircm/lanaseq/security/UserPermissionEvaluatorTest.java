package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link UserPermissionEvaluator}.
 */
@ServiceTestAnnotations
public class UserPermissionEvaluatorTest {
  private static final String USER_CLASS = User.class.getName();
  private static final String READ = "read";
  private static final Permission BASE_READ = Permission.READ;
  private static final String WRITE = "write";
  private static final Permission BASE_WRITE = Permission.WRITE;
  @Autowired
  private UserPermissionEvaluator permissionEvaluator;
  @Autowired
  private UserRepository userRepository;

  private Authentication authentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_ReadUser_Anonymous() {
    User user = userRepository.findById(3L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadUser_Self() {
    User user = userRepository.findById(3L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadUser_NotSelf() {
    User user = userRepository.findById(3L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadUser_Manager() {
    User user = userRepository.findById(3L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_ReadUser_Admin() {
    User user = userRepository.findById(3L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewUser_Anonymous() {
    User user = new User("new user");
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewUser_User() {
    User user = new User("new user");
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteNewUser_Manager() {
    User user = new User("new user");
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewAdmin_User() {
    User user = new User("new user");
    user.setAdmin(true);
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteNewAdmin_Manager() {
    User user = new User("new user");
    user.setAdmin(true);
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteNewUser_Admin() {
    User user = new User("new user");
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteNewAdmin_Admin() {
    User user = new User("new user");
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteUser_Anonymous() {
    User user = userRepository.findById(3L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteUser_Self() {
    User user = userRepository.findById(3L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteUser_NotSelf() {
    User user = userRepository.findById(3L).orElseThrow();
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteUser_Manager() {
    User user = userRepository.findById(3L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_WriteUser_Admin() {
    User user = userRepository.findById(3L).orElseThrow();
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_NotUser() {
    assertFalse(permissionEvaluator.hasPermission(authentication(), new Protocol(1L), READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), new Protocol(1L), WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), new Protocol(1L), BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), new Protocol(1L), BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, Protocol.class.getName(), READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), 1L, Protocol.class.getName(), WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), 1L, Protocol.class.getName(),
        BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), 1L, Protocol.class.getName(),
        BASE_WRITE));
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void hasPermission_NotLongId() {
    assertFalse(permissionEvaluator.hasPermission(authentication(), "lanaseq@ircm.qc.ca",
        USER_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "lanaseq@ircm.qc.ca",
        USER_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "lanaseq@ircm.qc.ca",
        USER_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "lanaseq@ircm.qc.ca",
        USER_CLASS, BASE_WRITE));
  }
}
