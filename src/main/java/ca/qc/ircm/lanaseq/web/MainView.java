package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.security.UserRole.USER;

import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.io.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main view.
 */
@Route(value = MainView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({USER})
public class MainView extends Composite<VerticalLayout> implements BeforeEnterObserver {

  public static final String VIEW_NAME = "";
  @Serial
  private static final long serialVersionUID = -8440231785563887343L;
  private static final Logger logger = LoggerFactory.getLogger(MainView.class);
  private transient AuthenticatedUser authenticatedUser;

  public MainView() {
    logger.debug("main view");
  }

  @Autowired
  protected MainView(AuthenticatedUser authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    event.forwardTo(DatasetsView.class);
  }
}
