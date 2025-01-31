package ca.qc.ircm.lanaseq.logging.web;

import static ca.qc.ircm.lanaseq.logging.web.MdcFilter.USER_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.MDC;

/**
 * Tests for {@link MdcFilter}.
 */
@NonTransactionalTestAnnotations
public class MdcFilterTest {

  private MdcFilter mdcFilter;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpSession session;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;

  @BeforeEach
  public void beforeTest() {
    mdcFilter = new MdcFilter(authenticatedUser);
  }

  @Test
  public void doFilter_Anonymous() throws Throwable {
    String sessionId = "sessionId";
    when(request.getSession()).thenReturn(session);
    when(session.getId()).thenReturn(sessionId);
    doAnswer(i -> {
      assertNull(MDC.get(USER_CONTEXT_KEY));
      return null;
    }).when(filterChain).doFilter(any(), any());

    mdcFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(MDC.get(USER_CONTEXT_KEY));
  }

  @Test
  public void doFilter_User() throws Throwable {
    long userId = 3L;
    String email = "test@ircm.qc.ca";
    when(authenticatedUser.getUser()).thenReturn(Optional.of(new User(userId, email)));
    doAnswer(i -> {
      assertEquals("3:test", MDC.get(USER_CONTEXT_KEY));
      return null;
    }).when(filterChain).doFilter(any(), any());

    mdcFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(MDC.get(USER_CONTEXT_KEY));
  }

  @Test
  public void doFilter_EmptyUser() throws Throwable {
    when(authenticatedUser.getUser()).thenReturn(Optional.empty());
    doAnswer(i -> {
      assertNull(MDC.get(USER_CONTEXT_KEY));
      return null;
    }).when(filterChain).doFilter(any(), any());

    mdcFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(MDC.get(USER_CONTEXT_KEY));
  }
}
