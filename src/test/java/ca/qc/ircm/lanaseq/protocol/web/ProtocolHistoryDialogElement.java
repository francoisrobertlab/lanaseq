package ca.qc.ircm.lanaseq.protocol.web;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ProtocolHistoryDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = ProtocolHistoryDialog.ID)
public class ProtocolHistoryDialogElement extends DialogElement {

  public H2Element header() {
    return $(H2Element.class).first();
  }

  public ProtocolHistoryFilesGridElement files() {
    return $(ProtocolHistoryFilesGridElement.class).first();
  }
}
