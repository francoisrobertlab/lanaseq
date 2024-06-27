package ca.qc.ircm.lanaseq.protocol.web;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ProtocolHistoryDialog} files grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = ProtocolHistoryDialog.ID + "-" + ProtocolHistoryDialog.FILES)
public class ProtocolHistoryFilesGridElement extends GridElement {
  private static final int FILENAME_COLUMN = 0;
  private static final int RECOVER_COLUMN = 1;

  public AnchorElement filename(int row) {
    return getCell(row, FILENAME_COLUMN).$(AnchorElement.class).first();
  }

  public ButtonElement recover(int row) {
    return getCell(row, RECOVER_COLUMN).$(ButtonElement.class).first();
  }
}
