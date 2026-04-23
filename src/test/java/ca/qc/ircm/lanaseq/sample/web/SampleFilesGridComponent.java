package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SampleFilesDialog} files grid.
 */
public class SampleFilesGridComponent {

  private static final int DOWNLOAD_COLUMN = 1;
  private final WebElement grid;

  public SampleFilesGridComponent(WebElement sampleFilesGrid) {
    assert styleName(ID, FILES).equals(sampleFilesGrid.getAttribute("id"));
    this.grid = sampleFilesGrid;
  }

  public WebElement download(int row) {
    WebElement body = grid.getShadowRoot().findElement(By.cssSelector("tbody"));
    WebElement gridRow = body.findElements(By.cssSelector("tr")).get(row);
    String slotName = gridRow.findElements(By.cssSelector("td")).get(DOWNLOAD_COLUMN)
        .findElement(By.cssSelector("slot")).getAttribute("name");
    WebElement cell = grid.findElement(
        By.cssSelector("vaadin-grid-cell-content[slot='" + slotName + "']"));
    return cell.findElement(By.cssSelector("a"));
  }
}
