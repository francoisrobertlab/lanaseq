package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link DatasetFilesDialog} files grid.
 */
public class DatasetFilesGridComponent extends GridComponent {

  private static final int DOWNLOAD_COLUMN = 1;

  public DatasetFilesGridComponent(WebElement datasetFilesGrid) {
    super(datasetFilesGrid);
    assert styleName(ID, FILES).equals(datasetFilesGrid.getAttribute("id"));
  }

  public WebElement download(int row) {
    WebElement cell = cell(row, DOWNLOAD_COLUMN);
    return cell.findElement(By.cssSelector("a"));
  }
}
