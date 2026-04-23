package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SamplesView} grid.
 */
public class SamplesGridComponent extends GridComponent {

  public SamplesGridComponent(WebElement samplesGrid) {
    super(samplesGrid);
    assert SAMPLES.equals(samplesGrid.getAttribute("id"));
  }
}
