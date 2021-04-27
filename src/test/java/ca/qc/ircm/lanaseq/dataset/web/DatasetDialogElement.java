/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ADD_NEW_SAMPLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ADD_SAMPLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAMPLES;
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
import ca.qc.ircm.lanaseq.web.TagsFieldElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H3Element;
import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

@Element("vaadin-dialog")
public class DatasetDialogElement extends DialogElement {
  public H3Element header() {
    return $(H3Element.class).id(id(HEADER));
  }

  public TagsFieldElement tags() {
    return $(TagsFieldElement.class).id(id(TAGS));
  }

  public ComboBoxElement protocol() {
    return $(ComboBoxElement.class).id(id(PROTOCOL));
  }

  public SelectElement assay() {
    return $(SelectElement.class).id(id(ASSAY));
  }

  public SelectElement type() {
    return $(SelectElement.class).id(id(TYPE));
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

  public DatePickerElement date() {
    return $(DatePickerElement.class).id(id(DATE));
  }

  public DatasetSamplesGridElement samples() {
    return $(DatasetSamplesGridElement.class).id(id(SAMPLES));
  }

  public ButtonElement addNewSample() {
    return $(ButtonElement.class).id(id(ADD_NEW_SAMPLE));
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
