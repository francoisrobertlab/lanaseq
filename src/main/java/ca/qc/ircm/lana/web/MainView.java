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

import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.web.SigninView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main layout.
 */
public class MainView extends VerticalLayout implements RouterLayout, BeforeEnterObserver {
  private static final long serialVersionUID = 710800815636494374L;
  private static final Logger logger = LoggerFactory.getLogger(MainView.class);
  @Inject
  private AuthorizationService authorizationService;

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (!authorizationService.isAuthorized(event.getNavigationTarget())) {
      if (authorizationService.isAnonymous()) {
        event.rerouteTo(SigninView.class);
      } else {
        logger.info("User {} does not have access to {}", authorizationService.currentUser(),
            event.getNavigationTarget());
        // Reroute to error.
        logger.warn("Access denied, but no access denied view");
        //event.rerouteToError(AccessDeniedView.class);
      }
    }
  }
}
