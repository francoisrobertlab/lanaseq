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

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.SpringVaadinServletService;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Configures Karibu-Testing.
 */
public class KaribuTestExecutionListener implements TestExecutionListener {
  private Routes routes;

  private boolean isKaribuTest(TestContext testContext) {
    return AbstractKaribuTestCase.class.isAssignableFrom(testContext.getTestClass());
  }

  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    if (!isKaribuTest(testContext)) {
      return;
    }
    routes = new Routes().autoDiscoverViews("ca.qc.ircm.lanaseq");
  }

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    if (!isKaribuTest(testContext)) {
      return;
    }
    AnnotationFinder
        .findAnnotation(testContext.getTestClass(), testContext.getTestMethod(), UserAgent.class)
        .ifPresent(ua -> MockVaadin.INSTANCE.setUserAgent(ua.value()));
    MockVaadin.setup(UI::new, new MockSpringServlet(testContext.getApplicationContext(), routes));
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    if (!isKaribuTest(testContext)) {
      return;
    }
    MockVaadin.tearDown();
  }

  static class MockSpringServlet extends SpringServlet {
    private static final long serialVersionUID = 7111284468498092255L;
    private ApplicationContext context;
    private Routes routes;

    public MockSpringServlet(ApplicationContext context, Routes routes) {
      super(context, false);
      this.context = context;
      this.routes = routes;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
      routes.register(servletConfig.getServletContext());
      super.init(servletConfig);
    }

    @Override
    protected VaadinServletService createServletService(
        DeploymentConfiguration deploymentConfiguration) throws ServiceException {
      final VaadinServletService service =
          new SpringVaadinServletService(this, deploymentConfiguration, context) {
            private static final long serialVersionUID = -406029953088130559L;

            @Override
            protected boolean isAtmosphereAvailable() {
              return false;
            }

            @Override
            public String getMainDivId(VaadinSession session, VaadinRequest request) {
              return "ROOT-1";
            }
          };
      service.init();
      return service;
    }
  }
}
