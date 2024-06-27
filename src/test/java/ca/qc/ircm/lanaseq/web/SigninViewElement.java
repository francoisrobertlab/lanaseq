package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link SigninView}.
 */
@Element("vaadin-login-overlay")
@Attribute(name = "id", value = SigninView.ID)
public class SigninViewElement extends LoginOverlayElement {
}
