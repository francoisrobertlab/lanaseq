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

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.id;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.H3Element;
import com.vaadin.testbench.elementsbase.Element;

@ServiceTestAnnotations
@Element("vaadin-dialog")
public class ProtocolHistoryDialogElement extends DialogElement {
  public H3Element header() {
    return $(H3Element.class).id(id(HEADER));
  }

  public ProtocolHistoryFilesGridElement files() {
    return $(ProtocolHistoryFilesGridElement.class).id(id(FILES));
  }
}
