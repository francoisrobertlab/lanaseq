package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.HASHED_PASSWORD;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.SecurityConfiguration;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.web.ForgotPasswordView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginI18n.ErrorMessage;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Sign in view.
 */
@Route(value = SigninView.VIEW_NAME)
@AnonymousAllowed
@JsModule("./styles/shared-styles.js")
public class SigninView extends LoginOverlay
    implements LocaleChangeObserver, HasDynamicTitle, AfterNavigationObserver, BeforeEnterObserver {

  public static final String VIEW_NAME = "signin";
  public static final String ID = "signin-view";
  public static final String HEADER = "header";
  public static final String DESCRIPTION = "description";
  public static final String ADDITIONAL_INFORMATION = "additionalInformation";
  public static final String FORM_TITLE = "form.title";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String SIGNIN = "signin";
  public static final String FORGOT_PASSWORD = "forgotPassword";
  public static final String FAIL = "fail";
  public static final String DISABLED = "disabled";
  public static final String LOCKED = "locked";
  private static final String MESSAGE_PREFIX = messagePrefix(SigninView.class);
  private static final String USER_PREFIX = messagePrefix(User.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 638443368018456019L;
  private static final Logger logger = LoggerFactory.getLogger(SigninView.class);
  protected LoginI18n i18n;
  protected String error;
  private final transient SecurityConfiguration configuration;
  private final transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected SigninView(SecurityConfiguration configuration, AuthenticatedUser authenticatedUser) {
    this.configuration = configuration;
    this.authenticatedUser = authenticatedUser;
  }

  @PostConstruct
  void init() {
    logger.debug("signin view");
    setId(ID);
    addLoginListener(e -> setError(false));
    setForgotPasswordButtonVisible(true);
    setAction(VIEW_NAME);
    setOpened(true);
    addForgotPasswordListener(e -> UI.getCurrent().navigate(ForgotPasswordView.class));
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    // Redirect to main view if user is known.
    if (!authenticatedUser.isAnonymous()) {
      logger.debug("user is known, redirecting to main view");
      event.forwardTo(MainView.class);
    }
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    i18n = LoginI18n.createDefault();
    i18n.setHeader(new LoginI18n.Header());
    i18n.getHeader().setTitle(getTranslation(MESSAGE_PREFIX + HEADER));
    i18n.getHeader().setDescription(getTranslation(MESSAGE_PREFIX + DESCRIPTION));
    i18n.setAdditionalInformation(getTranslation(MESSAGE_PREFIX + ADDITIONAL_INFORMATION));
    i18n.setForm(new LoginI18n.Form());
    i18n.getForm().setSubmit(getTranslation(MESSAGE_PREFIX + SIGNIN));
    i18n.getForm().setTitle(getTranslation(MESSAGE_PREFIX + FORM_TITLE));
    i18n.getForm().setUsername(getTranslation(USER_PREFIX + EMAIL));
    i18n.getForm().setPassword(getTranslation(USER_PREFIX + HASHED_PASSWORD));
    i18n.getForm().setForgotPassword(getTranslation(MESSAGE_PREFIX + FORGOT_PASSWORD));
    i18n.setErrorMessage(new ErrorMessage());
    if (error == null) {
      error = FAIL;
    }
    i18n.getErrorMessage().setTitle(getTranslation(MESSAGE_PREFIX + property(error, TITLE)));
    i18n.getErrorMessage().setMessage(
        getTranslation(MESSAGE_PREFIX + error, configuration.lockDuration().getSeconds() / 60));
    setI18n(i18n);
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();
    if (parameters.containsKey(DISABLED)) {
      error = DISABLED;
      setError(true);
    } else if (parameters.containsKey(LOCKED)) {
      error = LOCKED;
      setError(true);
    } else if (parameters.containsKey(FAIL) || parameters.containsKey("error")) {
      error = FAIL;
      setError(true);
    }
  }

  void fireForgotPasswordEvent() {
    fireEvent(new ForgotPasswordEvent(this, false));
  }
}
