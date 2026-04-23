package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SamplesView} grid.
 */
public class SamplesGridComponent {

  private final WebElement grid;

  public SamplesGridComponent(WebElement samplesGrid) {
    assert SAMPLES.equals(samplesGrid.getAttribute("id"));
    this.grid = samplesGrid;
  }

  public void select(int row) {
    WebElement body = grid.getShadowRoot().findElement(By.cssSelector("tbody"));
    WebElement gridRow = body.findElements(By.cssSelector("tr")).get(row);
    String slotName = gridRow.findElement(By.cssSelector("td")).findElement(By.cssSelector("slot"))
        .getAttribute("name");
    grid.findElement(By.cssSelector("vaadin-grid-cell-content[slot='" + slotName + "']")).click();
  }
}
