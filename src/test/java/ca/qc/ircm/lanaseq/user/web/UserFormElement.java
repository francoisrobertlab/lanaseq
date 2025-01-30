package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.UserProperties.ADMIN;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static ca.qc.ircm.lanaseq.user.UserProperties.NAME;
import static ca.qc.ircm.lanaseq.user.web.UserForm.id;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.formlayout.testbench.FormLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link UserForm}.
 */
@Element("vaadin-form-layout")
@Attribute(name = "id", value = UserForm.ID)
public class UserFormElement extends FormLayoutElement {

  public TextFieldElement email() {
    return $(TextFieldElement.class).id(id(EMAIL));
  }

  public TextFieldElement name() {
    return $(TextFieldElement.class).id(id(NAME));
  }

  public CheckboxElement admin() {
    return $(CheckboxElement.class).id(id(ADMIN));
  }

  public CheckboxElement manager() {
    return $(CheckboxElement.class).id(id(MANAGER));
  }

  public PasswordsFormElement passwords() {
    return $(PasswordsFormElement.class).id(PasswordsForm.ID);
  }
}
