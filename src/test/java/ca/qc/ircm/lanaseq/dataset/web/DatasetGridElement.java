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

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.elementsbase.Element;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Keys;

/**
 * Web element for {@link DatasetGrid}.
 */
@Element("vaadin-grid")
public class DatasetGridElement extends GridElement {
  private static final int NAME_COLUMN = 0;
  private static final int PROTOCOL_COLUMN = 2;
  private static final int OWNER_COLUMN = 4;

  private int column(int column) {
    return isMultiSelect() ? column + 1 : column;
  }

  public TextFieldElement nameFilter() {
    return getHeaderCell(column(NAME_COLUMN)).$(TextFieldElement.class).first();
  }

  public TextFieldElement protocolFilter() {
    return getHeaderCell(column(PROTOCOL_COLUMN)).$(TextFieldElement.class).first();
  }

  public TextFieldElement ownerFilter() {
    return getHeaderCell(column(OWNER_COLUMN)).$(TextFieldElement.class).first();
  }

  /**
   * Control click dataset.
   *
   * @param row
   *          row index
   */
  public void controlClick(int row) {
    Keys key = Keys.CONTROL;
    if (SystemUtils.IS_OS_MAC_OSX) {
      key = Keys.COMMAND;
    }
    getCell(row, column(NAME_COLUMN)).click(0, 0, key);
  }

  public void doubleClick(int row) {
    getCell(row, column(NAME_COLUMN)).doubleClick();
  }

  public void doubleClickProtocol(int row) {
    getCell(row, column(PROTOCOL_COLUMN)).doubleClick();
  }

  @Override
  public void select(int rowIndex) {
    if (isMultiSelect()) {
      getCell(rowIndex, 0).$(CheckboxElement.class).first().click();
    } else {
      super.select(rowIndex);
    }
  }

  private boolean isMultiSelect() {
    return getCell(0, 0).$(CheckboxElement.class).exists();
  }
}
