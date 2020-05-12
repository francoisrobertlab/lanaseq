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

package ca.qc.ircm.lanaseq.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
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
  public void hasPermission_ReadUser_Anonymous() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_ReadUser_Self() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_ReadUser_NotSelf() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_ReadUser_Manager() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_ReadUser_Admin() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_READ));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, READ));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_READ));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteNewUser_Anonymous() throws Throwable {
    User user = new User("new user");
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewUser_User() throws Throwable {
    User user = new User("new user");
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteNewUser_Manager() throws Throwable {
    User user = new User("new user");
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteNewAdmin_User() throws Throwable {
    User user = new User("new user");
    user.setAdmin(true);
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteNewAdmin_Manager() throws Throwable {
    User user = new User("new user");
    user.setAdmin(true);
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_WriteNewUser_Admin() throws Throwable {
    User user = new User("new user");
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_WriteNewAdmin_Admin() throws Throwable {
    User user = new User("new user");
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_WriteUser_Anonymous() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("jonh.smith@ircm.qc.ca")
  public void hasPermission_WriteUser_Self() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("olivia.brown@ircm.qc.ca")
  public void hasPermission_WriteUser_NotSelf() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void hasPermission_WriteUser_Manager() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_WriteUser_Admin() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, WRITE));
    assertTrue(permissionEvaluator.hasPermission(authentication(), user, BASE_WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, WRITE));
    assertTrue(
        permissionEvaluator.hasPermission(authentication(), user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  public void hasPermission_NullAuthentication() throws Throwable {
    User user = userRepository.findById(3L).orElse(null);
    assertFalse(permissionEvaluator.hasPermission(null, user, READ));
    assertFalse(permissionEvaluator.hasPermission(null, user, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, user, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(null, user, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, user.getId(), USER_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(null, user.getId(), USER_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(null, user.getId(), USER_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(null, user.getId(), USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithAnonymousUser
  public void hasPermission_Null_Anonymous() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_Null() throws Throwable {
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, BASE_WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), null, USER_CLASS, BASE_WRITE));
  }

  @Test
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_NotUser() throws Throwable {
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
  @WithUserDetails("lana@ircm.qc.ca")
  public void hasPermission_NotLongId() throws Throwable {
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "lana@ircm.qc.ca", USER_CLASS, READ));
    assertFalse(
        permissionEvaluator.hasPermission(authentication(), "lana@ircm.qc.ca", USER_CLASS, WRITE));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "lana@ircm.qc.ca", USER_CLASS,
        BASE_READ));
    assertFalse(permissionEvaluator.hasPermission(authentication(), "lana@ircm.qc.ca", USER_CLASS,
        BASE_WRITE));
  }
}
