package ca.qc.ircm.lanaseq.test.config;

import com.vaadin.testbench.TestBenchTestCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Request filter that sets security context.
 */
public class TestBenchSecurityFilter extends GenericFilterBean
    implements TestExecutionListener, Ordered {
  public static final String BEAN_NAME = "TestBenchSecurityFilter";
  private static final Logger logger = LoggerFactory.getLogger(TestBenchSecurityFilter.class);
  private static boolean copyAuthenticationOnFilter;
  private static Authentication authentication;
  private SecurityContextRepository repo;

  public TestBenchSecurityFilter() {
    this(new HttpSessionSecurityContextRepository());
  }

  public TestBenchSecurityFilter(SecurityContextRepository repo) {
    this.repo = repo;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    if (copyAuthenticationOnFilter) {
      if (authentication != null) {
        logger.debug("set authentication {} in security context", authentication);
        copyAuthenticationOnFilter = false;
        SecurityContext securityContext = repo.loadDeferredContext(request).get();
        securityContext.setAuthentication(authentication);
        repo.saveContext(securityContext, request, response);
        copyAuthenticationOnFilter = false;
      } else {
        logger.warn("authentication is null in test bench test");
      }
    }
    filterChain.doFilter(request, response);
  }

  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    testContext.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    copyAuthenticationOnFilter = isTestBenchTest(testContext);
    authentication = SecurityContextHolder.getContext().getAuthentication();
    logger.trace("saving authentication {}", authentication);
  }

  private boolean isTestBenchTest(TestContext testContext) {
    return TestBenchTestCase.class.isAssignableFrom(testContext.getTestClass());
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
