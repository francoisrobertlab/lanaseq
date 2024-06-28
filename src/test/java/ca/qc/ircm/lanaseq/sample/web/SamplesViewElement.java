package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ANALYZE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGE;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.annotations.Attribute;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

/**
 * Web element for {@link SamplesView}.
 */
@Element("vaadin-vertical-layout")
@Attribute(name = "id", value = SamplesView.ID)
public class SamplesViewElement extends VerticalLayoutElement {
  public SamplesGridElement samples() {
    return $(SamplesGridElement.class).first();
  }

  public DivElement error() {
    return $(DivElement.class).id(ERROR_TEXT);
  }

  public ButtonElement add() {
    return $(ButtonElement.class).id(ADD);
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

  public SampleDialogElement dialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(SampleDialog.ID)))
        .wrap(SampleDialogElement.class);
  }

  public SampleFilesDialogElement filesDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(SampleFilesDialog.ID)))
        .wrap(SampleFilesDialogElement.class);
  }

  public SamplesAnalysisDialogElement analyzeDialog() {
    return ((TestBenchElement) getDriver().findElement(By.id(SamplesAnalysisDialog.ID)))
        .wrap(SamplesAnalysisDialogElement.class);
  }
}
