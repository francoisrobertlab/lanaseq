package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.user.web.PasswordView.HEADER;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link PasswordView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = PasswordView.ID)
public class PasswordViewElement extends VerticalLayoutElement {
  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public PasswordsFormElement passwords() {
    return $(PasswordsFormElement.class).id(PasswordsForm.ID);
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(SAVE);
  }
}
