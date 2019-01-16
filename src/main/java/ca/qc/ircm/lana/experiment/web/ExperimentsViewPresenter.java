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

import static ca.qc.ircm.lana.experiment.web.ExperimentsView.EXPERIMENTS_REQUIRED;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.PERMISSIONS_DENIED;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentService;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.user.UserRole;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Experiments view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExperimentsViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(ExperimentsViewPresenter.class);
  private ExperimentsView view;
  @Inject
  private ExperimentService experimentService;
  @Inject
  private AuthorizationService authorizationService;
  private ListDataProvider<Experiment> experimentsDataProvider;
  private WebExperimentFilter filter = new WebExperimentFilter();
  private Locale locale = Locale.getDefault();

  protected ExperimentsViewPresenter() {
  }

  protected ExperimentsViewPresenter(ExperimentService experimentService,
      AuthorizationService authorizationService) {
    this.experimentService = experimentService;
    this.authorizationService = authorizationService;
  }

  void init(ExperimentsView view) {
    logger.debug("Experiments view");
    this.view = view;
    loadExperiments();
    view.experimentDialog.addSavedListener(e -> loadExperiments());
    if (!authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      view.ownerFilter.setValue(authorizationService.currentUser().getEmail());
    }
    clearError();
  }

  @SuppressWarnings("checkstyle:linelength")
  private void loadExperiments() {
    experimentsDataProvider = new ListDataProvider<>(experimentService.all());
    ConfigurableFilterDataProvider<Experiment, Void, SerializablePredicate<Experiment>> dataProvider =
        experimentsDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    view.experiments.setDataProvider(dataProvider);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  private void clearError() {
    view.error.setVisible(false);
  }

  void view(Experiment experiment) {
    clearError();
    view.experimentDialog.setExperiment(experimentService.get(experiment.getId()));
    view.experimentDialog.open();
  }

  void add() {
    clearError();
    view.experimentDialog.setExperiment(new Experiment());
    view.experimentDialog.open();
  }

  void permissions() {
    clearError();
    Experiment experiment = view.experiments.getSelectedItems().stream().findFirst().orElse(null);
    if (experiment == null) {
      MessageResource resources = new MessageResource(ExperimentsView.class, locale);
      view.error.setText(resources.message(EXPERIMENTS_REQUIRED));
      view.error.setVisible(true);
    } else if (!authorizationService.hasWrite(experiment)) {
      MessageResource resources = new MessageResource(ExperimentsView.class, locale);
      view.error.setText(resources.message(PERMISSIONS_DENIED));
      view.error.setVisible(true);
    } else {
      view.experimentPermissionsDialog.setExperiment(experiment);
      view.experimentPermissionsDialog.open();
    }
  }

  void filterName(String value) {
    clearError();
    filter.nameContains = value.isEmpty() ? null : value;
    view.experiments.getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    clearError();
    filter.ownerContains = value.isEmpty() ? null : value;
    view.experiments.getDataProvider().refreshAll();
  }

  WebExperimentFilter filter() {
    return filter;
  }
}
