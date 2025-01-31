package ca.qc.ircm.lanaseq.protocol.web;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ProtocolDialog} files grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = ProtocolDialog.ID + "-" + ProtocolDialog.FILES)
public class ProtocolFilesGridElement extends GridElement {

  private static final int FILENAME_COLUMN = 0;

  public AnchorElement filename(int row) {
    return getCell(row, FILENAME_COLUMN).$(AnchorElement.class).first();
  }
}
