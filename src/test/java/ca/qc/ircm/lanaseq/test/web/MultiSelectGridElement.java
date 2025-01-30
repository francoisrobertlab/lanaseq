package ca.qc.ircm.lanaseq.test.web;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Patched GridElement for multiple selection.
 */
@Element("vaadin-grid")
public class MultiSelectGridElement extends GridElement {

  @Override
  public void select(int rowIndex) {
    getCell(rowIndex, 0).$(CheckboxElement.class).first().click();
  }
}
