/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.THEME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Forgot password view.
 */
@Route(value = ForgotPasswordView.VIEW_NAME)
public class ForgotPasswordView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent, UrlComponent {
  private static final long serialVersionUID = 4760310643370830640L;
  private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordView.class);
  public static final String VIEW_NAME = "forgotpassword";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String SEPARATOR = "/";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String SAVED = "saved";
  protected H2 header = new H2();
  protected Div message = new Div();
  protected TextField email = new TextField();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  private transient ForgotPasswordViewPresenter presenter;

  @Autowired
  protected ForgotPasswordView(ForgotPasswordViewPresenter presenter) {
    this.presenter = presenter;
  }

  /**
   * Initializes view.
   */
  @PostConstruct
  protected void init() {
    logger.debug("forgot password view");
    setId(ID);
    FormLayout emailLayout = new FormLayout();
    emailLayout.add(email);
    add(header, message, emailLayout, buttonsLayout);
    buttonsLayout.add(save);
    header.setId(HEADER);
    message.setId(MESSAGE);
    email.setId(EMAIL);
    save.setId(SAVE);
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save(getLocale()));
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(getClass(), getLocale());
    final AppResources userResources = new AppResources(User.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    message.setText(resources.message(MESSAGE));
    email.setLabel(userResources.message(EMAIL));
    save.setText(webResources.message(SAVE));
    presenter.localeChange(getLocale());
  }

  @Override
  public String getPageTitle() {
    final AppResources resources = new AppResources(getClass(), getLocale());
    final AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
