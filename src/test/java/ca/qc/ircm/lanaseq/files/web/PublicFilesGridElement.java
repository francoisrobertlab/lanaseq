package ca.qc.ircm.lanaseq.files.web;

import ca.qc.ircm.lanaseq.web.DateRangeFieldElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link PublicFilesView} grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = PublicFilesView.FILES)
public class PublicFilesGridElement extends GridElement {

  private static final int FILENAME_COLUMN = 0;
  private static final int EXPIRY_DATE_COLUMN = 1;
  private static final int SAMPLE_NAME_COLUMN = 2;
  private static final int OWNER_COLUMN = 3;
  private static final int DELETE_COLUMN = 4;

  public TextFieldElement filenameFilter() {
    return getHeaderCell(FILENAME_COLUMN).$(TextFieldElement.class).first();
  }

  public DateRangeFieldElement expiryDateFilter() {
    return getHeaderCell(EXPIRY_DATE_COLUMN).$(DateRangeFieldElement.class).first();
  }

  public TextFieldElement sampleNameFilter() {
    return getHeaderCell(SAMPLE_NAME_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement ownerFilter() {
    return getHeaderCell(OWNER_COLUMN).$(TextFieldElement.class).first();
  }

  public ButtonElement delete(int row) {
    return getCell(row, DELETE_COLUMN).$(ButtonElement.class).first();
  }
}
