package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NOTE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_ERROR;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.id;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ProtocolDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = ProtocolDialog.ID)
public class ProtocolDialogElement extends DialogElement {

  public H2Element header() {
    return $(H2Element.class).first();
  }

  public TextFieldElement name() {
    return $(TextFieldElement.class).id(id(NAME));
  }

  public TextAreaElement note() {
    return $(TextAreaElement.class).id(id(NOTE));
  }

  public DivElement filesError() {
    return $(DivElement.class).id(id(FILES_ERROR));
  }

  public UploadElement upload() {
    return $(UploadElement.class).id(id(UPLOAD));
  }

  public ProtocolFilesGridElement files() {
    return $(ProtocolFilesGridElement.class).first();
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(id(SAVE));
  }

  public ButtonElement cancel() {
    return $(ButtonElement.class).id(id(CANCEL));
  }

  public ButtonElement delete() {
    return $(ButtonElement.class).id(id(DELETE));
  }

  public ConfirmDialogElement confirm() {
    return $(ConfirmDialogElement.class).id(id(CONFIRM));
  }
}
