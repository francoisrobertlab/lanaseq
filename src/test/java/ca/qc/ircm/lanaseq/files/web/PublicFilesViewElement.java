package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.DOWNLOAD_LINKS;

import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link PublicFilesView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = PublicFilesView.ID)
public class PublicFilesViewElement extends VerticalLayoutElement {

  public PublicFilesGridElement files() {
    return $(PublicFilesGridElement.class).first();
  }

  public AnchorElement downloadLinks() {
    return $(AnchorElement.class).id(DOWNLOAD_LINKS);
  }
}
