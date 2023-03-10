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
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link DatasetFilesDialog} files grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = DatasetFilesDialog.ID + "-" + DatasetFilesDialog.FILES)
public class DatasetFilesGridElement extends GridElement {
  private static final int FILE_COLUMN = 0;
  private static final int DOWNLOAD_COLUMN = 1;
  private static final int DELETE_COLUMN = 2;

  public String filename(int row) {
    return getCell(row, FILE_COLUMN).getText();
  }

  public ButtonElement download(int row) {
    return getCell(row, DOWNLOAD_COLUMN).$(ButtonElement.class).first();
  }

  public ButtonElement delete(int row) {
    return getCell(row, DELETE_COLUMN).$(ButtonElement.class).first();
  }
}
