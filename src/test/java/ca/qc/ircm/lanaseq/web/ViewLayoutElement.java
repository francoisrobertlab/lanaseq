package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROFILE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROTOCOLS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SAMPLES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIGNOUT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.TAB;
import static ca.qc.ircm.lanaseq.web.ViewLayout.USERS;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.flow.component.applayout.testbench.DrawerToggleElement;
import com.vaadin.flow.component.html.testbench.H1Element;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.sidenav.testbench.SideNavElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ViewLayout}.
 */
@Element("vaadin-app-layout")
@Attribute(name = "id", value = ViewLayout.ID)
public class ViewLayoutElement extends AppLayoutElement {
  public H1Element applicationName() {
    return $(H1Element.class).first();
  }

  public H2Element header() {
    return $(H2Element.class).first();
  }

  public H2Element laboratory() {
    return $(H2Element.class).first();
  }

  public DrawerToggleElement drawerToggle() {
    return $(DrawerToggleElement.class).first();
  }

  public SideNavElement sideNav() {
    return $(SideNavElement.class).first();
  }

  public SideNavItemElement datasets() {
    return $(SideNavItemElement.class).id(styleName(DATASETS, TAB));
  }

  public SideNavItemElement samples() {
    return $(SideNavItemElement.class).id(styleName(SAMPLES, TAB));
  }

  public SideNavItemElement protocols() {
    return $(SideNavItemElement.class).id(styleName(PROTOCOLS, TAB));
  }

  public SideNavItemElement profile() {
    return $(SideNavItemElement.class).id(styleName(PROFILE, TAB));
  }

  public SideNavItemElement users() {
    return $(SideNavItemElement.class).id(styleName(USERS, TAB));
  }

  public SideNavItemElement exitSwitchUser() {
    return $(SideNavItemElement.class).id(styleName(EXIT_SWITCH_USER, TAB));
  }

  public SideNavItemElement signout() {
    return $(SideNavItemElement.class).id(styleName(SIGNOUT, TAB));
  }
}
