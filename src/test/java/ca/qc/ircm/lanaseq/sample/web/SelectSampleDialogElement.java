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

import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.SAMPLES;
import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.id;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link SelectSampleDialog}.
 */
@Element("vaadin-dialog")
public class SelectSampleDialogElement extends DialogElement {
  public SelectSampleGridElement samples() {
    return $(SelectSampleGridElement.class).id(id(SAMPLES));
  }
}
