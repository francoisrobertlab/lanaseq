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

package ca.qc.ircm.lanaseq.protocol.web;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserRole;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Protocols view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolsViewPresenter {
  private static final Logger logger = LoggerFactory.getLogger(ProtocolsViewPresenter.class);
  private ProtocolsView view;
  private ListDataProvider<Protocol> protocolsDataProvider;
  private WebProtocolFilter filter = new WebProtocolFilter();
  private ProtocolService service;
  private AuthorizationService authorizationService;

  @Autowired
  ProtocolsViewPresenter(ProtocolService service, AuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  void init(ProtocolsView view) {
    logger.debug("protocols view");
    this.view = view;
    if (!authorizationService.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER)) {
      view.ownerFilter.setValue(authorizationService.getCurrentUser().getEmail());
    }
    loadProtocols();
    view.dialog.addSavedListener(e -> loadProtocols());
  }

  private void loadProtocols() {
    protocolsDataProvider = DataProvider.ofCollection(service.all());
    ConfigurableFilterDataProvider<Protocol, Void, SerializablePredicate<Protocol>> dataProvider =
        protocolsDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    view.protocols.setDataProvider(dataProvider);
  }

  public void view(Protocol protocol) {
    view.dialog.setProtocol(service.get(protocol.getId()));
    view.dialog.open();
  }

  public void add() {
    view.dialog.setProtocol(new Protocol());
    view.dialog.open();
  }

  public void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    view.protocols.getDataProvider().refreshAll();
  }

  public void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    view.protocols.getDataProvider().refreshAll();
  }

  WebProtocolFilter filter() {
    return filter;
  }
}
