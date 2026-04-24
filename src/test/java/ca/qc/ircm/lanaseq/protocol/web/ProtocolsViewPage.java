package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HISTORY;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS;

import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Web element for {@link ProtocolsView}.
 */
public class ProtocolsViewPage {

  private final WebElement view;

  public static Function<WebDriver, ProtocolsViewPage> find() {
    return d -> new ProtocolsViewPage(d.findElement(By.id(ID)));
  }

  public ProtocolsViewPage(WebElement protocolsView) {
    assert ID.equals(protocolsView.getAttribute("id"));
    this.view = protocolsView;
  }

  public ProtocolsGridComponent protocols() {
    return new ProtocolsGridComponent(view.findElement(By.id(PROTOCOLS)));
  }

  public WebElement edit() {
    return view.findElement(By.id(EDIT));
  }

  public WebElement history() {
    return view.findElement(By.id(HISTORY));
  }
}
