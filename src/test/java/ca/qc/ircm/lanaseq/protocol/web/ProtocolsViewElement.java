package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.elementsbase.Element;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@Element("vaadin-vertical-layout")
public class ProtocolsViewElement extends VerticalLayoutElement {
  private static final int NAME_COLUMN = 0;

  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public GridElement protocols() {
    return $(GridElement.class).id(PROTOCOLS);
  }

  public void doubleClickProtocol(int row) {
    protocols().getCell(row, NAME_COLUMN).doubleClick();
  }

  public ButtonElement add() {
    return $(ButtonElement.class).id(ADD);
  }
}
