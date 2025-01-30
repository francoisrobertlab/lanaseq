package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.REFRESH;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.id;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link AddSampleFilesDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = AddSampleFilesDialog.ID)
public class AddSampleFilesDialogElement extends DialogElement {
  public H2Element header() {
    return $(H2Element.class).first();
  }

  public DivElement message() {
    return $(DivElement.class).id(id(MESSAGE));
  }

  public GridElement files() {
    return $(GridElement.class).id(id(FILES));
  }

  public ButtonElement refresh() {
    return $(ButtonElement.class).id(id(REFRESH));
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(id(SAVE));
  }
}
