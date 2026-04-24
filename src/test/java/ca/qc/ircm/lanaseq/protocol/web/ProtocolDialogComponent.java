package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ProtocolDialog}.
 */
public class ProtocolDialogComponent {

  private final WebElement dialog;

  public static Function<WebDriver, ProtocolDialogComponent> find() {
    return d -> new ProtocolDialogComponent(d.findElement(By.id(ID)));
  }

  public ProtocolDialogComponent(WebElement protocolDialog) {
    assert ID.equals(protocolDialog.getAttribute("id"));
    this.dialog = protocolDialog;
  }

  public ProtocolFilesGridComponent files() {
    return new ProtocolFilesGridComponent(dialog.findElement(By.id(styleName(ID, FILES))));
  }
}
