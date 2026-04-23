package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SamplesView.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SamplesView}.
 */
public class SamplesViewPage {

  private final WebElement view;

  public static Function<WebDriver, SamplesViewPage> find() {
    return d -> new SamplesViewPage(d.findElement(By.id(ID)));
  }

  public SamplesViewPage(WebElement samplesView) {
    assert ID.equals(samplesView.getAttribute("id"));
    this.view = samplesView;
  }

  public SamplesGridComponent samples() {
    return new SamplesGridComponent(view.findElement(By.id(SAMPLES)));
  }

  public WebElement files() {
    return view.findElement(By.id(FILES));
  }
}
