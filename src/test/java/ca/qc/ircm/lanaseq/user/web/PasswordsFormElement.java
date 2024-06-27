package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.PASSWORD;
import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.PASSWORD_CONFIRM;
import static ca.qc.ircm.lanaseq.user.web.PasswordsForm.id;

import com.vaadin.flow.component.formlayout.testbench.FormLayoutElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link PasswordsForm}.
 */
@Element("vaadin-form-layout")
@Attribute(name = "id", value = PasswordsForm.ID)
public class PasswordsFormElement extends FormLayoutElement {
  public PasswordFieldElement password() {
    return $(PasswordFieldElement.class).id(id(PASSWORD));
  }

  public PasswordFieldElement passwordConfirm() {
    return $(PasswordFieldElement.class).id(id(PASSWORD_CONFIRM));
  }
}
