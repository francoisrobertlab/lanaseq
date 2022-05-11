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

package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.HISTORY;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

/**
 * Web element for {@link ProtocolsView}.
 */
@ServiceTestAnnotations
@Element("vaadin-vertical-layout")
public class ProtocolsViewElement extends VerticalLayoutElement {
  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public ProtocolsGridElement protocols() {
    return $(ProtocolsGridElement.class).id(PROTOCOLS);
  }

  public ButtonElement add() {
    return $(ButtonElement.class).id(ADD);
  }

  public ButtonElement history() {
    return $(ButtonElement.class).id(HISTORY);
  }

  public ProtocolDialogElement dialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(ProtocolDialog.ID)))
        .wrap(ProtocolDialogElement.class);
  }

  public ProtocolHistoryDialogElement historyDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(ProtocolHistoryDialog.ID)))
        .wrap(ProtocolHistoryDialogElement.class);
  }
}
