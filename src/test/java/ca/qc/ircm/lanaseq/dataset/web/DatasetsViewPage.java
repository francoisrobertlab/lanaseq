package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link DatasetsView}.
 */
public class DatasetsViewPage extends SeleniumComponent {

  public static Function<WebDriver, DatasetsViewPage> find() {
    return d -> new DatasetsViewPage(d.findElement(By.id(ID)));
  }

  public DatasetsViewPage(WebElement datasetsView) {
    super(datasetsView);
    assert ID.equals(datasetsView.getAttribute("id"));
  }

  public DatasetGridComponent datasets() {
    return new DatasetGridComponent(element.findElement(By.id(DatasetGrid.ID)));
  }

  public WebElement files() {
    return element.findElement(By.id(FILES));
  }
}
