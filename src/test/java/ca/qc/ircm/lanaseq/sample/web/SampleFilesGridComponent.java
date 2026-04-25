package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SampleFilesDialog} files grid.
 */
public class SampleFilesGridComponent extends GridComponent {

  private static final int DOWNLOAD_COLUMN = 1;

  public SampleFilesGridComponent(WebElement sampleFilesGrid) {
    super(sampleFilesGrid);
    assert styleName(ID, FILES).equals(sampleFilesGrid.getAttribute("id"));
  }

  public WebElement download(int row) {
    WebElement cell = cell(row, DOWNLOAD_COLUMN);
    return cell.findElement(By.cssSelector("a"));
  }
}
