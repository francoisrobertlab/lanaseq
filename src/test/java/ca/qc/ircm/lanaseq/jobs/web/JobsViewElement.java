package ca.qc.ircm.lanaseq.jobs.web;

import static ca.qc.ircm.lanaseq.jobs.web.JobsView.REFRESH;
import static ca.qc.ircm.lanaseq.jobs.web.JobsView.REMOVE_DONE;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link JobsView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = JobsView.ID)
public class JobsViewElement extends VerticalLayoutElement {

  public JobsGridElement jobs() {
    return $(JobsGridElement.class).first();
  }

  public ButtonElement refresh() {
    return $(ButtonElement.class).id(REFRESH);
  }

  public ButtonElement removeDone() {
    return $(ButtonElement.class).id(REMOVE_DONE);
  }
}
