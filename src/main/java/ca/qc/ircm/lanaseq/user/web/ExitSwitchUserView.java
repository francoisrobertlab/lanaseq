package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.security.SwitchUserService.ROLE_PREVIOUS_ADMINISTRATOR;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SwitchUserService;
import ca.qc.ircm.lanaseq.web.MainView;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exit switch user view.
 */
@Route(value = ExitSwitchUserView.VIEW_NAME)
@RolesAllowed({ROLE_PREVIOUS_ADMINISTRATOR})
public class ExitSwitchUserView extends VerticalLayout implements BeforeEnterObserver,
    UrlComponent {

  public static final String VIEW_NAME = "exitSwitchUser";
  private static final Logger logger = LoggerFactory.getLogger(ExitSwitchUserView.class);
  private final transient SwitchUserService switchUserService;
  private final transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected ExitSwitchUserView(SwitchUserService switchUserService,
      AuthenticatedUser authenticatedUser) {
    this.switchUserService = switchUserService;
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    logger.debug("Exit switch user {}", authenticatedUser.getUser());
    switchUserService.exitSwitchUser(VaadinServletRequest.getCurrent());
    UI.getCurrent().getPage().setLocation(getUrlWithContextPath(MainView.class));
  }
}
