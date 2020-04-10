/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

import ca.qc.ircm.lanaseq.security.AuthorizationService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Request filter that set MDC context for loggers.
 */
public class MdcFilter extends GenericFilterBean {
  public static final String BEAN_NAME = "MdcFilter";
  public static final String USER_CONTEXT_KEY = "user";
  @Autowired
  private AuthorizationService authorizationService;

  public MdcFilter() {
  }

  protected MdcFilter(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    try {
      this.setNdc(httpRequest);
      filterChain.doFilter(request, response);
    } finally {
      this.removeNdc();
    }
  }

  private void setNdc(HttpServletRequest request) {
    if (authorizationService.currentUser() != null) {
      MDC.put(USER_CONTEXT_KEY, authorizationService.currentUser().getId() + ":"
          + authorizationService.currentUser().getEmail().replaceFirst("@.*", ""));
    }
  }

  private void removeNdc() {
    MDC.clear();
  }
}
