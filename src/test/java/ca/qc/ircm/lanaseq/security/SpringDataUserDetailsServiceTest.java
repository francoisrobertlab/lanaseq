package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.UserAuthority.FORCE_CHANGE_PASSWORD;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.InitializeDatabaseExecutionListener;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Tests for {@link SpringDataUserDetailsService}.
 */
@NonTransactionalTestAnnotations
public class SpringDataUserDetailsServiceTest {

  private SpringDataUserDetailsService userDetailsService;
  @Mock
  private UserRepository userRepository;
  private User user;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    userDetailsService = new SpringDataUserDetailsService(userRepository);
    user = new User();
    user.setId(2L);
    user.setEmail("lanaseq@ircm.qc.ca");
    user.setName("A User");
    user.setHashedPassword(InitializeDatabaseExecutionListener.PASSWORD_PASS1);
    user.setActive(true);
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
  }

  private Optional<? extends GrantedAuthority> findAuthority(
      Collection<? extends GrantedAuthority> authorities, String authority) {
    return authorities.stream().filter(autho -> autho.getAuthority().equals(authority)).findFirst();
  }

  @Test
  public void loadUserByUsername() {
    UserDetails userDetails = userDetailsService.loadUserByUsername("lanaseq@ircm.qc.ca");

    verify(userRepository).findByEmail("lanaseq@ircm.qc.ca");
    assertEquals("lanaseq@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(1, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertInstanceOf(SimpleGrantedAuthority.class, authority);
    }
    assertTrue(findAuthority(authorities, USER).isPresent());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
    assertInstanceOf(UserDetailsWithId.class, userDetails);
    UserDetailsWithId userDetailsWithId = (UserDetailsWithId) userDetails;
    assertEquals((Long) 2L, userDetailsWithId.getId());
  }

  @Test
  public void loadUserByUsername_Admin() {
    user.setAdmin(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lanaseq@ircm.qc.ca");

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(2, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertInstanceOf(SimpleGrantedAuthority.class, authority);
    }
    assertTrue(findAuthority(authorities, USER).isPresent());
    assertTrue(findAuthority(authorities, ADMIN).isPresent());
  }

  @Test
  public void loadUserByUsername_Manager() {
    user.setManager(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lanaseq@ircm.qc.ca");

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(2, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertInstanceOf(SimpleGrantedAuthority.class, authority);
    }
    assertTrue(findAuthority(authorities, USER).isPresent());
    assertTrue(findAuthority(authorities, MANAGER).isPresent());
  }

  @Test
  public void loadUserByUsername_AdminManager() {
    user.setAdmin(true);
    user.setManager(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lanaseq@ircm.qc.ca");

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals(3, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertInstanceOf(SimpleGrantedAuthority.class, authority);
    }
    assertTrue(findAuthority(authorities, USER).isPresent());
    assertTrue(findAuthority(authorities, ADMIN).isPresent());
    assertTrue(findAuthority(authorities, MANAGER).isPresent());
  }

  @Test
  public void loadUserByUsername_NotExists() {
    assertThrows(UsernameNotFoundException.class, () -> {
      when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

      userDetailsService.loadUserByUsername("lanaseq@ircm.qc.ca");
    });
  }

  @Test
  public void loadUserByUsername_ExpiredPassword() {
    user.setExpiredPassword(true);

    UserDetails userDetails = userDetailsService.loadUserByUsername("lanaseq@ircm.qc.ca");

    assertEquals("lanaseq@ircm.qc.ca", userDetails.getUsername());
    assertEquals(InitializeDatabaseExecutionListener.PASSWORD_PASS1, userDetails.getPassword());
    List<? extends GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
    assertEquals(2, authorities.size());
    for (GrantedAuthority authority : authorities) {
      assertInstanceOf(SimpleGrantedAuthority.class, authority);
    }
    assertTrue(findAuthority(authorities, USER).isPresent());
    assertTrue(findAuthority(authorities, FORCE_CHANGE_PASSWORD).isPresent());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
  }
}
