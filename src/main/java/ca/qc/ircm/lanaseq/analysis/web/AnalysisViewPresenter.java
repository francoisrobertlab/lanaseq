package ca.qc.ircm.lanaseq.analysis.web;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Analysis view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnalysisViewPresenter {
  private AnalysisView view;

  void init(AnalysisView view) {
    this.view = view;
  }

  void localChange(Locale locale) {
  }

  void view(Dataset dataset) {
    view.dialog.setDataset(dataset);
    view.dialog.open();
  }
}
