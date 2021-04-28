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

import ca.qc.ircm.lanaseq.test.web.MultiSelectGridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.elementsbase.Element;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Keys;

/**
 * Web element for {@link SamplesView} grid.
 */
@Element("vaadin-grid")
public class SamplesGridElement extends MultiSelectGridElement {
  private static final int NAME_COLUMN = 1;
  private static final int PROTOCOL_COLUMN = 2;
  private static final int OWNER_COLUMN = 4;

  public TextFieldElement nameFilter() {
    return getHeaderCell(NAME_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement protocolFilter() {
    return getHeaderCell(PROTOCOL_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement ownerFilter() {
    return getHeaderCell(OWNER_COLUMN).$(TextFieldElement.class).first();
  }

  public String name(int row) {
    return getCell(row, NAME_COLUMN).getText();
  }

  /**
   * Control click sample.
   *
   * @param row
   *          row index
   */
  public void controlClick(int row) {
    Keys key = Keys.CONTROL;
    if (SystemUtils.IS_OS_MAC_OSX) {
      key = Keys.COMMAND;
    }
    getCell(row, NAME_COLUMN).click(0, 0, key);
  }

  public void doubleClick(int row) {
    getCell(row, NAME_COLUMN).doubleClick();
  }

  public void doubleClickProtocol(int row) {
    getCell(row, PROTOCOL_COLUMN).doubleClick();
  }
}
