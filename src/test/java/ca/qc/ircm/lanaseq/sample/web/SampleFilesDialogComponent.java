package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SampleFilesDialog}.
 */
public class SampleFilesDialogComponent extends SeleniumComponent {

  public static Function<WebDriver, SampleFilesDialogComponent> find() {
    return d -> new SampleFilesDialogComponent(d.findElement(By.id(ID)));
  }

  public SampleFilesDialogComponent(WebElement sampleFilesDialog) {
    super(sampleFilesDialog);
    assert ID.equals(sampleFilesDialog.getAttribute("id"));
  }

  public SampleFilesGridComponent files() {
    return new SampleFilesGridComponent(element.findElement(By.id(styleName(ID, FILES))));
  }
}
