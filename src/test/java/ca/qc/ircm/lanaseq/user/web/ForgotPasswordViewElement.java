package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.ForgotPasswordView.MESSAGE;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ForgotPasswordView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = ForgotPasswordView.ID)
public class ForgotPasswordViewElement extends VerticalLayoutElement {
  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public DivElement message() {
    return $(DivElement.class).id(MESSAGE);
  }

  public TextFieldElement email() {
    return $(TextFieldElement.class).id(EMAIL);
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(SAVE);
  }
}
