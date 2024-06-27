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

import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.tabs.testbench.TabElement;
import com.vaadin.flow.component.tabs.testbench.TabsElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link ViewLayout}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = ViewLayout.ID)
public class ViewLayoutElement extends VerticalLayoutElement {
  public TabsElement tabs() {
    return $(TabsElement.class).first();
  }

  public TabElement datasets() {
    return $(TabElement.class).id(styleName(DATASETS, TAB));
  }

  public TabElement samples() {
    return $(TabElement.class).id(styleName(SAMPLES, TAB));
  }

  public TabElement protocols() {
    return $(TabElement.class).id(styleName(PROTOCOLS, TAB));
  }

  public TabElement profile() {
    return $(TabElement.class).id(styleName(PROFILE, TAB));
  }

  public TabElement users() {
    return $(TabElement.class).id(styleName(USERS, TAB));
  }

  public TabElement exitSwitchUser() {
    return $(TabElement.class).id(styleName(EXIT_SWITCH_USER, TAB));
  }

  public TabElement signout() {
    return $(TabElement.class).id(styleName(SIGNOUT, TAB));
  }
}
