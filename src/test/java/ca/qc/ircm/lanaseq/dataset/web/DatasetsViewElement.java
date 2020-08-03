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

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE;

import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialogElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.elementsbase.Element;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Keys;

@Element("vaadin-vertical-layout")
public class DatasetsViewElement extends VerticalLayoutElement {
  private static final int NAME_COLUMN = 1;
  private static final int PROTOCOL_COLUMN = 3;
  private static final int OWNER_COLUMN = 5;

  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public GridElement datasets() {
    return $(GridElement.class).id(DATASETS);
  }

  public TextFieldElement nameFilter() {
    return datasets().getHeaderCell(NAME_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement protocolFilter() {
    return datasets().getHeaderCell(PROTOCOL_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement ownerFilter() {
    return datasets().getHeaderCell(OWNER_COLUMN).$(TextFieldElement.class).first();
  }

  public void controlClick(int row) {
    Keys key = Keys.CONTROL;
    if (SystemUtils.IS_OS_MAC_OSX) {
      key = Keys.COMMAND;
    }
    datasets().getCell(row, NAME_COLUMN).click(0, 0, key);
  }

  public void doubleClick(int row) {
    datasets().getCell(row, NAME_COLUMN).doubleClick();
  }

  public void doubleClickProtocol(int row) {
    datasets().getCell(row, PROTOCOL_COLUMN).doubleClick();
  }

  public DivElement error() {
    return $(DivElement.class).id(ERROR_TEXT);
  }

  public ButtonElement add() {
    return $(ButtonElement.class).id(ADD);
  }

  public ButtonElement merge() {
    return $(ButtonElement.class).id(MERGE);
  }

  public ButtonElement files() {
    return $(ButtonElement.class).id(FILES);
  }

  public DatasetDialogElement dialog() {
    return $(DatasetDialogElement.class).id(DatasetDialog.ID);
  }

  public DatasetFilesDialogElement filesDialog() {
    return $(DatasetFilesDialogElement.class).id(DatasetFilesDialog.ID);
  }

  public ProtocolDialogElement protocolDialog() {
    return $(ProtocolDialogElement.class).id(ProtocolDialog.ID);
  }
}
