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

package ca.qc.ircm.lanaseq.security.web;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserAuthority;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

/**
 * Adds before enter listener to check access to views.
 */
@SpringComponent
public class ConfigureUiServiceInitListener implements VaadinServiceInitListener {
  private static final long serialVersionUID = 1796331428220223698L;
  private static final Logger logger =
      LoggerFactory.getLogger(ConfigureUiServiceInitListener.class);
  @Autowired
  private AuthorizationService authorizationService;

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event.getSource().addUIInitListener(uiEvent -> {
      UI ui = uiEvent.getUI();
      ui.addBeforeEnterListener(this::beforeEnter);
    });
  }

  /**
   * Reroutes the user if she is not authorized to access the view.
   *
   * @param event
   *          before navigation event with event details
   */
  private void beforeEnter(BeforeEnterEvent event) {
    if (authorizationService.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)
        && !event.getLocation().getPath().equals(PasswordView.VIEW_NAME)) {
      logger.debug("user has role {}, redirect to {}", UserAuthority.FORCE_CHANGE_PASSWORD,
          PasswordView.class.getSimpleName());
      UI.getCurrent().navigate(PasswordView.class);
    }
    if (!authorizationService.isAuthorized(event.getNavigationTarget())) {
      if (authorizationService.isAnonymous()) {
        logger.debug("user is anonymous, redirect to {}", SigninView.class.getSimpleName());
        UI.getCurrent().navigate(SigninView.class);
      } else {
        UI ui = event.getUI();
        AppResources resources =
            new AppResources(ConfigureUiServiceInitListener.class, ui.getLocale());
        User user = authorizationService.getCurrentUser();
        String email = user != null ? user.getEmail() : null;
        String message = resources.message(AccessDeniedException.class.getSimpleName(), email,
            event.getNavigationTarget().getSimpleName());
        logger.info(message);
        event.rerouteToError(new AccessDeniedException(message), message);
      }
    }
  }
}
