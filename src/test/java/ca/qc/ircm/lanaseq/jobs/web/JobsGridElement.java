package ca.qc.ircm.lanaseq.jobs.web;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link JobsView} grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = JobsView.JOBS)
public class JobsGridElement extends GridElement {

  private static final int TITLE_COLUMN = 0;
  private static final int TIME_COLUMN = 1;
  private static final int PROGRESS_COLUMN = 2;

}
