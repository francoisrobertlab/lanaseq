package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.FILES;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link PublicFilesView} grid.
 */
public class PublicFilesGridComponent extends GridComponent {

  private static final int FILENAME_COLUMN = 0;
  private static final int EXPIRY_DATE_COLUMN = 1;
  private static final int SAMPLE_NAME_COLUMN = 2;
  private static final int OWNER_COLUMN = 3;
  private static final int DELETE_COLUMN = 4;

  public PublicFilesGridComponent(WebElement publicFilesGrid) {
    super(publicFilesGrid);
    assert FILES.equals(publicFilesGrid.getAttribute("id"));
  }

  public WebElement filenameFilter() {
    WebElement cell = headerCell(1, FILENAME_COLUMN);
    return cell.findElement(By.cssSelector("vaadin-text-field"));
  }
}
