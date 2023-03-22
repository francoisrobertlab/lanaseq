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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.HASHED_PASSWORD;

import ca.qc.ircm.lanaseq.AppResources;
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
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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
  private static final long serialVersionUID = 638443368018456019L;
  private static final Logger logger = LoggerFactory.getLogger(SigninView.class);
  protected LoginI18n i18n;
  protected String error;
  @Autowired
  private transient SecurityConfiguration configuration;
  @Autowired
  private transient AuthenticatedUser authenticatedUser;

  protected SigninView() {
  }

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
    final AppResources resources = new AppResources(getClass(), getLocale());
    final AppResources userResources = new AppResources(User.class, getLocale());
    i18n = LoginI18n.createDefault();
    i18n.setHeader(new LoginI18n.Header());
    i18n.getHeader().setTitle(resources.message(HEADER));
    i18n.getHeader().setDescription(resources.message(DESCRIPTION));
    i18n.setAdditionalInformation(resources.message(ADDITIONAL_INFORMATION));
    i18n.setForm(new LoginI18n.Form());
    i18n.getForm().setSubmit(resources.message(SIGNIN));
    i18n.getForm().setTitle(resources.message(FORM_TITLE));
    i18n.getForm().setUsername(userResources.message(EMAIL));
    i18n.getForm().setPassword(userResources.message(HASHED_PASSWORD));
    i18n.getForm().setForgotPassword(resources.message(FORGOT_PASSWORD));
    i18n.setErrorMessage(new ErrorMessage());
    if (error == null) {
      error = FAIL;
    }
    i18n.getErrorMessage().setTitle(resources.message(property(error, TITLE)));
    i18n.getErrorMessage()
        .setMessage(resources.message(error, configuration.getLockDuration().getSeconds() / 60));
    setI18n(i18n);
  }

  @Override
  public String getPageTitle() {
    final AppResources resources = new AppResources(getClass(), getLocale());
    final AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
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
