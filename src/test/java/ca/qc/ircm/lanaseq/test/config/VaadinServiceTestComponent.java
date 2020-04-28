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

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("testvaadinservice")
@UIScope
public class VaadinServiceTestComponent extends Label {
  private static final long serialVersionUID = 7709133504135340522L;
  private static final Logger logger = LoggerFactory.getLogger(VaadinServiceTestComponent.class);
  public static VaadinService service;
  public static VaadinRequest request;
  public static VaadinResponse response;
  public static VaadinSession session;

  /**
   * Creates test component to trap Vaadin service.
   */
  public VaadinServiceTestComponent() {
    logger.debug("getting VaadinService instance");
    service = VaadinService.getCurrent();
    request = VaadinRequest.getCurrent();
    response = VaadinResponse.getCurrent();
    session = VaadinSession.getCurrent();
    setText("VaadinService saved");
  }
}
