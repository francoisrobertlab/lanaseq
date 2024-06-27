package ca.qc.ircm.lanaseq.security.web;

import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.HEADER;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.HOME;
import static ca.qc.ircm.lanaseq.security.web.AccessDeniedView.MESSAGE;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link AccessDeniedView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = AccessDeniedView.VIEW_NAME)
public class AccessDeniedViewElement extends VerticalLayoutElement {
  public H2Element header() {
    return $(H2Element.class).id(HEADER);
  }

  public DivElement message() {
    return $(DivElement.class).id(MESSAGE);
  }

  public ButtonElement home() {
    return $(ButtonElement.class).id(HOME);
  }
}
