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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.CREATE_FOLDER_EXCEPTION;
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
import java.util.Collection;
import java.util.Collections;
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
public class DatasetsAnalysisDialogPresenter {
  private static final Logger logger =
      LoggerFactory.getLogger(DatasetsAnalysisDialogPresenter.class);
  private DatasetsAnalysisDialog dialog;
  private Collection<Dataset> datasets;
  private Locale locale;
  private AnalysisService service;
  private AppConfiguration configuration;

  @Autowired
  protected DatasetsAnalysisDialogPresenter(AnalysisService service,
      AppConfiguration configuration) {
    this.service = service;
    this.configuration = configuration;
  }

  void init(DatasetsAnalysisDialog dialog) {
    this.dialog = dialog;
    dialog.addOpenedChangeListener(e -> {
      if (e.isOpened() && datasets != null) {
        validate();
      }
    });
  }

  void localChange(Locale locale) {
    this.locale = locale;
  }

  boolean validate() {
    List<String> errors = new ArrayList<>();
    service.validateDatasets(datasets, locale, error -> errors.add(error));
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
      logger.debug("creating analysis folder for datasets {}", datasets);
      AppResources resources = new AppResources(DatasetsAnalysisDialog.class, locale);
      try {
        service.copyDatasetsResources(datasets);
        boolean unix = dialog.getUI().map(ui -> {
          WebBrowser browser = ui.getSession().getBrowser();
          return browser.isMacOSX() || browser.isLinux();
        }).orElse(false);
        String folder = configuration.datasetAnalysisLabel(datasets, unix);
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
        dialog.errorsLayout.add(new Span(resources.message(CREATE_FOLDER_EXCEPTION)));
        dialog.errors.open();
      } catch (IllegalArgumentException e) {
        // re-validate, something changed.
        validate();
      }
    }
  }

  Collection<Dataset> getDatasets() {
    return datasets;
  }

  void setDataset(Dataset dataset) {
    this.datasets = Collections.nCopies(1, dataset);
  }

  void setDatasets(List<Dataset> datasets) {
    this.datasets = datasets;
  }
}
