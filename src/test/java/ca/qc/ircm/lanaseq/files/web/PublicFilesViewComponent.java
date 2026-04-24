package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.DOWNLOAD_LINKS;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.FILES;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.ID;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link PublicFilesView}.
 */
public class PublicFilesViewComponent extends SeleniumComponent {

  public static Function<WebDriver, PublicFilesViewComponent> find() {
    return d -> new PublicFilesViewComponent(d.findElement(By.id(ID)));
  }

  public PublicFilesViewComponent(WebElement publicFilesView) {
    super(publicFilesView);
    assert ID.equals(publicFilesView.getAttribute("id"));
  }

  public PublicFilesGridComponent files() {
    return new PublicFilesGridComponent(element.findElement(By.id(FILES)));
  }

  public WebElement downloadLinks() {
    return element.findElement(By.id(DOWNLOAD_LINKS));
  }
}
