package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserAuthority;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.PasswordView;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

/**
 * Adds before enter listener to check access to views.
 */
@SpringComponent
public class ConfigureUiServiceInitListener implements VaadinServiceInitListener {

  private static final String MESSAGE_PREFIX = messagePrefix(ConfigureUiServiceInitListener.class);
  @Serial
  private static final long serialVersionUID = 1796331428220223698L;
  private static final Logger logger =
      LoggerFactory.getLogger(ConfigureUiServiceInitListener.class);
  private final AuthenticatedUser authenticatedUser;

  @Autowired
  protected ConfigureUiServiceInitListener(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event.getSource().addUIInitListener(uiEvent -> {
      UI ui = uiEvent.getUI();
      ui.addBeforeEnterListener(this::beforeEnter);
      ui.addAfterNavigationListener(this::afterNavigation);
    });
  }

  /**
   * Reroutes the user if she is not authorized to access the view.
   *
   * @param event event
   */
  private void beforeEnter(BeforeEnterEvent event) {
    if (!authenticatedUser.isAuthorized(event.getNavigationTarget())) {
      if (authenticatedUser.isAnonymous()) {
        logger.debug("user is anonymous, redirect to {}", SigninView.class.getSimpleName());
        UI.getCurrent().navigate(SigninView.class);
      } else {
        UI ui = event.getUI();
        String email = authenticatedUser.getUser().map(User::getEmail).orElse("<anonymous>");
        String message =
            ui.getTranslation(MESSAGE_PREFIX + AccessDeniedException.class.getSimpleName(), email,
                event.getNavigationTarget().getSimpleName());
        logger.info(message);
        event.rerouteToError(new AccessDeniedException(message), message);
      }
    }
  }

  /**
   * Reroutes the user to change password, if they are forced to.
   *
   * @param event event
   */
  private void afterNavigation(AfterNavigationEvent event) {
    if (authenticatedUser.hasRole(UserAuthority.FORCE_CHANGE_PASSWORD)
        && !event.getLocation().getPath().equals(PasswordView.VIEW_NAME)) {
      logger.debug("user has role {}, redirect to {}", UserAuthority.FORCE_CHANGE_PASSWORD,
          PasswordView.class.getSimpleName());
      UI.getCurrent().navigate(PasswordView.class);
    }
  }
}
