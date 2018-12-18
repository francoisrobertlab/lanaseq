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

package ca.qc.ircm.lana.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class LdapDaoAuthenticationProviderTest {
  @Inject
  private LdapDaoAuthenticationProvider ldapDaoAuthenticationProvider;
  @Mock
  private LdapService ldapService;
  @Mock
  private LdapConfiguration ldapConfiguration;
  @Inject
  private UserRepository userRepository;

  @Before
  public void beforeTest() {
    ldapDaoAuthenticationProvider.setLdapService(ldapService);
    ldapDaoAuthenticationProvider.setLdapConfiguration(ldapConfiguration);
  }

  @Test
  public void authenticate_NoLdap() throws Throwable {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    verifyZeroInteractions(ldapService);
    User user = userRepository.findById(3L).get();
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_LdapSuccess() throws Throwable {
    when(ldapConfiguration.isEnabled()).thenReturn(true);
    when(ldapService.getUsername(any())).thenReturn("frobert");
    when(ldapService.isPasswordValid(any(), any())).thenReturn(true);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "test");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    verify(ldapService).getUsername("jonh.smith@ircm.qc.ca");
    verify(ldapService).isPasswordValid("frobert", "test");
    User user = userRepository.findById(3L).get();
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_LdapFail() throws Throwable {
    when(ldapConfiguration.isEnabled()).thenReturn(true);
    when(ldapService.getUsername(any())).thenReturn("frobert");

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "test");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(BadCredentialsException.class.getName() + " expected");
    } catch (BadCredentialsException e) {
      // Success.
    }

    verify(ldapService).getUsername("jonh.smith@ircm.qc.ca");
    verify(ldapService).isPasswordValid("frobert", "test");
    User user = userRepository.findById(3L).get();
    assertEquals(3, user.getSignAttempts());
    assertTrue(Instant.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_LdapFailPasswordEncoderSuccess() throws Throwable {
    when(ldapConfiguration.isEnabled()).thenReturn(true);
    when(ldapService.getUsername(any())).thenReturn("frobert");

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    User user = userRepository.findById(3L).get();
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_NotAnLdapUser() throws Throwable {
    when(ldapConfiguration.isEnabled()).thenReturn(true);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "test");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(BadCredentialsException.class.getName() + " expected");
    } catch (BadCredentialsException e) {
      // Success.
    }

    verify(ldapService).getUsername("jonh.smith@ircm.qc.ca");
    verify(ldapService, never()).isPasswordValid(any(), any());
    User user = userRepository.findById(3L).get();
    assertEquals(3, user.getSignAttempts());
    assertTrue(Instant.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_Inactive() throws Throwable {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("inactive.user@ircm.qc.ca", "pass1");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(DisabledException.class.getName() + " expected");
    } catch (DisabledException e) {
      // Success.
    }

    User user = userRepository.findById(6L).get();
    assertEquals(3, user.getSignAttempts());
    assertTrue(Instant.now().minus(19, ChronoUnit.MINUTES).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(30, ChronoUnit.MINUTES).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void loadUserByUsername_NotLockedLastSignAttemp() {
    User user = userRepository.findById(3L).get();
    user.setSignAttempts(5);
    user.setLastSignAttempt(Instant.now().minus(6, ChronoUnit.MINUTES));
    userRepository.save(user);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    user = userRepository.findById(3L).get();
    assertEquals(0, user.getSignAttempts());
    assertTrue(Instant.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void loadUserByUsername_Locked() {
    User user = userRepository.findById(3L).get();
    user.setSignAttempts(5);
    user.setLastSignAttempt(Instant.now().minus(1, ChronoUnit.MINUTES));
    userRepository.save(user);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(LockedException.class.getName() + " expected");
    } catch (LockedException e) {
      // Success.
    }

    user = userRepository.findById(3L).get();
    assertEquals(5, user.getSignAttempts());
    assertTrue(Instant.now().minus(1, ChronoUnit.MINUTES).isAfter(user.getLastSignAttempt()));
    assertTrue(Instant.now().minus(2, ChronoUnit.MINUTES).isBefore(user.getLastSignAttempt()));
  }
}
