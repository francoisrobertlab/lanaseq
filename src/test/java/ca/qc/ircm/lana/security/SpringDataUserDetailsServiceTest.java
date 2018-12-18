package ca.qc.ircm.lana.security;

import static ca.qc.ircm.lana.security.SecurityConfiguration.FORCE_CHANGE_PASSWORD_ROLE;
import static ca.qc.ircm.lana.security.SecurityConfiguration.ROLE_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserRole;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class SpringDataUserDetailsServiceTest {
  private SpringDataUserDetailsService userDetailsService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private SecurityConfiguration securityConfiguration;
  private User user;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    userDetailsService = new SpringDataUserDetailsService(userRepository, securityConfiguration);
    user = new User();
    user.setId(2L);
    user.setEmail("lana@ircm.qc.ca");
    user.setName("A User");
    user.setHashedPassword(InitializeDatabaseExecutionListener.PASSWORD_PASS1);
    user.setActive(true);
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
    when(securityConfiguration.getLockAttemps()).thenReturn(5);
    when(securityConfiguration.getLockDuration()).thenReturn(Duration.ofMinutes(3));
  }

  private Optional<? extends GrantedAuthority>
      findAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
    return authorities.stream().filter(autho -> autho.getAuthority().equals(authority)).findFirst();
  }

  @Test
  public void loadUserByUsername() {
    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    assertEquals("lana@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(1, authorities.size());
    GrantedAuthority authority = authorities.iterator().next();
    assertTrue(authority instanceof SimpleGrantedAuthority);
    assertEquals(ROLE_PREFIX + UserRole.USER, authority.getAuthority());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
  }

  @Test
  public void loadUserByUsername_Admin() {
    user.setAdmin(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(2, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertTrue(authority instanceof SimpleGrantedAuthority);
    }
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.USER).isPresent());
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.ADMIN).isPresent());
  }

  @Test
  public void loadUserByUsername_Manager() {
    user.setManager(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(2, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertTrue(authority instanceof SimpleGrantedAuthority);
    }
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.USER).isPresent());
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.MANAGER).isPresent());
  }

  @Test
  public void loadUserByUsername_AdminManager() {
    user.setAdmin(true);
    user.setManager(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(3, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertTrue(authority instanceof SimpleGrantedAuthority);
    }
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.USER).isPresent());
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.ADMIN).isPresent());
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.MANAGER).isPresent());
  }

  @Test(expected = UsernameNotFoundException.class)
  public void loadUserByUsername_NotExists() {
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

    userDetailsService.loadUserByUsername("lana@ircm.qc.ca");
  }

  @Test
  public void loadUserByUsername_Inactive() {
    user.setActive(false);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    assertEquals("lana@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(1, authorities.size());
    GrantedAuthority authority = authorities.iterator().next();
    assertTrue(authority instanceof SimpleGrantedAuthority);
    assertEquals(ROLE_PREFIX + UserRole.USER, authority.getAuthority());
    assertFalse(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
  }

  @Test
  public void loadUserByUsername_ExpiredPassword() {
    user.setExpiredPassword(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    assertEquals("lana@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    List<? extends GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
    assertEquals(2, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertTrue(authority instanceof SimpleGrantedAuthority);
    }
    assertTrue(findAuthority(authorities, ROLE_PREFIX + UserRole.USER).isPresent());
    assertTrue(findAuthority(authorities, FORCE_CHANGE_PASSWORD_ROLE).isPresent());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
  }

  @Test
  public void loadUserByUsername_NotLockedAttemps() {
    user.setSignAttempts(1);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.SECONDS));

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    assertEquals("lana@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(1, authorities.size());
    GrantedAuthority authority = authorities.iterator().next();
    assertTrue(authority instanceof SimpleGrantedAuthority);
    assertEquals(ROLE_PREFIX + UserRole.USER, authority.getAuthority());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
  }

  @Test
  public void loadUserByUsername_NotLockedLastSignAttemp() {
    user.setSignAttempts(5);
    user.setLastSignAttempt(Instant.now().minus(6, ChronoUnit.MINUTES));

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    assertEquals("lana@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(1, authorities.size());
    GrantedAuthority authority = authorities.iterator().next();
    assertTrue(authority instanceof SimpleGrantedAuthority);
    assertEquals(ROLE_PREFIX + UserRole.USER, authority.getAuthority());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
  }

  @Test
  public void loadUserByUsername_Locked() {
    user.setSignAttempts(5);
    user.setLastSignAttempt(Instant.now().minus(20, ChronoUnit.SECONDS));

    UserDetails userDetails = userDetailsService.loadUserByUsername("lana@ircm.qc.ca");

    assertEquals("lana@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(1, authorities.size());
    GrantedAuthority authority = authorities.iterator().next();
    assertTrue(authority instanceof SimpleGrantedAuthority);
    assertEquals(ROLE_PREFIX + UserRole.USER, authority.getAuthority());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertFalse(userDetails.isAccountNonLocked());
  }
}
