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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.REPLICATE;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.id;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.H3Element;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-dialog")
public class SampleDialogElement extends DialogElement {
  public H3Element header() {
    return $(H3Element.class).id(id(HEADER));
  }

  public TextFieldElement name() {
    return $(TextFieldElement.class).id(id(NAME));
  }

  public TextFieldElement replicate() {
    return $(TextFieldElement.class).id(id(REPLICATE));
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(id(SAVE));
  }

  public ButtonElement cancel() {
    return $(ButtonElement.class).id(id(CANCEL));
  }
}