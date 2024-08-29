package ca.qc.ircm.lanaseq.dataset.web;

import ca.qc.ircm.lanaseq.dataset.DatasetProperties;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.html.testbench.NativeLabelElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link DatasetDialog} samples grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = DatasetDialog.ID + "-" + DatasetProperties.SAMPLES)
public class DatasetSamplesGridElement extends GridElement {
  private static final int SAMPLE_ID_COLUMN = 0;
  private static final int REPLICATE_COLUMN = 1;
  private static final int REMOVE_COLUMN = 3;

  public NativeLabelElement sampleId(int row) {
    return getCell(row, SAMPLE_ID_COLUMN).$(NativeLabelElement.class).first();
  }

  public NativeLabelElement replicate(int row) {
    return getCell(row, REPLICATE_COLUMN).$(NativeLabelElement.class).first();
  }

  public ButtonElement remove(int row) {
    return getCell(row, REMOVE_COLUMN).$(ButtonElement.class).first();
  }
}
