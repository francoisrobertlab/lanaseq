package ca.qc.ircm.lanaseq.sample.web;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link SelectSampleDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = SelectSampleDialog.ID)
public class SelectSampleDialogElement extends DialogElement {
  public SelectSampleGridElement samples() {
    return $(SelectSampleGridElement.class).first();
  }
}
