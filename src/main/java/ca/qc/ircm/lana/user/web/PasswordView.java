package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;

import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Change password view.
 */
@Route(value = PasswordView.VIEW_NAME)
public class PasswordView extends Composite<VerticalLayout>
    implements LocaleChangeObserver, HasDynamicTitle, BaseComponent {
  public static final String VIEW_NAME = "password";
  public static final String HEADER = "header";
  private static final long serialVersionUID = -8554355390432590290L;
  protected H2 header = new H2();
  protected PasswordsForm passwords = new PasswordsForm();
  protected Button save = new Button();
  @Inject
  private PasswordViewPresenter presenter;

  protected PasswordView() {
  }

  protected PasswordView(PasswordViewPresenter presenter) {
    this.presenter = presenter;
  }

  @PostConstruct
  void init() {
    VerticalLayout layout = getContent();
    layout.setId(VIEW_NAME);
    layout.add(header, passwords, save);
    header.addClassName(HEADER);
    save.addClassName(SAVE);
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    MessageResource resources = new MessageResource(PasswordView.class, getLocale());
    MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    header.setText(resources.message(HEADER));
    save.setText(webResources.message(SAVE));
  }

  @Override
  public String getPageTitle() {
    MessageResource resources = new MessageResource(PasswordView.class, getLocale());
    MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
    return resources.message(TITLE, webResources.message(APPLICATION_NAME));
  }
}
