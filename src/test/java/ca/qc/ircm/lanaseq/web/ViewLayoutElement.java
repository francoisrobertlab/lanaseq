package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.ViewLayout.DATASETS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.EXIT_SWITCH_USER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.HEADER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.ID;
import static ca.qc.ircm.lanaseq.web.ViewLayout.LABORATORY;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NAV;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_COUNT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_HEADER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_LIST;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_MARK_AS_READ;
import static ca.qc.ircm.lanaseq.web.ViewLayout.NOTIFICATIONS_POPOVER;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROFILE;
import static ca.qc.ircm.lanaseq.web.ViewLayout.PROTOCOLS;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SAMPLES;
import static ca.qc.ircm.lanaseq.web.ViewLayout.SIGNOUT;
import static ca.qc.ircm.lanaseq.web.ViewLayout.USERS;

import com.vaadin.flow.component.applayout.testbench.AppLayoutElement;
import com.vaadin.flow.component.applayout.testbench.DrawerToggleElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.H1Element;
import com.vaadin.flow.component.html.testbench.H2Element;
import com.vaadin.flow.component.html.testbench.H4Element;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.component.popover.testbench.PopoverElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

/**
 * Web element for {@link ViewLayout}.
 */
@Element("vaadin-app-layout")
@Attribute(name = "id", value = ID)
public class ViewLayoutElement extends AppLayoutElement {

  public H1Element applicationName() {
    return $(H1Element.class).id(styleName(APPLICATION_NAME));
  }

  public H2Element header() {
    return $(H2Element.class).id(styleName(ID, HEADER));
  }

  public H1Element laboratory() {
    return $(H1Element.class).id(styleName(ID, LABORATORY));
  }

  public DrawerToggleElement drawerToggle() {
    return $(DrawerToggleElement.class).first();
  }

  public SideNavElement sideNav() {
    return $(SideNavElement.class).first();
  }

  private void openSideNav() {
    if ("false".equals(drawerToggle().getDomAttribute("aria-expanded"))) {
      drawerToggle().click();
    }
  }

  public SideNavItemElement datasets() {
    openSideNav();
    return $(SideNavItemElement.class).id(styleName(DATASETS, NAV));
  }

  public SideNavItemElement samples() {
    openSideNav();
    return $(SideNavItemElement.class).id(styleName(SAMPLES, NAV));
  }

  public SideNavItemElement protocols() {
    openSideNav();
    return $(SideNavItemElement.class).id(styleName(PROTOCOLS, NAV));
  }

  public SideNavItemElement profile() {
    openSideNav();
    return $(SideNavItemElement.class).id(styleName(PROFILE, NAV));
  }

  public SideNavItemElement users() {
    openSideNav();
    return $(SideNavItemElement.class).id(styleName(USERS, NAV));
  }

  public SideNavItemElement exitSwitchUser() {
    openSideNav();
    return $(SideNavItemElement.class).id(styleName(EXIT_SWITCH_USER, NAV));
  }

  public SideNavItemElement signout() {
    openSideNav();
    return $(SideNavItemElement.class).id(styleName(SIGNOUT, NAV));
  }

  public ButtonElement notifications() {
    openSideNav();
    return $(ButtonElement.class).id(styleName(ID, NOTIFICATIONS));
  }

  public SpanElement notificationsCount() {
    openSideNav();
    return $(SpanElement.class).id(styleName(ID, NOTIFICATIONS_COUNT));
  }

  public PopoverElement notificationsPopover() {
    return ((TestBenchElement) getDriver().findElement(
        By.id(styleName(ID, NOTIFICATIONS_POPOVER)))).wrap(PopoverElement.class);
  }

  public H4Element notificationsHeader() {
    return notificationsPopover().$(H4Element.class).id(styleName(ID, NOTIFICATIONS_HEADER));
  }

  public ButtonElement notificationsMarkAsRead() {
    return notificationsPopover().$(ButtonElement.class)
        .id(styleName(ID, NOTIFICATIONS_MARK_AS_READ));
  }

  public MessageListElement notificationsList() {
    return notificationsPopover().$(MessageListElement.class).id(styleName(ID, NOTIFICATIONS_LIST));
  }
}
