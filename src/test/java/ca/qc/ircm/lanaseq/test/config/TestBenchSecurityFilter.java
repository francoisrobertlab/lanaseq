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

package ca.qc.ircm.lanaseq.test.config;

import com.vaadin.testbench.TestBenchTestCase;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    if (copyAuthenticationOnFilter && authentication != null) {
      logger.debug("set authentication {} in security context", authentication);
      SecurityContext securityContext = repo.loadContext(request).get();
      securityContext.setAuthentication(authentication);
      repo.saveContext(securityContext, request, response);
      copyAuthenticationOnFilter = false;
      filterChain.doFilter(request, response);
    } else {
      if (copyAuthenticationOnFilter && authentication == null) {
        logger.warn("authentication is null in test bench test");
      }
      filterChain.doFilter(request, response);
    }
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
