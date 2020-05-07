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

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS_REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.PERMISSIONS_DENIED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.acls.domain.BasePermission;

/**
 * Datasets view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetsViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(DatasetsViewPresenter.class);
  private DatasetsView view;
  @Autowired
  private DatasetService service;
  @Autowired
  private ProtocolService protocolService;
  @Autowired
  private AuthorizationService authorizationService;
  private ListDataProvider<Dataset> datasetsDataProvider;
  private WebDatasetFilter filter = new WebDatasetFilter();
  private Locale locale = Locale.getDefault();

  protected DatasetsViewPresenter() {
  }

  protected DatasetsViewPresenter(DatasetService service, ProtocolService protocolService,
      AuthorizationService authorizationService) {
    this.service = service;
    this.protocolService = protocolService;
    this.authorizationService = authorizationService;
  }

  void init(DatasetsView view) {
    logger.debug("Datasets view");
    this.view = view;
    loadDatasets();
    view.datasetDialog.addSavedListener(e -> loadDatasets());
    view.protocolDialog.addSavedListener(e -> loadDatasets());
    if (!authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      view.ownerFilter.setValue(authorizationService.getCurrentUser().getEmail());
    }
    clearError();
  }

  @SuppressWarnings("checkstyle:linelength")
  private void loadDatasets() {
    datasetsDataProvider = new ListDataProvider<>(service.all());
    ConfigurableFilterDataProvider<Dataset, Void, SerializablePredicate<Dataset>> dataProvider =
        datasetsDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    view.datasets.setDataProvider(dataProvider);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  private void clearError() {
    view.error.setVisible(false);
  }

  void view(Dataset dataset) {
    clearError();
    view.datasetDialog.setDataset(service.get(dataset.getId()));
    view.datasetDialog.open();
  }

  void view(Protocol protocol) {
    clearError();
    view.protocolDialog.setProtocol(protocolService.get(protocol.getId()));
    view.protocolDialog.open();
  }

  void add() {
    clearError();
    view.datasetDialog.setDataset(new Dataset());
    view.datasetDialog.open();
  }

  void permissions() {
    clearError();
    Dataset dataset = view.datasets.getSelectedItems().stream().findFirst().orElse(null);
    if (dataset == null) {
      AppResources resources = new AppResources(DatasetsView.class, locale);
      view.error.setText(resources.message(DATASETS_REQUIRED));
      view.error.setVisible(true);
    } else if (!authorizationService.hasPermission(dataset, BasePermission.WRITE)) {
      AppResources resources = new AppResources(DatasetsView.class, locale);
      view.error.setText(resources.message(PERMISSIONS_DENIED));
      view.error.setVisible(true);
    } else {
      view.datasetPermissionsDialog.setDataset(dataset);
      view.datasetPermissionsDialog.open();
    }
  }

  void filterName(String value) {
    clearError();
    filter.nameContains = value.isEmpty() ? null : value;
    view.datasets.getDataProvider().refreshAll();
  }

  void filterProject(String value) {
    clearError();
    filter.projectContains = value.isEmpty() ? null : value;
    view.datasets.getDataProvider().refreshAll();
  }

  void filterProtocol(String value) {
    clearError();
    filter.protocolContains = value.isEmpty() ? null : value;
    view.datasets.getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    clearError();
    filter.ownerContains = value.isEmpty() ? null : value;
    view.datasets.getDataProvider().refreshAll();
  }

  WebDatasetFilter filter() {
    return filter;
  }
}