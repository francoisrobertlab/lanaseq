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

import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.SAMPLES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ADD_LARGE_FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FOLDERS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.id;

import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialogElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import java.util.List;
import org.openqa.selenium.By;

/**
 * Web element for {@link DatasetFilesDialog}.
 */
@Element("vaadin-dialog")
@Attribute(name = "id", value = DatasetFilesDialog.ID)
public class DatasetFilesDialogElement extends DialogElement {
  public H2Element header() {
    return $(H2Element.class).first();
  }

  public DivElement message() {
    return $(DivElement.class).id(id(MESSAGE));
  }

  public FoldersElement folders() {
    return $(FoldersElement.class).first();
  }

  public DatasetFilesGridElement files() {
    return $(DatasetFilesGridElement.class).first();
  }

  public GridElement samples() {
    return $(GridElement.class).id(id(SAMPLES));
  }

  public TextFieldElement filenameEdit() {
    return $(TextFieldElement.class).id(id(FILENAME));
  }

  public UploadElement upload() {
    return $(UploadElement.class).id(id(UPLOAD));
  }

  public ButtonElement addLargeFiles() {
    return $(ButtonElement.class).id(id(ADD_LARGE_FILES));
  }

  public AddDatasetFilesDialogElement addFilesDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(AddDatasetFilesDialog.ID)))
        .wrap(AddDatasetFilesDialogElement.class);
  }

  public SampleFilesDialogElement sampleFilesDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(SampleFilesDialog.ID)))
        .wrap(SampleFilesDialogElement.class);
  }

  @Attribute(name = "id", value = DatasetFilesDialog.ID + "-" + FOLDERS)
  public static class FoldersElement extends VerticalLayoutElement {
    public List<SpanElement> labels() {
      return $(SpanElement.class).all();
    }
  }
}
