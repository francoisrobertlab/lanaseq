package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link KeywordsField}.
 */
@Element("vaadin-multi-select-combo-box")
@Attribute(name = "class", value = KeywordsField.CLASS_NAME)
public class KeywordsFieldElement extends MultiSelectComboBoxElement {

}
