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

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
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
 * Samples view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SamplesViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(SamplesViewPresenter.class);
  private SamplesView view;
  private ListDataProvider<Sample> samplesDataProvider;
  private WebSampleFilter filter = new WebSampleFilter();
  private SampleService service;
  private ProtocolService protocolService;
  private AuthorizationService authorizationService;

  @Autowired
  SamplesViewPresenter(SampleService service, ProtocolService protocolService,
      AuthorizationService authorizationService) {
    this.service = service;
    this.protocolService = protocolService;
    this.authorizationService = authorizationService;
  }

  void init(SamplesView view) {
    logger.debug("samples view");
    this.view = view;
    if (!authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      view.ownerFilter.setValue(authorizationService.getCurrentUser().getEmail());
    }
    loadSamples();
    view.dialog.addSavedListener(e -> loadSamples());
    view.protocolDialog.addSavedListener(e -> loadSamples());
  }

  private void loadSamples() {
    samplesDataProvider = DataProvider.ofCollection(service.all());
    ConfigurableFilterDataProvider<Sample, Void, SerializablePredicate<Sample>> dataProvider =
        samplesDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    view.samples.setDataProvider(dataProvider);
  }

  public void view(Sample sample) {
    view.dialog.setSample(service.get(sample.getId()));
    view.dialog.open();
  }

  public void view(Protocol protocol) {
    view.protocolDialog.setProtocol(protocolService.get(protocol.getId()));
    view.protocolDialog.open();
  }

  public void add() {
    view.dialog.setSample(null);
    view.dialog.open();
  }

  public void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    view.samples.getDataProvider().refreshAll();
  }

  public void filterProtocol(String value) {
    filter.protocolContains = value.isEmpty() ? null : value;
    view.samples.getDataProvider().refreshAll();
  }

  public void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    view.samples.getDataProvider().refreshAll();
  }

  public void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    view.samples.getDataProvider().refreshAll();
  }

  WebSampleFilter filter() {
    return filter;
  }
}