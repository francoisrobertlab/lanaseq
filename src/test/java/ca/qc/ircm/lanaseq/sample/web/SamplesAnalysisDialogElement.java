package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.FILENAME_PATTERNS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.id;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

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

  public MultiSelectComboBoxElement filenamePatterns() {
    return $(MultiSelectComboBoxElement.class).id(id(FILENAME_PATTERNS));
  }

  public ButtonElement create() {
    return $(ButtonElement.class).id(id(CREATE_FOLDER));
  }

  public ConfirmDialogElement confirm() {
    return ((TestBenchElement) getDriver().findElement(By.id(id(CONFIRM)))).wrap(
        ConfirmDialogElement.class);
  }

  public ConfirmDialogElement errors() {
    return ((TestBenchElement) getDriver().findElement(By.id(id(ERRORS)))).wrap(
        ConfirmDialogElement.class);
  }
}
