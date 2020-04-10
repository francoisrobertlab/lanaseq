/*
 * Copyright (c) 2010 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.lana.test.config;

import static ca.qc.ircm.lana.test.config.VaadinServiceTestComponent.request;
import static ca.qc.ircm.lana.test.config.VaadinServiceTestComponent.response;
import static ca.qc.ircm.lana.test.config.VaadinServiceTestComponent.service;
import static ca.qc.ircm.lana.test.config.VaadinServiceTestComponent.session;

import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Saves VaadinService in current thread.
 */
public class VaadinServiceTestExecutionListener
    implements TestExecutionListener, InjectDependencies {
  private static final Logger logger =
      LoggerFactory.getLogger(VaadinServiceTestExecutionListener.class);
  @Value("http://localhost:${local.server.port}")
  protected String baseUrl;

  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    injectDependencies(testContext.getApplicationContext());
  }

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    URL url = new URL(baseUrl + "/testvaadinservice");
    URLConnection connection = url.openConnection();
    connection.connect();
    try (Reader reader = new InputStreamReader(connection.getInputStream())) {
      StringWriter writer = new StringWriter();
      IOUtils.copy(reader, writer);
      if (service == null) {
        logger.warn("Could not get an instanceof {} for the current thread using URL {}",
            VaadinService.class, url);
        logger.debug("Response from /testvaadinservice {}", writer);
      }
    }
    if (service != null) {
      VaadinService.setCurrent(service);
      service.setCurrentInstances(request, response);
      CurrentInstance.set(VaadinSession.class, session);
      session.getLockInstance().lock();
    }
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    if (service != null) {
      service.setCurrentInstances(null, null);
      session.getLockInstance().unlock();
    }
    VaadinService.setCurrent(null);
    CurrentInstance.set(VaadinSession.class, null);
    VaadinServiceTestComponent.service = null;
    VaadinServiceTestComponent.request = null;
    VaadinServiceTestComponent.response = null;
  }
}
