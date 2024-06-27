package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.HEADER;
import static ca.qc.ircm.lanaseq.user.web.UseForgotPasswordView.MESSAGE;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link UseForgotPasswordView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = UseForgotPasswordView.ID)
public class UseForgotPasswordViewElement extends VerticalLayoutElement {
  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public DivElement message() {
    return $(DivElement.class).id(MESSAGE);
  }

  public PasswordsFormElement passwordsForm() {
    return $(PasswordsFormElement.class).id(PasswordsForm.ID);
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(SAVE);
  }
}
