package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.elementsbase.Element;
import java.util.List;

@Element("vaadin-custom-field")
public class TagsFieldElement extends CustomFieldElement {
  public List<ButtonElement> tags() {
    return $(ButtonElement.class).all();
  }

  public ButtonElement tag(String tag) {
    for (ButtonElement button : tags()) {
      if (button.getText().equals(tag)) {
        return button;
      }
    }
    return null;
  }

  public TextFieldElement newTag() {
    return $(TextFieldElement.class).first();
  }
}
