package ca.qc.ircm.lanaseq.sample.web;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link SelectSampleDialog} grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = SelectSampleDialog.ID + "-" + SelectSampleDialog.SAMPLES)
public class SelectSampleGridElement extends GridElement {

  private static final int NAME_COLUMN = 0;
  private static final int OWNER_COLUMN = 2;

  public TextFieldElement nameFilter() {
    return getHeaderCell(NAME_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement ownerFilter() {
    return getHeaderCell(OWNER_COLUMN).$(TextFieldElement.class).first();
  }

  public String name(int row) {
    return getCell(row, NAME_COLUMN).getText();
  }

  public void doubleClick(int row) {
    getCell(row, NAME_COLUMN).doubleClick();
  }
}
