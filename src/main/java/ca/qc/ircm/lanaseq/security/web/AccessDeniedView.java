package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.web.MainView;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

/**
 * Access denied view.
 */
@Route("accessdenied")
@AnonymousAllowed
public class AccessDeniedView extends Composite<VerticalLayout>
    implements HasErrorParameter<AccessDeniedException>, LocaleChangeObserver, HasDynamicTitle {

  public static final String VIEW_NAME = "accessdenied";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String HOME = "home";
  private static final String MESSAGE_PREFIX = messagePrefix(AccessDeniedView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = -5974627607641654031L;
  private static final Logger logger = LoggerFactory.getLogger(AccessDeniedView.class);
  protected H2 header = new H2();
  protected Div message = new Div();
  protected Button home = new Button();

  /**
   * Creates new access denied view.
   */
  public AccessDeniedView() {
    logger.debug("access denied view");
    VerticalLayout root = getContent();
    root.setId(VIEW_NAME);
    root.add(header, header, home);
    header.addClassName(HEADER);
    message.addClassName(MESSAGE);
    home.addClassName(HOME);
    home.addClickListener(e -> UI.getCurrent().navigate(MainView.class));
  }

  @Override
  public int setErrorParameter(BeforeEnterEvent event,
      ErrorParameter<AccessDeniedException> parameter) {
    return HttpServletResponse.SC_FORBIDDEN;
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    header.setText(getTranslation(MESSAGE_PREFIX + HEADER));
    message.setText(getTranslation(MESSAGE_PREFIX + MESSAGE));
    home.setText(getTranslation(MESSAGE_PREFIX + HOME));
  }
}
