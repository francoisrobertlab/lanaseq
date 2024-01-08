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
import ca.qc.ircm.lanaseq.dataset.DatasetFilter;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import ca.qc.ircm.lanaseq.web.VaadinSort;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
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
  private AuthenticatedUser authenticatedUser;
  private DataProvider<Dataset, DatasetFilter> datasetsDataProvider;
  private WebDatasetFilter filter = new WebDatasetFilter();

  protected DatasetGridPresenter() {
  }

  @Autowired
  protected DatasetGridPresenter(DatasetService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  void init(DatasetGrid grid) {
    this.grid = grid;
    loadDataset();
    if (!authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      authenticatedUser.getUser().ifPresent(user -> grid.ownerFilter.setValue(user.getEmail()));
    }
  }

  private void loadDataset() {
    FetchCallback<Dataset, Void> fetchCallback = query -> {
      filter.sort = VaadinSort.springDataSort(query.getSortOrders());
      filter.page = query.getOffset() / grid.getPageSize();
      filter.size = query.getLimit();
      return service.all(filter).stream();
    };
    grid.setItems(fetchCallback);
  }

  void refreshDatasets() {
    grid.getDataProvider().refreshAll();
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
