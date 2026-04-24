package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ProtocolHistoryDialog}.
 */
public class ProtocolHistoryDialogComponent extends SeleniumComponent {

  public static Function<WebDriver, ProtocolHistoryDialogComponent> find() {
    return d -> new ProtocolHistoryDialogComponent(d.findElement(By.id(ID)));
  }

  public ProtocolHistoryDialogComponent(WebElement protocolHistoryDialog) {
    super(protocolHistoryDialog);
    assert ID.equals(protocolHistoryDialog.getAttribute("id"));
  }

  public ProtocolHistoryFilesGridComponent files() {
    return new ProtocolHistoryFilesGridComponent(element.findElement(By.id(styleName(ID, FILES))));
  }
}
