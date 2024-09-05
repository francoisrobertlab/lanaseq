package ca.qc.ircm.lanaseq.sample.web;

import ca.qc.ircm.lanaseq.test.web.MultiSelectGridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Keys;

/**
 * Web element for {@link SamplesView} grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = SamplesView.SAMPLES)
public class SamplesGridElement extends MultiSelectGridElement {
  private static final int NAME_COLUMN = 1;
  private static final int TAGS_COLUMN = 2;
  private static final int PROTOCOL_COLUMN = 3;
  private static final int OWNER_COLUMN = 5;

  public TextFieldElement nameFilter() {
    return getHeaderCell(NAME_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement tagsFilter() {
    return getHeaderCell(TAGS_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement protocolFilter() {
    return getHeaderCell(PROTOCOL_COLUMN).$(TextFieldElement.class).first();
  }

  public TextFieldElement ownerFilter() {
    return getHeaderCell(OWNER_COLUMN).$(TextFieldElement.class).first();
  }

  public String name(int row) {
    return getCell(row, NAME_COLUMN).getText();
  }

  /**
   * Control click sample.
   *
   * @param row
   *          row index
   */
  public void controlClick(int row) {
    Keys key = Keys.CONTROL;
    if (SystemUtils.IS_OS_MAC_OSX) {
      key = Keys.COMMAND;
    }
    getCell(row, NAME_COLUMN).click(0, 0, key);
  }

  public void doubleClick(int row) {
    getCell(row, NAME_COLUMN).doubleClick();
  }

  public void doubleClickProtocol(int row) {
    getCell(row, PROTOCOL_COLUMN).doubleClick();
  }
}
