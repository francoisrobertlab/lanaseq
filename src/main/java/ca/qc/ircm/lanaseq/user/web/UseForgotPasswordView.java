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

package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.user.ForgotPassword;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.web.SigninView;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.Locale;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Use forgot password view.
 */
@Route(value = UseForgotPasswordView.VIEW_NAME)
@AnonymousAllowed
public class UseForgotPasswordView extends VerticalLayout implements LocaleChangeObserver,
    HasUrlParameter<String>, HasDynamicTitle, NotificationComponent {
  public static final String VIEW_NAME = "useforgotpassword";
  public static final String ID = "useforgotpassword-view";
  public static final String SEPARATOR = "/";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String SAVED = "saved";
  public static final String INVALID = "invalid";
  private static final long serialVersionUID = 4760310643370830640L;
  private static final Logger logger = LoggerFactory.getLogger(UseForgotPasswordView.class);
  protected H2 header = new H2();
  protected Div message = new Div();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected PasswordsForm form = new PasswordsForm();
  private ForgotPassword forgotPassword;
  private transient ForgotPasswordService service;

  @Autowired
  protected UseForgotPasswordView(ForgotPasswordService service) {
    this.service = service;
  }

  /**
   * Initializes view.
   */
  @PostConstruct
  protected void init() {
    logger.debug("use forgot password view");
    setId(ID);
    add(header, message, form, buttonsLayout);
    buttonsLayout.add(save);
    header.setId(HEADER);
    message.setId(MESSAGE);
    save.setId(SAVE);
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(getClass(), getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    message.setText(resources.message(MESSAGE));
    save.setText(webResources.message(SAVE));
  }

  @Override
  public String getPageTitle() {
    final AppResources resources = new AppResources(getClass(), getLocale());
    final AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }

  private boolean validateParameter(String parameter, Locale locale) {
    final AppResources resources = new AppResources(UseForgotPasswordView.class, locale);
    if (parameter == null) {
      showNotification(resources.message(INVALID));
      return false;
    }

    String[] parameters = parameter.split(SEPARATOR, -1);
    boolean valid = true;
    if (parameters.length < 2) {
      valid = false;
    } else {
      try {
        long id = Long.parseLong(parameters[0]);
        String confirmNumber = parameters[1];
        if (!service.get(id, confirmNumber).isPresent()) {
          valid = false;
        }
      } catch (NumberFormatException e) {
        valid = false;
      }
    }
    if (!valid) {
      showNotification(resources.message(INVALID));
    }
    return valid;
  }

  @Override
  public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
    if (validateParameter(parameter, getLocale())) {
      String[] parameters = parameter.split(SEPARATOR, -1);
      long id = Long.parseLong(parameters[0]);
      String confirmNumber = parameters[1];
      forgotPassword = service.get(id, confirmNumber).orElse(null);
    } else {
      save.setEnabled(false);
      form.setEnabled(false);
    }
  }

  void save() {
    if (form.isValid()) {
      String password = form.getPassword();
      logger.debug("save new password for user {}", forgotPassword.getUser());
      service.updatePassword(forgotPassword, password);
      final AppResources resources = new AppResources(UseForgotPasswordView.class, getLocale());
      showNotification(resources.message(SAVED));
      UI.getCurrent().navigate(SigninView.class);
    }
  }
}
