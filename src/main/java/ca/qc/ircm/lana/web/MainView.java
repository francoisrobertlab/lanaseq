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

package ca.qc.ircm.lana.web;

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;

import ca.qc.ircm.lana.experiment.web.ExperimentsView;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.web.UsersView;
import ca.qc.ircm.lana.web.component.BaseComponent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main view.
 */
@Route(value = MainView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class MainView extends Composite<VerticalLayout>
    implements BaseComponent, BeforeEnterObserver {
  public static final String VIEW_NAME = "";
  private static final long serialVersionUID = -8440231785563887343L;
  private static final Logger logger = LoggerFactory.getLogger(MainView.class);
  @Inject
  private transient AuthorizationService authorizationService;

  public MainView() {
    logger.debug("main view");
  }

  protected MainView(AuthorizationService authorizationService) {
    this();
    this.authorizationService = authorizationService;
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (authorizationService.hasRole(ADMIN)) {
      event.rerouteTo(UsersView.class);
    } else if (authorizationService.hasRole(MANAGER)) {
      event.rerouteTo(ExperimentsView.class);
    } else {
      event.rerouteTo(ExperimentsView.class);
    }
  }
}
