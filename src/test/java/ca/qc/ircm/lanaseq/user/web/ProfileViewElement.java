package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.Constants.SAVE;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ProfileView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = ProfileView.ID)
public class ProfileViewElement extends VerticalLayoutElement {

  public UserFormElement form() {
    return $(UserFormElement.class).id(UserForm.ID);
  }

  public ButtonElement save() {
    return $(ButtonElement.class).id(SAVE);
  }
}
