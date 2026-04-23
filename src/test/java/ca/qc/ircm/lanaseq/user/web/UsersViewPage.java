package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.UsersView.ID;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER;
import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link UsersView}.
 */
public class UsersViewPage {

  private final WebElement view;

  public static Function<WebDriver, UsersViewPage> find() {
    return d -> new UsersViewPage(d.findElement(By.id(ID)));
  }

  public UsersViewPage(WebElement usersView) {
    assert ID.equals(usersView.getAttribute("id"));
    this.view = usersView;
  }

  public UsersGridComponent users() {
    return new UsersGridComponent(view.findElement(By.id(USERS)));
  }

  public WebElement switchUser() {
    return view.findElement(By.id(SWITCH_USER));
  }
}
