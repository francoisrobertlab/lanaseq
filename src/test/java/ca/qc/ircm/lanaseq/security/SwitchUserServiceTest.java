package ca.qc.ircm.lanaseq.security;

import static ca.qc.ircm.lanaseq.security.SwitchUserService.ROLE_PREVIOUS_ADMINISTRATOR;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;

/**
 * Tests for {@link SwitchUserService}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class SwitchUserServiceTest extends SpringUIUnitTest {
  @Autowired
  private SwitchUserService service;
  @Autowired
  private UserDetailsService userDetailsService;
  @Autowired
  private UserRepository repository;
  @MockBean
  private ApplicationEventPublisher eventPublisher;
  @MockBean
  private UserDetailsChecker userDetailsChecker;

  @BeforeEach
  public void beforeEach() {
    service.setUserDetailsChecker(userDetailsChecker);
  }

  private org.springframework.security.core.userdetails.User userDetails(User user,
      String... roles) {
    Collection<GrantedAuthority> authorities =
        Stream.of(roles).map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
    org.springframework.security.core.userdetails.User userDetails =
        new org.springframework.security.core.userdetails.User(user.getEmail(),
            user.getHashedPassword(), authorities);
    return userDetails;
  }

  @Test
  public void switchUser() {
    User user = repository.findById(3L).get();
    service.switchUser(user, VaadinServletRequest.getCurrent());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertTrue(authentication.getPrincipal() instanceof UserDetailsWithId);
    UserDetailsWithId userDetails = (UserDetailsWithId) authentication.getPrincipal();
    assertEquals(3L, userDetails.getId());
    assertEquals("jonh.smith@ircm.qc.ca", userDetails.getUsername());
    assertEquals(user.getHashedPassword(), userDetails.getPassword());
    assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority(USER)));
    Optional<SwitchUserGrantedAuthority> optionalSwitchUserGrantedAuthority =
        authentication.getAuthorities().stream()
            .filter(authority -> authority instanceof SwitchUserGrantedAuthority)
            .map(authority -> (SwitchUserGrantedAuthority) authority).findFirst();
    assertTrue(optionalSwitchUserGrantedAuthority.isPresent());
    SwitchUserGrantedAuthority switchUserGrantedAuthority =
        optionalSwitchUserGrantedAuthority.get();
    assertEquals(ROLE_PREVIOUS_ADMINISTRATOR, switchUserGrantedAuthority.getAuthority());
    assertNotNull(switchUserGrantedAuthority.getSource());
    Authentication previousAuthentication = switchUserGrantedAuthority.getSource();
    assertTrue(previousAuthentication.getPrincipal() instanceof UserDetailsWithId);
    UserDetailsWithId previousUserDetails =
        (UserDetailsWithId) previousAuthentication.getPrincipal();
    assertEquals(1L, previousUserDetails.getId());
    assertEquals("lanaseq@ircm.qc.ca", previousUserDetails.getUsername());
    assertTrue(previousAuthentication.getAuthorities().contains(new SimpleGrantedAuthority(ADMIN)));
    assertNotNull(VaadinServletRequest.getCurrent().getWrappedSession(true)
        .getAttribute(SPRING_SECURITY_CONTEXT_KEY));
    SecurityContext sessionContext = (SecurityContext) VaadinServletRequest.getCurrent()
        .getWrappedSession(true).getAttribute(SPRING_SECURITY_CONTEXT_KEY);
    assertSame(authentication, sessionContext.getAuthentication());
  }

  @Test
  @WithAnonymousUser
  public void switchUser_AccessDenied_Anonymous() {
    User user = repository.findById(3L).get();
    assertThrows(AccessDeniedException.class, () -> {
      service.switchUser(user, VaadinServletRequest.getCurrent());
    });
  }

  @Test
  @WithMockUser(authorities = { USER, UserRole.MANAGER })
  public void switchUser_AccessDenied() {
    User user = repository.findById(3L).get();
    assertThrows(AccessDeniedException.class, () -> {
      service.switchUser(user, VaadinServletRequest.getCurrent());
    });
  }

  @Test
  public void switchUser_NullSwitchTo() {
    assertThrows(NullPointerException.class, () -> {
      service.switchUser(null, VaadinServletRequest.getCurrent());
    });
  }

  @Test
  public void switchUser_NullRequest() {
    User user = repository.findById(3L).get();
    assertThrows(NullPointerException.class, () -> {
      service.switchUser(user, null);
    });
  }

  @Test
  public void exitSwitchUser() {
    User user = repository.findById(3L).get();
    service.switchUser(user, VaadinServletRequest.getCurrent());
    service.exitSwitchUser(VaadinServletRequest.getCurrent());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertTrue(authentication.getPrincipal() instanceof UserDetailsWithId);
    UserDetailsWithId userDetails = (UserDetailsWithId) authentication.getPrincipal();
    assertEquals(1L, userDetails.getId());
    assertEquals("lanaseq@ircm.qc.ca", userDetails.getUsername());
    assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority(ADMIN)));
    assertNotNull(VaadinServletRequest.getCurrent().getWrappedSession(true)
        .getAttribute(SPRING_SECURITY_CONTEXT_KEY));
    SecurityContext sessionContext = (SecurityContext) VaadinServletRequest.getCurrent()
        .getWrappedSession(true).getAttribute(SPRING_SECURITY_CONTEXT_KEY);
    assertSame(authentication, sessionContext.getAuthentication());
  }

  @Test
  public void exitSwitchUser_NotSwitched() {
    assertThrows(AuthenticationCredentialsNotFoundException.class,
        () -> service.exitSwitchUser(VaadinServletRequest.getCurrent()));
  }
}
