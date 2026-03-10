package ca.qc.ircm.lanaseq.security.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationContext;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link RememberMeFixAnnotatedViewAccessChecker}.
 * <p>
 * Additional annotation test are done by other UI unit tests.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class RememberMeFixAnnotatedViewAccessCheckerTest {

  private RememberMeFixAnnotatedViewAccessChecker checker;
  @Mock
  private NavigationContext context;

  @BeforeEach
  public void before() {
    checker = new RememberMeFixAnnotatedViewAccessChecker();
  }

  @Test
  public void check_null_principal() {
    when(context.getPrincipal()).thenReturn(null);
    Authentication authentication = Objects.requireNonNull(
        SecurityContextHolder.getContext().getAuthentication());
    RememberMeAuthenticationToken token = new RememberMeAuthenticationToken(
        authentication.getName(), Objects.requireNonNull(authentication.getPrincipal()),
        authentication.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(token);
    AccessCheckResult result = checker.check(context);
    assertEquals(AccessCheckResult.neutral(), result);
  }
}
