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

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.user.web.UsersView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_FAILED;
import static ca.qc.ircm.lanaseq.user.web.UsersView.SWITCH_USER;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

@Element("vaadin-vertical-layout")
public class UsersViewElement extends VerticalLayoutElement {
  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public UsersGridElement users() {
    return $(UsersGridElement.class).first();
  }

  public DivElement switchFailed() {
    return $(DivElement.class).id(SWITCH_FAILED);
  }

  public ButtonElement add() {
    return $(ButtonElement.class).id(ADD);
  }

  public ButtonElement switchUser() {
    return $(ButtonElement.class).id(SWITCH_USER);
  }

  public UserDialogElement dialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(UserDialog.ID)))
        .wrap(UserDialogElement.class);
  }
}
