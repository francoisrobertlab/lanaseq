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

package ca.qc.ircm.lanaseq.user.web;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link UsersView} grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = UsersView.USERS)
public class UsersGridElement extends GridElement {
  private static final int EMAIL_COLUMN = 0;
  private static final int EDIT_COLUMN = 3;

  public void doubleClick(int row) {
    getCell(row, EMAIL_COLUMN).doubleClick();
  }

  public String email(int row) {
    return getCell(row, EMAIL_COLUMN).getText();
  }

  public ButtonElement edit(int row) {
    return getCell(row, EDIT_COLUMN).$(ButtonElement.class).first();
  }
}
