package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;

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
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.util.Locale;
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
  private static final String MESSAGE_PREFIX = messagePrefix(UseForgotPasswordView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 4760310643370830640L;
  private static final Logger logger = LoggerFactory.getLogger(UseForgotPasswordView.class);
  protected H2 header = new H2();
  protected Div message = new Div();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected PasswordsForm form = new PasswordsForm();
  private ForgotPassword forgotPassword;
  private final transient ForgotPasswordService service;

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
    form.setMaxWidth("30em");
    form.setRequired(true);
    save.setId(SAVE);
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    header.setText(getTranslation(MESSAGE_PREFIX + HEADER));
    message.setText(getTranslation(MESSAGE_PREFIX + MESSAGE));
    save.setText(getTranslation(CONSTANTS_PREFIX + SAVE));
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  private boolean validateParameter(String parameter, Locale locale) {
    String[] parameters = parameter.split(SEPARATOR, -1);
    boolean valid = true;
    if (parameters.length < 2) {
      valid = false;
    } else {
      try {
        long id = Long.parseLong(parameters[0]);
        String confirmNumber = parameters[1];
        if (service.get(id, confirmNumber).isEmpty()) {
          valid = false;
        }
      } catch (NumberFormatException e) {
        valid = false;
      }
    }
    if (!valid) {
      showNotification(getTranslation(MESSAGE_PREFIX + INVALID));
    }
    return valid;
  }

  @Override
  public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
    if (validateParameter(parameter, getLocale())) {
      String[] parameters = parameter.split(SEPARATOR, -1);
      long id = Long.parseLong(parameters[0]);
      String confirmNumber = parameters[1];
      forgotPassword = service.get(id, confirmNumber).orElseThrow();
    } else {
      save.setEnabled(false);
      form.setEnabled(false);
    }
  }

  void save() {
    if (form.isValid()) {
      String password = form.getPassword();
      assert password != null;
      logger.debug("save new password for user {}", forgotPassword.getUser());
      service.updatePassword(forgotPassword, password);
      showNotification(getTranslation(MESSAGE_PREFIX + SAVED));
      UI.getCurrent().navigate(SigninView.class);
    }
  }
}
