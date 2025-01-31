package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link FilenamesField}.
 */
@Element("vaadin-multi-select-combo-box")
@Attribute(name = "class", value = FilenamesField.CLASS_NAME)
public class FilenamesFieldElement extends MultiSelectComboBoxElement {

}
