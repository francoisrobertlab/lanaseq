package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetGrid.ID;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link DatasetGrid}.
 */
public class DatasetGridComponent extends GridComponent {

  public DatasetGridComponent(WebElement datasetGrid) {
    super(datasetGrid);
    assert ID.equals(datasetGrid.getAttribute("id"));
  }
}
