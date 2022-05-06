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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link DatasetDialog} samples grid.
 */
@Element("vaadin-grid")
public class DatasetSamplesGridElement extends GridElement {
  private static final int SAMPLE_ID_COLUMN = 0;
  private static final int REPLICATE_COLUMN = 1;
  private static final int REMOVE_COLUMN = 3;

  public LabelElement sampleId(int row) {
    return getCell(row, SAMPLE_ID_COLUMN).$(LabelElement.class).first();
  }

  public LabelElement replicate(int row) {
    return getCell(row, REPLICATE_COLUMN).$(LabelElement.class).first();
  }

  public ButtonElement remove(int row) {
    return getCell(row, REMOVE_COLUMN).$(ButtonElement.class).first();
  }
}
