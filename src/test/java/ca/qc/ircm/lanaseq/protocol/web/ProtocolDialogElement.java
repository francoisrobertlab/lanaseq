package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.FILES;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_ERROR;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.id;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H3Element;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.testbench.elementsbase.Element;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@Element("vaadin-dialog")
public class ProtocolDialogElement extends DialogElement {
  private static final int FILENAME_COLUMN = 0;

  public H3Element header() {
    return $(H3Element.class).id(id(HEADER));
  }

  public TextFieldElement name() {
    return $(TextFieldElement.class).id(id(NAME));
  }

  public DivElement filesError() {
    return $(DivElement.class).id(id(FILES_ERROR));
  }

  public UploadElement upload() {
    return $(UploadElement.class).id(id(UPLOAD));
  }

  public GridElement files() {
    return $(GridElement.class).id(id(FILES));
  }

  public AnchorElement filename(int row) {
    return files().getCell(row, FILENAME_COLUMN).$(AnchorElement.class).first();
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(id(SAVE));
  }

  public ButtonElement cancel() {
    return $(ButtonElement.class).id(id(CANCEL));
  }
}
