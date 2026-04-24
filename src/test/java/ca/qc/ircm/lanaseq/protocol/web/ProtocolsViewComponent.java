package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HISTORY;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS;

import ca.qc.ircm.lanaseq.test.config.SeleniumComponent;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ProtocolsView}.
 */
public class ProtocolsViewComponent extends SeleniumComponent {

  public static Function<WebDriver, ProtocolsViewComponent> find() {
    return d -> new ProtocolsViewComponent(d.findElement(By.id(ID)));
  }

  public ProtocolsViewComponent(WebElement protocolsView) {
    super(protocolsView);
    assert ID.equals(protocolsView.getAttribute("id"));
  }

  public ProtocolsGridComponent protocols() {
    return new ProtocolsGridComponent(element.findElement(By.id(PROTOCOLS)));
  }

  public WebElement edit() {
    return element.findElement(By.id(EDIT));
  }

  public WebElement history() {
    return element.findElement(By.id(HISTORY));
  }
}
