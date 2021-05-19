/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.CREATE_FOLDER_EXCEPTION;
import static ca.qc.ircm.lanaseq.text.Strings.property;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Analysis dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnalysisDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(AnalysisDialogPresenter.class);
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
    dialog.confirm.addConfirmListener(e -> dialog.close());
  }

  void localChange(Locale locale) {
    this.locale = locale;
  }

  boolean validate() {
    List<String> errors = new ArrayList<>();
    service.validate(dataset, locale, error -> errors.add(error));
    if (!errors.isEmpty()) {
      dialog.errorsLayout.removeAll();
      errors.forEach(error -> dialog.errorsLayout.add(new Span(error)));
      dialog.errors.open();
    }
    dialog.createFolder.setEnabled(errors.isEmpty());
    return errors.isEmpty();
  }

  void createFolder() {
    if (validate()) {
      logger.debug("creating analysis folder for dataset {}", dataset);
      AppResources resources = new AppResources(AnalysisDialog.class, locale);
      try {
        service.copyResources(dataset);
        boolean unix = dialog.getUI().map(ui -> {
          WebBrowser browser = ui.getSession().getBrowser();
          return browser.isMacOSX() || browser.isLinux();
        }).orElse(false);
        String folder = configuration.analysisLabel(dataset, unix);
        String network = configuration.folderNetwork(unix);
        dialog.confirmLayout.removeAll();
        dialog.confirmLayout.add(new Span(resources.message(property(CONFIRM, "message"), folder)));
        if (network != null) {
          dialog.confirmLayout
              .add(new Span(resources.message(property(CONFIRM, "network"), network)));
        }
        dialog.confirm.open();
      } catch (IOException e) {
        dialog.errorsLayout.removeAll();
        dialog.errorsLayout
            .add(new Span(resources.message(CREATE_FOLDER_EXCEPTION, dataset.getName())));
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
