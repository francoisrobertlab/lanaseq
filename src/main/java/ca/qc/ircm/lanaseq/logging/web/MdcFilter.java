package ca.qc.ircm.lanaseq.logging.web;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Request filter that set MDC context for loggers.
 */
public class MdcFilter extends GenericFilterBean {
  public static final String BEAN_NAME = "MdcFilter";
  public static final String USER_CONTEXT_KEY = "user";
  private final AuthenticatedUser authenticatedUser;

  public MdcFilter(AuthenticatedUser authenticatedUser) {
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
