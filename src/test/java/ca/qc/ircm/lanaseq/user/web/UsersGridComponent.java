package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.UsersView.USERS;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link UsersView} grid.
 */
public class UsersGridComponent {

  private final WebElement grid;

  public UsersGridComponent(WebElement usersGrid) {
    assert USERS.equals(usersGrid.getAttribute("id"));
    this.grid = usersGrid;
  }

  public void select(int row) {
    WebElement body = grid.getShadowRoot().findElement(By.cssSelector("tbody"));
    WebElement gridRow = body.findElements(By.cssSelector("tr")).get(row);
    String slotName = gridRow.findElement(By.cssSelector("td")).findElement(By.cssSelector("slot"))
        .getAttribute("name");
    grid.findElement(By.cssSelector("vaadin-grid-cell-content[slot='" + slotName + "']")).click();
  }
}
