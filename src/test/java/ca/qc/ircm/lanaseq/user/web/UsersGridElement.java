package ca.qc.ircm.lanaseq.user.web;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Web element for {@link UsersView} grid.
 */
@Element("vaadin-grid")
@Attribute(name = "id", value = UsersView.USERS)
public class UsersGridElement extends GridElement {

  private static final int EMAIL_COLUMN = 0;

  public void doubleClick(int row) {
    getCell(row, EMAIL_COLUMN).doubleClick();
  }

  public String email(int row) {
    return getCell(row, EMAIL_COLUMN).getText();
  }
}
