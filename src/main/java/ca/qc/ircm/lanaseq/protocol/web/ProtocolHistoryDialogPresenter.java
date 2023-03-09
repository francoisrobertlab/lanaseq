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

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVERED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Protocol history dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolHistoryDialogPresenter {
  private static final Logger logger =
      LoggerFactory.getLogger(ProtocolHistoryDialogPresenter.class);
  private ProtocolHistoryDialog dialog;
  private Protocol protocol;
  private ListDataProvider<ProtocolFile> filesDataProvider =
      DataProvider.ofCollection(new ArrayList<>());
  private Locale locale;
  private ProtocolService service;

  @Autowired
  ProtocolHistoryDialogPresenter(ProtocolService service) {
    this.service = service;
  }

  void init(ProtocolHistoryDialog dialog) {
    this.dialog = dialog;
    dialog.files.setItems(filesDataProvider);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
  }

  void recoverFile(ProtocolFile file) {
    logger.debug("save recovered protocol files for protocol {}", protocol);
    service.recover(file);
    final AppResources resources = new AppResources(ProtocolHistoryDialog.class, locale);
    dialog.showNotification(resources.message(RECOVERED, file.getFilename()));
    filesDataProvider.getItems().remove(file);
    filesDataProvider.refreshAll();
  }

  void cancel() {
    dialog.close();
  }

  Protocol getProtocol() {
    return protocol;
  }

  void setProtocol(Protocol protocol) {
    Objects.requireNonNull(protocol);
    this.protocol = protocol;
    filesDataProvider.getItems().clear();
    filesDataProvider.getItems().addAll(service.deletedFiles(protocol));
    filesDataProvider.refreshAll();
  }
}
