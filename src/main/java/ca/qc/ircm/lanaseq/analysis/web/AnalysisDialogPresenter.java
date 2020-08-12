package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.CREATE_FOLDER_EXCEPTION;
import static ca.qc.ircm.lanaseq.text.Strings.property;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Analysis dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnalysisDialogPresenter {
  private AnalysisDialog dialog;
  private Dataset dataset;
  private Locale locale;
  private AnalysisService service;
  private AppConfiguration configuration;

  @Autowired
  protected AnalysisDialogPresenter(AnalysisService service, AppConfiguration configuration) {
    this.service = service;
    this.configuration = configuration;
  }

  void init(AnalysisDialog dialog) {
    this.dialog = dialog;
    dialog.addOpenedChangeListener(e -> {
      if (e.isOpened() && dataset != null) {
        validate();
      }
    });
  }

  void localChange(Locale locale) {
    this.locale = locale;
  }

  boolean validate() {
    List<String> errors = new ArrayList<>();
    service.validate(dataset, locale, error -> errors.add(error));
    if (!errors.isEmpty()) {
      dialog.errorsLayout.removeAll();
      errors.forEach(error -> dialog.errorsLayout.add(error));
      dialog.errors.open();
    }
    return errors.isEmpty();
  }

  void createFolder() {
    if (validate()) {
      AppResources resources = new AppResources(AnalysisDialog.class, locale);
      try {
        service.copyResources(dataset);
        boolean unix = dialog.getUI().map(ui -> {
          WebBrowser browser = ui.getSession().getBrowser();
          return browser.isMacOSX() || browser.isLinux();
        }).orElse(false);
        String folder = configuration.analysisLabel(dataset, unix);
        String network = configuration.folderNetwork(unix);
        dialog.confirmLayout.add(resources.message(property(CONFIRM, "message"), folder));
        if (network != null) {
          dialog.confirmLayout.add(resources.message(property(CONFIRM, "network"), network));
        }
        dialog.confirm.open();
        dialog.close();
      } catch (IOException e) {
        dialog.errorsLayout.removeAll();
        dialog.errorsLayout.add(resources.message(CREATE_FOLDER_EXCEPTION, dataset.getName()));
        dialog.errors.open();
      } catch (IllegalArgumentException e) {
        // re-validate, something changed.
        validate();
      }
    }
  }

  Dataset getDataset() {
    return dataset;
  }

  void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }
}
