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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import java.util.List;

/**
 * Web element for {@link TagsField}.
 */
@Element("vaadin-custom-field")
@Attribute(name = "class", value = TagsField.CLASS_NAME)
public class TagsFieldElement extends CustomFieldElement {
  public List<ButtonElement> tags() {
    return $(ButtonElement.class).all();
  }

  /**
   * Returns tag element.
   *
   * @param tag
   *          tag
   * @return tag element
   */
  public ButtonElement tag(String tag) {
    for (ButtonElement button : tags()) {
      if (button.getText().equals(tag)) {
        return button;
      }
    }
    return null;
  }

  public ComboBoxElement newTag() {
    return $(ComboBoxElement.class).first();
  }
}
