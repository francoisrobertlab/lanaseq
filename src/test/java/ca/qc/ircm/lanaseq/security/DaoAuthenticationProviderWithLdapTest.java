package ca.qc.ircm.lanaseq.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Tests for {@link DaoAuthenticationProviderWithLdap}.
 */
@ServiceTestAnnotations
public class DaoAuthenticationProviderWithLdapTest {
  @Autowired
  private DaoAuthenticationProviderWithLdap ldapDaoAuthenticationProvider;
  @Mock
  private LdapService ldapService;
  @Mock
  private LdapConfiguration ldapConfiguration;
  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  public void beforeTest() {
    ldapDaoAuthenticationProvider.setLdapService(ldapService);
    ldapDaoAuthenticationProvider.setLdapConfiguration(ldapConfiguration);
  }

  @Test
  public void authenticate_NoLdap() throws Throwable {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    verifyNoInteractions(ldapService);
    User user = userRepository.findById(3L).orElseThrow();
    assertEquals(0, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test()
  public void authenticate_NoLdapFail() throws Throwable {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass");

    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(BadCredentialsException.class.getSimpleName() + " expected");
    } catch (BadCredentialsException e) {
      // Success.
    }

    verify(ldapService, never()).getUsername(any());
    verify(ldapService, never()).isPasswordValid(any(), any());
    User user = userRepository.findById(3L).orElseThrow();
    assertEquals(3, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_LdapSuccess() throws Throwable {
    when(ldapConfiguration.enabled()).thenReturn(true);
    when(ldapService.getUsername(any())).thenReturn(Optional.of("frobert"));
    when(ldapService.isPasswordValid(any(), any())).thenReturn(true);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "test");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    verify(ldapService).getUsername("jonh.smith@ircm.qc.ca");
    verify(ldapService).isPasswordValid("frobert", "test");
    User user = userRepository.findById(3L).orElseThrow();
    assertEquals(0, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_LdapFail() throws Throwable {
    when(ldapConfiguration.enabled()).thenReturn(true);
    when(ldapService.getUsername(any())).thenReturn(Optional.of("frobert"));

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "test");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(BadCredentialsException.class.getSimpleName() + " expected");
    } catch (BadCredentialsException e) {
      // Success.
    }

    verify(ldapService).getUsername("jonh.smith@ircm.qc.ca");
    verify(ldapService).isPasswordValid("frobert", "test");
    User user = userRepository.findById(3L).orElseThrow();
    assertEquals(3, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_LdapFailPasswordEncoderSuccess() throws Throwable {
    when(ldapConfiguration.enabled()).thenReturn(true);
    when(ldapService.getUsername(any())).thenReturn(Optional.of("frobert"));

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    User user = userRepository.findById(3L).orElseThrow();
    assertEquals(0, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_NotAnLdapUser() throws Throwable {
    when(ldapConfiguration.enabled()).thenReturn(true);
    when(ldapService.getUsername(any())).thenReturn(Optional.empty());

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "test");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(BadCredentialsException.class.getSimpleName() + " expected");
    } catch (BadCredentialsException e) {
      // Success.
    }

    verify(ldapService).getUsername("jonh.smith@ircm.qc.ca");
    verify(ldapService, never()).isPasswordValid(any(), any());
    User user = userRepository.findById(3L).orElseThrow();
    assertEquals(3, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_Inactive() throws Throwable {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("inactive.user@ircm.qc.ca", "pass1");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(DisabledException.class.getSimpleName() + " expected");
    } catch (DisabledException e) {
      // Success.
    }

    User user = userRepository.findById(6L).orElseThrow();
    assertEquals(3, user.getSignAttempts());
    assertTrue(
        LocalDateTime.now().minus(19, ChronoUnit.MINUTES).isAfter(user.getLastSignAttempt()));
    assertTrue(
        LocalDateTime.now().minus(30, ChronoUnit.MINUTES).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void authenticate_Disable() throws Throwable {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass");
    User user = userRepository.findById(3L).orElseThrow();
    user.setSignAttempts(19);
    userRepository.save(user);

    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(BadCredentialsException.class.getSimpleName() + " expected");
    } catch (BadCredentialsException e) {
      // Success.
    }

    user = userRepository.findById(3L).orElseThrow();
    assertEquals(20, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
    assertFalse(user.isActive());
  }

  @Test
  public void loadUserByUsername_NotLockedSignAttemp() {
    User user = userRepository.findById(3L).orElseThrow();
    user.setSignAttempts(0);
    user.setLastSignAttempt(LocalDateTime.now());
    userRepository.save(user);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    user = userRepository.findById(3L).orElseThrow();
    assertEquals(0, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void loadUserByUsername_NotLockedLastSignAttemp() {
    User user = userRepository.findById(3L).orElseThrow();
    user.setSignAttempts(5);
    user.setLastSignAttempt(LocalDateTime.now().minus(6, ChronoUnit.MINUTES));
    userRepository.save(user);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    ldapDaoAuthenticationProvider.authenticate(authentication);

    user = userRepository.findById(3L).orElseThrow();
    assertEquals(0, user.getSignAttempts());
    assertTrue(LocalDateTime.now().plusSeconds(1).isAfter(user.getLastSignAttempt()));
    assertTrue(LocalDateTime.now().minusSeconds(10).isBefore(user.getLastSignAttempt()));
  }

  @Test
  public void loadUserByUsername_Locked() {
    User user = userRepository.findById(3L).orElseThrow();
    user.setSignAttempts(5);
    user.setLastSignAttempt(LocalDateTime.now().minus(1, ChronoUnit.MINUTES));
    userRepository.save(user);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken("jonh.smith@ircm.qc.ca", "pass1");
    try {
      ldapDaoAuthenticationProvider.authenticate(authentication);
      fail(LockedException.class.getSimpleName() + " expected");
    } catch (LockedException e) {
      // Success.
    }

    user = userRepository.findById(3L).orElseThrow();
    assertEquals(5, user.getSignAttempts());
    assertTrue(LocalDateTime.now().minus(1, ChronoUnit.MINUTES).isAfter(user.getLastSignAttempt()));
    assertTrue(
        LocalDateTime.now().minus(2, ChronoUnit.MINUTES).isBefore(user.getLastSignAttempt()));
  }
}
