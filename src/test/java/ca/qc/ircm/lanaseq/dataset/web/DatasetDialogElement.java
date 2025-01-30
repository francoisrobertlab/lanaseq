package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.FILENAMES;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.KEYWORDS;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NOTE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ADD_SAMPLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.GENERATE_NAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.NAME_PREFIX;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAMPLES_HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.id;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;

import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialogElement;
import ca.qc.ircm.lanaseq.web.FilenamesFieldElement;
import ca.qc.ircm.lanaseq.web.KeywordsFieldElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.html.testbench.H4Element;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

/**
 * Web element for {@link DatasetDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = DatasetDialog.ID)
public class DatasetDialogElement extends DialogElement {

  public H2Element header() {
    return $(H2Element.class).first();
  }

  public TextFieldElement namePrefix() {
    return $(TextFieldElement.class).id(id(NAME_PREFIX));
  }

  public ButtonElement generateName() {
    return $(ButtonElement.class).id(id(GENERATE_NAME));
  }

  public KeywordsFieldElement keywords() {
    return $(KeywordsFieldElement.class).id(id(KEYWORDS));
  }

  public FilenamesFieldElement filenames() {
    return $(FilenamesFieldElement.class).id(id(FILENAMES));
  }

  public TextFieldElement protocol() {
    return $(TextFieldElement.class).id(id(PROTOCOL));
  }

  public TextFieldElement assay() {
    return $(TextFieldElement.class).id(id(ASSAY));
  }

  public TextFieldElement type() {
    return $(TextFieldElement.class).id(id(TYPE));
  }

  public TextFieldElement target() {
    return $(TextFieldElement.class).id(id(TARGET));
  }

  public TextFieldElement strain() {
    return $(TextFieldElement.class).id(id(STRAIN));
  }

  public TextFieldElement strainDescription() {
    return $(TextFieldElement.class).id(id(STRAIN_DESCRIPTION));
  }

  public TextFieldElement treatment() {
    return $(TextFieldElement.class).id(id(TREATMENT));
  }

  public TextAreaElement note() {
    return $(TextAreaElement.class).id(id(NOTE));
  }

  public DatePickerElement date() {
    return $(DatePickerElement.class).id(id(DATE));
  }

  public H4Element samplesHeader() {
    return $(H4Element.class).id(id(SAMPLES_HEADER));
  }

  public DatasetSamplesGridElement samples() {
    return $(DatasetSamplesGridElement.class).first();
  }

  public ButtonElement addSample() {
    return $(ButtonElement.class).id(id(ADD_SAMPLE));
  }

  public DivElement error() {
    return $(DivElement.class).id(id(ERROR_TEXT));
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

  public SelectSampleDialogElement selectSampleDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(SelectSampleDialog.ID)))
        .wrap(SelectSampleDialogElement.class);
  }
}
