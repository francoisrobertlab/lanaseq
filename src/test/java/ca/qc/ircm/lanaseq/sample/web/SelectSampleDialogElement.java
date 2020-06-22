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

import static ca.qc.ircm.lanaseq.sample.SampleProperties.SAMPLE_ID;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.id;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-dialog")
public class SelectSampleDialogElement extends DialogElement {
  private static final int NAME_COLUMN = 0;

  public GridElement samples() {
    return $(GridElement.class).id(id(SAMPLE_ID));
  }

  public void doubleClick(int row) {
    samples().getCell(row, NAME_COLUMN).doubleClick();
  }
}
