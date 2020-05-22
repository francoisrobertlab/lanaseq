package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.web.DateRangeField.FROM;
import static ca.qc.ircm.lanaseq.web.DateRangeField.TO;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-custom-field")
public class DateRangeFieldElement extends CustomFieldElement {
  public DatePickerElement form() {
    return $(DatePickerElement.class).attribute("class", FROM).first();
  }

  public DatePickerElement to() {
    return $(DatePickerElement.class).attribute("class", TO).first();
  }
}
