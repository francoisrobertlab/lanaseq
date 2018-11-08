package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.user.UserProperties.EMAIL;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserService;
import ca.qc.ircm.lana.web.MainView;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.templatemodel.TemplateModel;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Users view.
 */
@Tag("users-view")
@HtmlImport("src/user/users-view.html")
@Route(value = UsersView.VIEW_NAME, layout = MainView.class)
@RouteAlias(value = "")
public class UsersView extends PolymerTemplate<UsersView.UsersViewModel> {
  public static final String VIEW_NAME = "users";
  private static final long serialVersionUID = 2568742367790329628L;
  @Id("h2")
  private H2 h2;
  @Id("users")
  private Grid<User> users;
  @Inject
  private UserService userService;

  /**
   * Creates a new UsersView.
   */
  public UsersView() {
    users.addColumn(user -> user.getEmail(), EMAIL).setKey(EMAIL).setHeader("Email");
  }

  @PostConstruct
  void init() {
    users.setItems(userService.all());
  }

  /**
   * This model binds properties between UsersView and users-view.html
   */
  public interface UsersViewModel extends TemplateModel {
    // Add setters and getters for template properties here.
  }
}
