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

package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.HEADER;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.id;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H3Element;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-dialog")
public class AnalysisDialogElement extends DialogElement {
  public H3Element header() {
    return $(H3Element.class).id(id(HEADER));
  }

  public DivElement message() {
    return $(DivElement.class).id(id(MESSAGE));
  }

  public ButtonElement create() {
    return $(ButtonElement.class).id(id(CREATE_FOLDER));
  }

  public ConfirmDialogElement confirm() {
    return $(ConfirmDialogElement.class).id(id(CONFIRM));
  }

  public ConfirmDialogElement errors() {
    return $(ConfirmDialogElement.class).id(id(ERRORS));
  }
}
