package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.security.UserRole.ROLE_PREVIOUS_ADMINISTRATOR;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exit switch user view.
 */
@Route(value = ExitSwitchUserView.VIEW_NAME)
@RolesAllowed(ROLE_PREVIOUS_ADMINISTRATOR)
public class ExitSwitchUserView extends VerticalLayout implements UrlComponent {

  public static final String VIEW_NAME = "exitSwitchUser";
  private static final Logger logger = LoggerFactory.getLogger(ExitSwitchUserView.class);
  private final transient AuthenticatedUser authenticatedUser;

  /**
   * Creates ExitSwitchUserView.
   *
   * @param authenticatedUser AuthenticatedUser
   */
  @Autowired
  protected ExitSwitchUserView(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * Initializes exit switch user view.
   */
  @PostConstruct
  protected void init() {
    logger.debug("Exit switch user view");
    addAttachListener(event -> exitSwitchUser());
  }

  private void exitSwitchUser() {
    logger.debug("Exit switch user {}", authenticatedUser.getUser());
    getUI().ifPresent(ui -> ui.getPage().setLocation(prependContextPath("impersonate/exit")));
  }
}
