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
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
    Long userId = 3L;
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
