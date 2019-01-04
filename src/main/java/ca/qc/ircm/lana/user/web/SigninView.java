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

package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.HASHED_PASSWORD;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;

import ca.qc.ircm.lana.security.web.WebSecurityConfiguration;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Sign in view.
 */
@Route(value = SigninView.VIEW_NAME)
@HtmlImport("styles/shared-styles.html")
@HtmlImport("frontend://bower_components/iron-form/iron-form.html")
public class SigninView extends Composite<VerticalLayout> implements LocaleChangeObserver,
    HasDynamicTitle, PageConfigurator, AfterNavigationObserver, BaseComponent {
  public static final String VIEW_NAME = "signin";
  public static final String HEADER = "header";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String SIGNIN = "signin";
  public static final String DO_SIGNIN = "dosignin";
  public static final String FAIL = "fail";
  public static final String DISABLED = "disabled";
  public static final String EXCESSIVE_ATTEMPTS = "excessiveAttempts";
  private static final long serialVersionUID = 638443368018456019L;
  protected H2 header = new H2();
  protected Element ironForm = new Element("iron-form");
  protected Element form = new Element("form");
  protected TextField email = new TextField();
  protected PasswordField password = new PasswordField();
  protected Button signin = new Button();
  protected Button doSignin = new Button();
  protected Div error = new Div();
  @Inject
  private transient SigninViewPresenter presenter;

  public SigninView() {
  }

  protected SigninView(SigninViewPresenter presenter) {
    this.presenter = presenter;
  }

  @PostConstruct
  void init() {
    VerticalLayout root = getContent();
    root.setId(VIEW_NAME);
    root.add(header);
    header.addClassName(HEADER);
    root.getElement().appendChild(ironForm);
    ironForm.setAttribute("id", "ironform");
    ironForm.setAttribute("allow-redirect", "");
    ironForm.appendChild(form);
    form.setAttribute("method", "post");
    form.setAttribute("action", WebSecurityConfiguration.SIGNIN_PROCESSING_URL);
    VerticalLayout formLayout = new VerticalLayout();
    form.appendChild(formLayout.getElement());
    formLayout.add(email);
    email.addClassName(EMAIL);
    email.getElement().setAttribute("name", USERNAME);
    formLayout.add(password);
    password.addClassName(HASHED_PASSWORD);
    password.getElement().setAttribute("name", PASSWORD);
    formLayout.add(signin);
    signin.addClassName(SIGNIN);
    formLayout.add(doSignin);
    doSignin.setId(DO_SIGNIN);
    doSignin.getStyle().set("display", "none");
    root.add(error);
    error.addClassName(FAIL);
    presenter.init(this);
  }

  @Override
  public void configurePage(InitialPageSettings settings) {
    // Force login page to use Shady DOM to avoid problems with browsers and
    // password managers not supporting shadow DOM
    settings.addInlineWithContents(InitialPageSettings.Position.PREPEND,
        "window.customElements=window.customElements||{};"
            + "window.customElements.forcePolyfill=true;" + "window.ShadyDOM={force:true};",
        InitialPageSettings.WrapMode.JAVASCRIPT);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final MessageResource resources = new MessageResource(getClass(), getLocale());
    final MessageResource userResources = new MessageResource(User.class, getLocale());
    header.setText(resources.message(HEADER));
    email.setLabel(userResources.message(EMAIL));
    password.setLabel(userResources.message(HASHED_PASSWORD));
    signin.setText(resources.message(SIGNIN));
    presenter.localeChange(getLocale());
  }

  @Override
  public String getPageTitle() {
    final MessageResource resources = new MessageResource(getClass(), getLocale());
    final MessageResource generalResources = new MessageResource(WebConstants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    getCurrentUi().getPage().executeJavaScript("document.getElementById('" + DO_SIGNIN
        + "').addEventListener('click', () => document.getElementById('ironform').submit());");
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    presenter.showError(event.getLocation().getQueryParameters().getParameters(), getLocale());
  }
}
