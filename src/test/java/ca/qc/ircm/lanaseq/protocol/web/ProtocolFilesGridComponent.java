package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.ID;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ProtocolDialog} files grid.
 */
public class ProtocolFilesGridComponent extends GridComponent {

  private static final int FILENAME_COLUMN = 0;

  public ProtocolFilesGridComponent(WebElement protocolFilesGrid) {
    super(protocolFilesGrid);
    assert styleName(ID, FILES).equals(protocolFilesGrid.getAttribute("id"));
  }

  public WebElement filename(int row) {
    WebElement cell = cell(row, FILENAME_COLUMN);
    return cell.findElement(By.cssSelector("a"));
  }
}
