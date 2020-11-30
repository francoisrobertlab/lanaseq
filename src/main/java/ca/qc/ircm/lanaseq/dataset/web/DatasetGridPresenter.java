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

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset grid presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetGridPresenter {
  private DatasetGrid grid;
  private DatasetService service;
  private AuthorizationService authorizationService;
  private ListDataProvider<Dataset> datasetsDataProvider;
  private WebDatasetFilter filter = new WebDatasetFilter();

  protected DatasetGridPresenter() {
  }

  @Autowired
  protected DatasetGridPresenter(DatasetService service,
      AuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  void init(DatasetGrid grid) {
    this.grid = grid;
    refreshDatasets();
    if (!authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      grid.ownerFilter.setValue(authorizationService.getCurrentUser().getEmail());
    }
  }

  @SuppressWarnings("checkstyle:linelength")
  void refreshDatasets() {
    List<Dataset> datasets = service.all();
    sort(datasets, comparing(Dataset::getDate).reversed());
    datasetsDataProvider = new ListDataProvider<>(datasets);
    ConfigurableFilterDataProvider<Dataset, Void, SerializablePredicate<Dataset>> dataProvider =
        datasetsDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    grid.setDataProvider(dataProvider);
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    grid.getDataProvider().refreshAll();
  }

  void filterTags(String value) {
    filter.tagsContains = value.isEmpty() ? null : value;
    grid.getDataProvider().refreshAll();
  }

  void filterProtocol(String value) {
    filter.protocolContains = value.isEmpty() ? null : value;
    grid.getDataProvider().refreshAll();
  }

  void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    grid.getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    grid.getDataProvider().refreshAll();
  }

  WebDatasetFilter filter() {
    return filter;
  }
}
