package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SamplesView.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SamplesView}.
 */
public class SamplesViewPage extends SeleniumComponent {

  public static Function<WebDriver, SamplesViewPage> find() {
    return d -> new SamplesViewPage(d.findElement(By.id(ID)));
  }

  public SamplesViewPage(WebElement samplesView) {
    super(samplesView);
    assert ID.equals(samplesView.getAttribute("id"));
  }

  public SamplesGridComponent samples() {
    return new SamplesGridComponent(element.findElement(By.id(SAMPLES)));
  }

  public WebElement files() {
    return element.findElement(By.id(FILES));
  }
}
