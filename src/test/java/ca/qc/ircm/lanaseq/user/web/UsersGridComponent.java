package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link UsersView} grid.
 */
public class UsersGridComponent extends GridComponent {

  public UsersGridComponent(WebElement usersGrid) {
    super(usersGrid);
    assert USERS.equals(usersGrid.getAttribute("id"));
  }
}
