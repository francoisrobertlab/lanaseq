package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link DatasetFilesDialog}.
 */
public class DatasetFilesDialogComponent extends SeleniumComponent {

  public static Function<WebDriver, DatasetFilesDialogComponent> find() {
    return d -> new DatasetFilesDialogComponent(d.findElement(By.id(ID)));
  }

  public DatasetFilesDialogComponent(WebElement datasetFilesDialog) {
    super(datasetFilesDialog);
    assert ID.equals(datasetFilesDialog.getAttribute("id"));
  }

  public DatasetFilesGridComponent files() {
    return new DatasetFilesGridComponent(element.findElement(By.id(styleName(ID, FILES))));
  }
}
