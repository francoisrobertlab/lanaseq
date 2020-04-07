package ca.qc.ircm.lana.security.web;

import ca.qc.ircm.lana.AppResources;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.UserAuthority;
import ca.qc.ircm.lana.user.web.PasswordView;
import ca.qc.ircm.lana.user.web.SigninView;
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
    if (authorizationService.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)) {
      logger.debug("user has role {}, redirect to {}", UserAuthority.FORCE_CHANGE_PASSWORD,
          PasswordView.class.getSimpleName());
      event.rerouteTo(PasswordView.class);
    }
    if (!authorizationService.isAuthorized(event.getNavigationTarget())) {
      if (authorizationService.isAnonymous()) {
        logger.debug("user is anonymous, redirect to {}", SigninView.class.getSimpleName());
        event.rerouteTo(SigninView.class);
      } else {
        UI ui = event.getUI();
        AppResources resources =
            new AppResources(ConfigureUiServiceInitListener.class, ui.getLocale());
        String message = resources.message(AccessDeniedException.class.getSimpleName(),
            authorizationService.currentUser().getEmail(),
            event.getNavigationTarget().getSimpleName());
        logger.info(message);
        event.rerouteToError(new AccessDeniedException(message), message);
      }
    }
  }
}
