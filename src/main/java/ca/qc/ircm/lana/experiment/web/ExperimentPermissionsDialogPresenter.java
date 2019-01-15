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
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserService;
import ca.qc.ircm.lana.user.web.WebUserFilter;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Experiment dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExperimentPermissionsDialogPresenter {
  private static final Logger logger =
      LoggerFactory.getLogger(ExperimentPermissionsDialogPresenter.class);
  private ExperimentPermissionsDialog dialog;
  @Inject
  private ExperimentService experimentService;
  @Inject
  private UserService userService;
  private Experiment experiment;
  private ListDataProvider<User> managersDataProvider;
  private WebUserFilter filter = new WebUserFilter();

  protected ExperimentPermissionsDialogPresenter() {
  }

  protected ExperimentPermissionsDialogPresenter(ExperimentService experimentService,
      UserService userService) {
    this.experimentService = experimentService;
    this.userService = userService;
  }

  void init(ExperimentPermissionsDialog dialog) {
    logger.debug("open experiments permissions dialog for experiment {}", experiment);
    this.dialog = dialog;
    List<User> managers = userService.managers();
    Set<Laboratory> laboratories = new HashSet<>();
    managers = managers.stream().filter(user -> laboratories.add(user.getLaboratory()))
        .collect(Collectors.toList());
    managersDataProvider = new ListDataProvider<>(managers);
    ConfigurableFilterDataProvider<User, Void, SerializablePredicate<User>> dataProvider =
        managersDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    dialog.managers.setDataProvider(dataProvider);
  }

  void filterLaboratory(String value) {
    filter.laboratoryNameContains = value.isEmpty() ? null : value;
    dialog.managers.getDataProvider().refreshAll();
  }

  void filterEmail(String value) {
    filter.emailContains = value.isEmpty() ? null : value;
    dialog.managers.getDataProvider().refreshAll();
  }

  void save() {
    // TODO Auto-generated method stub
    //experimentService.savePermissions(experiments, permissions);
  }

  void cancel() {
    dialog.close();
  }

  Experiment getExperiment() {
    return experiment;
  }

  void setExperiment(Experiment experiment) {
    this.experiment = experiment;
  }

  WebUserFilter userFilter() {
    return filter;
  }
}
