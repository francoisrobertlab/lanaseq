package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link DatasetsView}.
 */
public class DatasetsViewPage {

  private final WebElement view;

  public static Function<WebDriver, DatasetsViewPage> find() {
    return d -> new DatasetsViewPage(d.findElement(By.id(ID)));
  }

  public DatasetsViewPage(WebElement datasetsView) {
    assert ID.equals(datasetsView.getAttribute("id"));
    this.view = datasetsView;
  }
}
