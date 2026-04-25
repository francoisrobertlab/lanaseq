package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ProtocolHistoryDialog} files grid.
 */
public class ProtocolHistoryFilesGridComponent extends GridComponent {

  private static final int FILENAME_COLUMN = 0;

  public ProtocolHistoryFilesGridComponent(WebElement protocolHistoryFilesGrid) {
    super(protocolHistoryFilesGrid);
    assert styleName(ID, FILES).equals(protocolHistoryFilesGrid.getAttribute("id"));
  }

  public WebElement filename(int row) {
    WebElement cell = cell(row, FILENAME_COLUMN);
    return cell.findElement(By.cssSelector("a"));
  }
}
