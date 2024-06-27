package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.id;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link AddDatasetFilesDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = AddDatasetFilesDialog.ID)
public class AddDatasetFilesDialogElement extends DialogElement {
  public H2Element header() {
    return $(H2Element.class).first();
  }

  public DivElement message() {
    return $(DivElement.class).id(id(MESSAGE));
  }

  public GridElement files() {
    return $(GridElement.class).id(id(FILES));
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(id(SAVE));
  }
}
