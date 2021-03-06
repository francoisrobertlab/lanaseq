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

import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.DATASETS;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.HEADER;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ROBTOOLS;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-vertical-layout")
public class AnalysisViewElement extends VerticalLayoutElement {
  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public GridElement datasets() {
    return $(GridElement.class).id(DATASETS);
  }

  public AnchorElement robtools() {
    return $(AnchorElement.class).id(ROBTOOLS);
  }

  public AnalysisDialogElement dialog() {
    return $(AnalysisDialogElement.class).id(AnalysisDialog.ID);
  }
}
