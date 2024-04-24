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

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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
  private AuthenticatedUser authenticatedUser;

  public MdcFilter() {
  }

  protected MdcFilter(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
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
    authenticatedUser.getUser().ifPresent(user -> MDC.put(USER_CONTEXT_KEY,
        user.getId() + ":" + user.getEmail().replaceFirst("@.*", "")));
  }

  private void removeNdc() {
    MDC.clear();
  }
}
