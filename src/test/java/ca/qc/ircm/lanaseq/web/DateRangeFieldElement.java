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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.DateRangeField.FROM;
import static ca.qc.ircm.lanaseq.web.DateRangeField.TO;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link DateRangeField}.
 */
@Element("vaadin-custom-field")
public class DateRangeFieldElement extends CustomFieldElement {
  public DatePickerElement form() {
    return $(DatePickerElement.class).attribute("class", FROM).first();
  }

  public DatePickerElement to() {
    return $(DatePickerElement.class).attribute("class", TO).first();
  }
}
