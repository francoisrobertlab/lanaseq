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

package ca.qc.ircm.lanaseq.sample.web;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Select sample dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectSampleDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(SelectSampleDialogPresenter.class);
  private SelectSampleDialog dialog;
  private ListDataProvider<Sample> samplesDataProvider;
  private WebSampleFilter filter = new WebSampleFilter();
  private SampleService service;
  private AuthorizationService authorizationService;

  @Autowired
  SelectSampleDialogPresenter(SampleService service, AuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  void init(SelectSampleDialog dialog) {
    logger.debug("select sample dialog");
    this.dialog = dialog;
    if (!authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      dialog.ownerFilter.setValue(authorizationService.getCurrentUser().getEmail());
    }
    loadSamples();
  }

  private void loadSamples() {
    samplesDataProvider = DataProvider.ofCollection(service.all());
    ConfigurableFilterDataProvider<Sample, Void, SerializablePredicate<Sample>> dataProvider =
        samplesDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    dialog.samples.setDataProvider(dataProvider);
  }

  public void select(Sample sample) {
    dialog.fireSelectedEvent(sample);
    dialog.close();
  }

  public void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    dialog.samples.getDataProvider().refreshAll();
  }

  public void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    dialog.samples.getDataProvider().refreshAll();
  }

  public void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    dialog.samples.getDataProvider().refreshAll();
  }

  WebSampleFilter filter() {
    return filter;
  }
}
