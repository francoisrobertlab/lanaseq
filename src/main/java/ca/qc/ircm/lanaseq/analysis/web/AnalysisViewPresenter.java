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

import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.DATASETS_REQUIRED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Analysis view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnalysisViewPresenter {
  private AnalysisView view;
  private Locale locale;

  void init(AnalysisView view) {
    this.view = view;
    clearError();
  }

  void localChange(Locale locale) {
    this.locale = locale;
  }

  private void clearError() {
    view.error.setVisible(false);
  }

  void analyze(Dataset dataset) {
    view.dialog.setDataset(dataset);
    view.dialog.open();
  }

  void analyze(Set<Dataset> datasets) {
    clearError();
    if (!datasets.isEmpty()) {
      view.dialog.setDatasets(datasets.stream().collect(Collectors.toList()));
      view.dialog.open();
    } else {
      AppResources resources = new AppResources(AnalysisView.class, locale);
      view.error.setText(resources.message(DATASETS_REQUIRED));
      view.error.setVisible(true);
    }
  }
}
