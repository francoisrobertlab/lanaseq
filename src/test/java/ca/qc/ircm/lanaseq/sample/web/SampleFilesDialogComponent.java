package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link SampleFilesDialog}.
 */
public class SampleFilesDialogComponent {

  private final WebElement dialog;

  public static Function<WebDriver, SampleFilesDialogComponent> find() {
    return d -> new SampleFilesDialogComponent(d.findElement(By.id(ID)));
  }

  public SampleFilesDialogComponent(WebElement sampleFilesDialog) {
    assert ID.equals(sampleFilesDialog.getAttribute("id"));
    this.dialog = sampleFilesDialog;
  }

  public SampleFilesGridComponent files() {
    return new SampleFilesGridComponent(dialog.findElement(By.id(styleName(ID, FILES))));
  }
}
