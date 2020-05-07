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

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryService;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserService;
import ca.qc.ircm.lanaseq.user.web.WebUserFilter;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetPermissionsDialogPresenter {
  private static final Logger logger =
      LoggerFactory.getLogger(DatasetPermissionsDialogPresenter.class);
  private DatasetPermissionsDialog dialog;
  @Autowired
  private DatasetService datasetService;
  @Autowired
  private LaboratoryService laboratoryService;
  @Autowired
  private UserService userService;
  private Dataset dataset;
  private ListDataProvider<User> managersDataProvider;
  private WebUserFilter filter = new WebUserFilter();

  protected DatasetPermissionsDialogPresenter() {
  }

  protected DatasetPermissionsDialogPresenter(DatasetService datasetService,
      LaboratoryService laboratoryService, UserService userService) {
    this.datasetService = datasetService;
    this.laboratoryService = laboratoryService;
    this.userService = userService;
  }

  void init(DatasetPermissionsDialog dialog) {
    dialog.addOpenedChangeListener(e -> {
      if (e.isOpened()) {
        logger.debug("open datasets permissions dialog for dataset {}", dataset);
      }
    });
    this.dialog = dialog;
    List<Laboratory> laboratories = laboratoryService.all();
    List<User> managers = laboratories.stream()
        .map(lab -> Optional.ofNullable(userService.manager(lab)).orElse(fakeManager(lab)))
        .collect(Collectors.toList());
    managersDataProvider = new ListDataProvider<>(managers);
    ConfigurableFilterDataProvider<User, Void, SerializablePredicate<User>> dataProvider =
        managersDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    dialog.managers.setDataProvider(dataProvider);
    managers.forEach(user -> dialog.read(user));
    initReads();
  }

  private User fakeManager(Laboratory laboratory) {
    User user = new User();
    user.setLaboratory(laboratory);
    return user;
  }

  private void initReads() {
    if (dialog == null) {
      return;
    }
    dialog.reads.clear();
    managersDataProvider.getItems().forEach(user -> dialog.read(user));
    if (dataset != null) {
      Set<Long> laboratoriesId = datasetService.permissions(dataset).stream()
          .map(lab -> lab.getId()).collect(Collectors.toSet());
      dialog.reads.entrySet().stream().forEach(entry -> {
        User user = entry.getKey();
        Checkbox read = entry.getValue();
        read.setValue(laboratoriesId.contains(user.getLaboratory().getId()));
        read.setReadOnly(false);
        if (user.getLaboratory().getId().equals(dataset.getOwner().getLaboratory().getId())) {
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
    if (dataset == null) {
      throw new IllegalStateException("Cannot update permissions without an dataset");
    }
    List<Laboratory> laboratories = dialog.reads.entrySet().stream()
        .filter(entry -> entry.getValue().getValue()).map(entry -> entry.getKey().getLaboratory())
        .filter(lab -> !lab.getId().equals(dataset.getOwner().getLaboratory().getId()))
        .collect(Collectors.toList());
    logger.info("save read for laboratories {} for dataset {}", laboratories, dataset);
    datasetService.savePermissions(dataset, laboratories);
    dialog.close();
  }

  void cancel() {
    dialog.close();
  }

  Dataset getDataset() {
    return dataset;
  }

  void setDataset(Dataset dataset) {
    this.dataset = dataset;
    initReads();
  }

  WebUserFilter userFilter() {
    return filter;
  }
}