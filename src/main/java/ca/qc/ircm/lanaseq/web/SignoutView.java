package ca.qc.ircm.lanaseq.web;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * Sign out view.
 */
@Route(value = SignoutView.VIEW_NAME)
@AnonymousAllowed
public class SignoutView extends VerticalLayout implements BeforeEnterObserver, UrlComponent {

  public static final String VIEW_NAME = "signout";
  private static final Logger logger = LoggerFactory.getLogger(SignoutView.class);
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected SignoutView(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    logger.debug("Sign out user {}", authenticatedUser.getUser());
    UI.getCurrent().getPage().setLocation(getUrl(MainView.VIEW_NAME));
    CompositeLogoutHandler logoutHandler = new CompositeLogoutHandler(
        new CookieClearingLogoutHandler("remember-me"), new SecurityContextLogoutHandler());
    logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(),
        VaadinServletResponse.getCurrent().getHttpServletResponse(), null);
  }
}
