package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HISTORY;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

/**
 * Web element for {@link ProtocolsView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = ProtocolsView.ID)
public class ProtocolsViewElement extends VerticalLayoutElement {
  public ProtocolsGridElement protocols() {
    return $(ProtocolsGridElement.class).first();
  }

  public ButtonElement add() {
    return $(ButtonElement.class).id(ADD);
  }

  public ButtonElement history() {
    return $(ButtonElement.class).id(HISTORY);
  }

  public ProtocolDialogElement dialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(ProtocolDialog.ID)))
        .wrap(ProtocolDialogElement.class);
  }

  public ProtocolHistoryDialogElement historyDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(ProtocolHistoryDialog.ID)))
        .wrap(ProtocolHistoryDialogElement.class);
  }
}
