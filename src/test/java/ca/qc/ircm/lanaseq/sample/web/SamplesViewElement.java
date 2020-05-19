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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-vertical-layout")
public class SamplesViewElement extends VerticalLayoutElement {
  private static final int NAME_COLUMN = 0;
  private static final int PROTOCOL_COLUMN = 1;
  private static final int OWNER_COLUMN = 3;

  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public GridElement samples() {
    return $(GridElement.class).id(SAMPLES);
  }

  public TextFieldElement nameFilter() {
    return samples().getHeaderCell(NAME_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement protocolFilter() {
    return samples().getHeaderCell(PROTOCOL_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement ownerFilter() {
    return samples().getHeaderCell(OWNER_COLUMN).$(TextFieldElement.class).first();
  }

  public void doubleClickSample(int row) {
    samples().getCell(row, NAME_COLUMN).doubleClick();
  }

  public void doubleClickProtocol(int row) {
    samples().getCell(row, PROTOCOL_COLUMN).doubleClick();
  }

  public ButtonElement add() {
    return $(ButtonElement.class).id(ADD);
  }
}
