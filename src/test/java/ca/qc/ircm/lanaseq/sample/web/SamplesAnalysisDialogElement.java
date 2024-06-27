package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.id;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link SamplesAnalysisDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = SamplesAnalysisDialog.ID)
public class SamplesAnalysisDialogElement extends DialogElement {
  public H2Element header() {
    return $(H2Element.class).first();
  }

  public DivElement message() {
    return $(DivElement.class).id(id(MESSAGE));
  }

  public ButtonElement create() {
    return $(ButtonElement.class).id(id(CREATE_FOLDER));
  }

  public ConfirmDialogElement confirm() {
    return $(ConfirmDialogElement.class).id(id(CONFIRM));
  }

  public ConfirmDialogElement errors() {
    return $(ConfirmDialogElement.class).id(id(ERRORS));
  }
}
