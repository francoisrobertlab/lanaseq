package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.UsersView.ID;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER;
import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link UsersView}.
 */
public class UsersViewPage extends SeleniumComponent {

  public static Function<WebDriver, UsersViewPage> find() {
    return d -> new UsersViewPage(d.findElement(By.id(ID)));
  }

  public UsersViewPage(WebElement usersView) {
    super(usersView);
    assert ID.equals(usersView.getAttribute("id"));
  }

  public UsersGridComponent users() {
    return new UsersGridComponent(element.findElement(By.id(USERS)));
  }

  public WebElement switchUser() {
    return element.findElement(By.id(SWITCH_USER));
  }
}
