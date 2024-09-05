package ca.qc.ircm.lanaseq.protocol.web;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ProtocolsView} grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = ProtocolsView.PROTOCOLS)
public class ProtocolsGridElement extends GridElement {
  private static final int NAME_COLUMN = 0;
  private static final int OWNER_COLUMN = 2;

  public TextFieldElement ownerFilter() {
    return getHeaderCell(OWNER_COLUMN).$(TextFieldElement.class).first();
  }
}
