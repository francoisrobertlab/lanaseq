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
import static ca.qc.ircm.lanaseq.Constants.INVALID_EMAIL;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.SpringConfiguration.messagePrefix;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.user.ForgotPasswordService;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.web.SigninView;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import ca.qc.ircm.lanaseq.web.component.UrlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Forgot password view.
 */
@Route(value = ForgotPasswordView.VIEW_NAME)
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent, UrlComponent {
  public static final String VIEW_NAME = "forgotpassword";
  public static final String ID = "forgotpassword-view";
  public static final String SEPARATOR = "/";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String SAVED = "saved";
  private static final String MESSAGE_PREFIX = messagePrefix(ForgotPasswordView.class);
  private static final String USER_PREFIX = messagePrefix(User.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = 4760310643370830640L;
  private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordView.class);
  protected H2 header = new H2();
  protected Div message = new Div();
  protected TextField email = new TextField();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  private Binder<User> binder = new BeanValidationBinder<User>(User.class);
  private transient ForgotPasswordService service;
  private transient UserService userService;

  @Autowired
  protected ForgotPasswordView(ForgotPasswordService service, UserService userService) {
    this.service = service;
    this.userService = userService;
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
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    binder.setBean(new User());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    header.setText(getTranslation(MESSAGE_PREFIX + HEADER));
    message.setText(getTranslation(MESSAGE_PREFIX + MESSAGE));
    email.setLabel(getTranslation(USER_PREFIX + EMAIL));
    save.setText(getTranslation(CONSTANTS_PREFIX + SAVE));
    binder.forField(email).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("")
        .withValidator(new EmailValidator(getTranslation(CONSTANTS_PREFIX + INVALID_EMAIL)))
        .bind(EMAIL);
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  BinderValidationStatus<User> validateUser() {
    return binder.validate();
  }

  boolean validate() {
    return validateUser().isOk();
  }

  void save() {
    if (validate()) {
      String email = this.email.getValue();
      logger.debug("create new forgot password for user {}", email);
      if (userService.exists(email)) {
        service.insert(email, (fp, fplocale) -> getUrl(UseForgotPasswordView.VIEW_NAME) + "/"
            + fp.getId() + UseForgotPasswordView.SEPARATOR + fp.getConfirmNumber());
      }
      showNotification(getTranslation(MESSAGE_PREFIX + SAVED, email));
      UI.getCurrent().navigate(SigninView.class);
    }
  }
}
