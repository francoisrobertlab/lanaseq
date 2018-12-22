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

package ca.qc.ircm.lana.experiment.web;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.inject.Inject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Experiments view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExperimentsViewPresenter {
  private ExperimentsView view;
  @Inject
  private ExperimentService experimentService;

  protected ExperimentsViewPresenter() {
  }

  protected ExperimentsViewPresenter(ExperimentService experimentService) {
    this.experimentService = experimentService;
  }

  void init(ExperimentsView view) {
    this.view = view;
    view.experiments.setItems(experimentService.all());
    view.experimentDialog.addSaveListener(e -> save(e.getSavedObject()));
  }

  void view(Experiment experiment) {
    view.experimentDialog.setExperiment(experimentService.get(experiment.getId()));
    view.experimentDialog.open();
  }

  void add() {
    Experiment experiment = new Experiment();
    view.experimentDialog.setExperiment(experiment);
    view.experimentDialog.open();
  }

  private void save(Experiment experiment) {
    experimentService.save(experiment);
  }
}
