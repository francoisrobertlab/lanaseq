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
import com.vaadin.flow.component.checkbox.Checkbox;
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
    dialog.addOpenedChangeListener(e -> {
      if (e.isOpened()) {
        logger.debug("open experiments permissions dialog for experiment {}", experiment);
      }
    });
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
    managers.forEach(user -> dialog.read(user));
    initReads();
  }

  private void initReads() {
    if (dialog == null) {
      return;
    }
    dialog.reads.clear();
    managersDataProvider.getItems().forEach(user -> dialog.read(user));
    if (experiment != null) {
      Set<Long> laboratoriesId = experimentService.permissions(experiment).stream()
          .map(lab -> lab.getId()).collect(Collectors.toSet());
      dialog.reads.entrySet().stream().forEach(entry -> {
        User user = entry.getKey();
        Checkbox read = entry.getValue();
        read.setValue(laboratoriesId.contains(user.getLaboratory().getId()));
        read.setReadOnly(false);
        if (user.getLaboratory().getId().equals(experiment.getOwner().getLaboratory().getId())) {
          read.setValue(true);
          read.setReadOnly(true);
        }
      });
    } else {
      dialog.reads.values().forEach(read -> {
        read.setReadOnly(false);
        read.setValue(false);
      });
    }
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
    if (experiment == null) {
      throw new IllegalStateException("Cannot update permissions without an experiment");
    }
    List<Laboratory> laboratories = dialog.reads.entrySet().stream()
        .filter(entry -> entry.getValue().getValue()).map(entry -> entry.getKey().getLaboratory())
        .filter(lab -> !lab.getId().equals(experiment.getOwner().getLaboratory().getId()))
        .collect(Collectors.toList());
    logger.info("save read for laboratories {} for experiment {}", laboratories, experiment);
    experimentService.savePermissions(experiment, laboratories);
    dialog.close();
  }

  void cancel() {
    dialog.close();
  }

  Experiment getExperiment() {
    return experiment;
  }

  void setExperiment(Experiment experiment) {
    this.experiment = experiment;
    initReads();
  }

  WebUserFilter userFilter() {
    return filter;
  }
}
