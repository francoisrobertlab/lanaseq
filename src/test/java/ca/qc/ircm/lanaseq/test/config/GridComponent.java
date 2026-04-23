package ca.qc.ircm.lanaseq.test.config;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for grid.
 */
public class GridComponent {

  private final WebElement grid;

  public GridComponent(WebElement grid) {
    assert "vaadin-grid".equals(grid.getTagName());
    this.grid = grid;
  }

  protected WebElement cell(int row, int column) {
    WebElement body = grid.getShadowRoot().findElement(By.cssSelector("tbody"));
    WebElement gridRow = body.findElements(By.cssSelector("tr")).get(row);
    String slotName = gridRow.findElements(By.cssSelector("td")).get(column)
        .findElement(By.cssSelector("slot")).getAttribute("name");
    return grid.findElement(By.cssSelector("vaadin-grid-cell-content[slot='" + slotName + "']"));
  }

  public void select(int row) {
    cell(row, 0).click();
  }
}
