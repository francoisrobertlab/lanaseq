package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.io.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Profile view.
 */
@Route(value = ProfileView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed(USER)
public class ProfileView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent {

  public static final String VIEW_NAME = "profile";
  public static final String ID = "profile-view";
  public static final String SAVED = "saved";
  private static final String MESSAGE_PREFIX = messagePrefix(ProfileView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 1252966315920684518L;
  private static final Logger logger = LoggerFactory.getLogger(ProfileView.class);
  protected UserForm form;
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  private final transient UserService service;
  private final transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected ProfileView(UserService service, AuthenticatedUser authenticatedUser, UserForm form) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
    this.form = form;
  }

  /**
   * Initializes user dialog.
   */
  @PostConstruct
  protected void init() {
    logger.debug("profile view");
    setId(ID);
    setMaxWidth("40em");
    add(form, save);
    save.setId(SAVE);
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    form.setUser(authenticatedUser.getUser().orElseThrow());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    save.setText(getTranslation(CONSTANTS_PREFIX + SAVE));
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  void save() {
    if (form.isValid()) {
      User user = form.getUser();
      String password = form.getPassword();
      logger.debug("save user {}", user);
      service.save(user, password);
      showNotification(getTranslation(MESSAGE_PREFIX + SAVED, user.getEmail()));
    }
  }
}
