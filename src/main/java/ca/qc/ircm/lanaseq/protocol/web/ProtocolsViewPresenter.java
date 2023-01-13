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

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.PROTOCOLS_REQUIRED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.UserRole;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Protocols view presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolsViewPresenter {
  private ProtocolsView view;
  private ListDataProvider<Protocol> protocolsDataProvider;
  private WebProtocolFilter filter = new WebProtocolFilter();
  private Locale locale;
  private ProtocolService service;
  private AuthenticatedUser authenticatedUser;

  @Autowired
  ProtocolsViewPresenter(ProtocolService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  void init(ProtocolsView view) {
    this.view = view;
    boolean manager = authenticatedUser.hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
    if (!manager) {
      authenticatedUser.getUser().ifPresent(user -> view.ownerFilter.setValue(user.getEmail()));
    }
    view.history.setVisible(manager);
    loadProtocols();
    view.dialog.addSavedListener(e -> loadProtocols());
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  private void loadProtocols() {
    protocolsDataProvider = DataProvider.ofCollection(service.all());
    ConfigurableFilterDataProvider<Protocol, Void, SerializablePredicate<Protocol>> dataProvider =
        protocolsDataProvider.withConfigurableFilter();
    dataProvider.setFilter(filter);
    view.protocols.setDataProvider(dataProvider);
  }

  void edit(Protocol protocol) {
    view.dialog.setProtocol(service.get(protocol.getId()).orElse(null));
    view.dialog.open();
  }

  void history() {
    List<Protocol> protocols = new ArrayList<>(view.protocols.getSelectedItems());
    AppResources resources = new AppResources(ProtocolsView.class, locale);
    boolean error = false;
    if (protocols.isEmpty()) {
      view.error.setText(resources.message(PROTOCOLS_REQUIRED));
      error = true;
    } else if (protocols.size() > 1) {
      view.error.setText(resources.message(PROTOCOLS_MORE_THAN_ONE));
      error = true;
    }
    view.error.setVisible(error);
    if (!error) {
      Protocol protocol = protocols.iterator().next();
      history(protocol);
    }
  }

  void history(Protocol protocol) {
    if (authenticatedUser.hasAnyRole(UserRole.MANAGER, UserRole.ADMIN)) {
      view.historyDialog.setProtocol(service.get(protocol.getId()).orElse(null));
      view.historyDialog.open();
    }
  }

  void add() {
    view.dialog.setProtocol(new Protocol());
    view.dialog.open();
  }

  void filterName(String value) {
    filter.nameContains = value.isEmpty() ? null : value;
    view.protocols.getDataProvider().refreshAll();
  }

  void filterDate(Range<LocalDate> value) {
    filter.dateRange = value;
    view.protocols.getDataProvider().refreshAll();
  }

  void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    view.protocols.getDataProvider().refreshAll();
  }

  WebProtocolFilter filter() {
    return filter;
  }
}
