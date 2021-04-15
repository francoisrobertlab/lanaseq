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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-grid")
public class SampleFilesGridElement extends GridElement {
  private static final int DOWNLOAD_COLUMN = 1;
  private static final int DELETE_COLUMN = 2;

  public AnchorElement download(int row) {
    return getCell(row, DOWNLOAD_COLUMN).$(AnchorElement.class).first();
  }

  public ButtonElement downloadButton(int row) {
    return getCell(row, DOWNLOAD_COLUMN).$(ButtonElement.class).first();
  }

  public ButtonElement delete(int row) {
    return getCell(row, DELETE_COLUMN).$(ButtonElement.class).first();
  }
}
