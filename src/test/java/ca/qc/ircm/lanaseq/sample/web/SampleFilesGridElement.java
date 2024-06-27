package ca.qc.ircm.lanaseq.sample.web;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link SampleFilesDialog} files grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = SampleFilesDialog.ID + "-" + SampleFilesDialog.FILES)
public class SampleFilesGridElement extends GridElement {
  private static final int FILE_COLUMN = 0;
  private static final int DOWNLOAD_COLUMN = 1;
  private static final int DELETE_COLUMN = 2;

  public String filename(int row) {
    return getCell(row, FILE_COLUMN).getText();
  }

  public ButtonElement download(int row) {
    return getCell(row, DOWNLOAD_COLUMN).$(ButtonElement.class).first();
  }

  public ButtonElement delete(int row) {
    return getCell(row, DELETE_COLUMN).$(ButtonElement.class).first();
  }
}
