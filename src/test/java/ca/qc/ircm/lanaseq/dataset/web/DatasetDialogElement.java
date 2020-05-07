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
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.ASSAY;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.STRAIN;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TARGET;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TYPE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ADD_SAMPLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAMPLES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAMPLES_HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.id;

import ca.qc.ircm.lanaseq.sample.web.SampleDialog;
import ca.qc.ircm.lanaseq.sample.web.SampleDialogElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.H3Element;
import com.vaadin.flow.component.html.testbench.H4Element;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.WebDriverWait;

@Element("vaadin-dialog")
public class DatasetDialogElement extends DialogElement {
  private static final int NAME_COLUMN = 0;

  public H3Element header() {
    return $(H3Element.class).id(id(HEADER));
  }

  public TextFieldElement name() {
    return $(TextFieldElement.class).id(id(NAME));
  }

  public TextFieldElement project() {
    return $(TextFieldElement.class).id(id(PROJECT));
  }

  public ComboBoxElement protocol() {
    return $(ComboBoxElement.class).id(id(PROTOCOL));
  }

  public ComboBoxElement assay() {
    return $(ComboBoxElement.class).id(id(ASSAY));
  }

  public ComboBoxElement type() {
    return $(ComboBoxElement.class).id(id(TYPE));
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

  public H4Element samplesHeader() {
    return $(H4Element.class).id(id(SAMPLES_HEADER));
  }

  public GridElement samples() {
    return $(GridElement.class).id(id(SAMPLES));
  }

  public void doubleClickSample(int row) {
    samples().getCell(row, NAME_COLUMN).doubleClick();
  }

  public ButtonElement addSample() {
    return $(ButtonElement.class).id(id(ADD_SAMPLE));
  }

  public SampleDialogElement sampleDialog() {
    return new WebDriverWait(getDriver(), 10).until(driver -> {
      try {
        return $(SampleDialogElement.class).id(SampleDialog.ID);
      } catch (NoSuchElementException e) {
        return null;
      }
    });
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(id(SAVE));
  }

  public ButtonElement cancel() {
    return $(ButtonElement.class).id(id(CANCEL));
  }
}
