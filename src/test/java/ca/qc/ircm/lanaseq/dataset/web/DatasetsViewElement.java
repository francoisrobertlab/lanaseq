package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ANALYZE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

/**
 * Web element for {@link DatasetsView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = DatasetsView.ID)
public class DatasetsViewElement extends VerticalLayoutElement {

  public DatasetGridElement datasets() {
    return $(DatasetGridElement.class).first();
  }

  public ButtonElement edit() {
    return $(ButtonElement.class).id(EDIT);
  }

  public ButtonElement merge() {
    return $(ButtonElement.class).id(MERGE);
  }

  public ButtonElement files() {
    return $(ButtonElement.class).id(FILES);
  }

  public ButtonElement analyze() {
    return $(ButtonElement.class).id(ANALYZE);
  }

  public DatasetDialogElement dialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(DatasetDialog.ID)))
        .wrap(DatasetDialogElement.class);
  }

  public DatasetFilesDialogElement filesDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(DatasetFilesDialog.ID)))
        .wrap(DatasetFilesDialogElement.class);
  }

  public DatasetsAnalysisDialogElement analyzeDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(DatasetsAnalysisDialog.ID)))
        .wrap(DatasetsAnalysisDialogElement.class);
  }
}
