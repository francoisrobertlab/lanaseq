package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS;

import ca.qc.ircm.lanaseq.test.config.GridComponent;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ProtocolsView} grid.
 */
public class ProtocolsGridComponent extends GridComponent {

  public ProtocolsGridComponent(WebElement protocolsGrid) {
    super(protocolsGrid);
    assert PROTOCOLS.equals(protocolsGrid.getAttribute("id"));
  }
}
