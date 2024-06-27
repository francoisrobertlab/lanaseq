package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link TagsField}.
 */
@Element("vaadin-multi-select-combo-box")
@Attribute(name = "class", value = TagsField.CLASS_NAME)
public class TagsFieldElement extends MultiSelectComboBoxElement {
}
