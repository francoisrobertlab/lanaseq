package ca.qc.ircm.lana.user.web;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.templatemodel.TemplateModel;

/**
 * Sign in view.
 */
@Route(value = SigninView.VIEW_NAME)
@Tag("signin-view")
@HtmlImport("src/user/signin-view.html")
public class SigninView extends PolymerTemplate<SigninView.SigninViewModel>
    implements PageConfigurator, AfterNavigationObserver {
  public static final String VIEW_NAME = "signin";
  public static final String HEADER = "h2";
  public static final String SIGNIN = "signin";
  public static final String PASSWORD = "password";
  public static final String USERNAME = "username";
  private static final long serialVersionUID = 638443368018456019L;
  @Id("h2")
  private H2 h2;
  @Id("username")
  private TextField username;
  @Id("password")
  private PasswordField password;
  @Id("signin")
  private Button signin;

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
  public void afterNavigation(AfterNavigationEvent event) {
    boolean error = event.getLocation().getQueryParameters().getParameters().containsKey("error");
    getModel().setError(error);
  }

  /**
   * This model binds properties between SigninView and signin-view.html
   */
  public interface SigninViewModel extends TemplateModel {
    void setError(boolean error);
  }
}
