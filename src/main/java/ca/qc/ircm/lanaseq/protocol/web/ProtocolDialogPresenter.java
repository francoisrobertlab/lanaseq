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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NOTE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_IOEXCEPTION;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_OVER_MAXIMUM;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_REQUIRED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.MAXIMUM_FILES_COUNT;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.SAVED;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.FileCopyUtils;

/**
 * Protocol dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(ProtocolDialogPresenter.class);
  private ProtocolDialog dialog;
  private Binder<Protocol> binder = new BeanValidationBinder<Protocol>(Protocol.class);
  private ListDataProvider<ProtocolFile> filesDataProvider =
      DataProvider.ofCollection(new ArrayList<>());
  private Locale locale;
  private ProtocolService service;
  private AuthenticatedUser authenticatedUser;

  @Autowired
  ProtocolDialogPresenter(ProtocolService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  void init(ProtocolDialog dialog) {
    this.dialog = dialog;
    dialog.files.setItems(filesDataProvider);
    localeChange(Constants.DEFAULT_LOCALE);
    setProtocol(null);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
    AppResources resources = new AppResources(Constants.class, locale);
    binder.forField(dialog.name).asRequired(resources.message(REQUIRED)).withNullRepresentation("")
        .withValidator(nameExists()).bind(NAME);
    binder.forField(dialog.note).withNullRepresentation("").bind(NOTE);
  }

  private Validator<String> nameExists() {
    return (value, context) -> {
      if (service.nameExists(value) && !service.get(binder.getBean().getId())
          .map(pr -> value.equals(pr.getName())).orElse(false)) {
        final AppResources resources = new AppResources(Constants.class, locale);
        return ValidationResult.error(resources.message(ALREADY_EXISTS));
      }
      return ValidationResult.ok();
    };
  }

  private void setReadOnly() {
    boolean readOnly = false;
    Protocol protocol = binder.getBean();
    if (protocol != null && protocol.getId() != null) {
      readOnly = !authenticatedUser.hasPermission(protocol, Permission.WRITE);
    }
    binder.setReadOnly(readOnly);
    dialog.upload.setVisible(!readOnly);
    dialog.remove.setVisible(!readOnly);
    dialog.save.setVisible(!readOnly);
    dialog.cancel.setVisible(!readOnly);
    dialog.delete.setVisible(!readOnly && service.isDeletable(protocol));
  }

  void addFile(String filename, InputStream input) {
    logger.trace("received file {}", filename);
    ProtocolFile file = new ProtocolFile();
    file.setFilename(filename);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      FileCopyUtils.copy(input, output);
    } catch (IOException e) {
      AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.showNotification(resources.message(FILES_IOEXCEPTION, filename));
      return;
    }
    file.setContent(output.toByteArray());
    if (filesDataProvider.getItems().size() >= MAXIMUM_FILES_COUNT) {
      AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.showNotification(resources.message(FILES_OVER_MAXIMUM, MAXIMUM_FILES_COUNT));
      return;
    }
    filesDataProvider.getItems().add(file);
    filesDataProvider.refreshAll();
  }

  void removeFile(ProtocolFile file) {
    filesDataProvider.getItems().remove(file);
    filesDataProvider.refreshAll();
  }

  BinderValidationStatus<Protocol> validateProtocol() {
    return binder.validate();
  }

  boolean isValid() {
    dialog.filesError.setVisible(false);
    boolean valid = true;
    valid = validateProtocol().isOk() && valid;
    if (filesDataProvider.getItems().isEmpty()) {
      valid = false;
      final AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.filesError.setVisible(true);
      dialog.filesError.setText(resources.message(FILES_REQUIRED));
    }
    return valid;
  }

  void save() {
    if (isValid()) {
      Protocol protocol = binder.getBean();
      logger.debug("save protocol {}", protocol);
      service.save(protocol, new ArrayList<>(filesDataProvider.getItems()));
      final AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, protocol.getName()));
      dialog.close();
      dialog.fireSavedEvent();
    }
  }

  void delete() {
    Protocol protocol = binder.getBean();
    logger.debug("delete protocol {}", protocol);
    service.delete(protocol);
    AppResources resources = new AppResources(ProtocolDialog.class, locale);
    dialog.showNotification(resources.message(DELETED, protocol.getName()));
    dialog.fireDeletedEvent();
    dialog.close();
  }

  void cancel() {
    dialog.close();
  }

  Protocol getProtocol() {
    return binder.getBean();
  }

  void setProtocol(Protocol protocol) {
    if (protocol == null) {
      protocol = new Protocol();
    }
    binder.setBean(protocol);
    filesDataProvider.getItems().clear();
    filesDataProvider.getItems().addAll(service.files(protocol));
    filesDataProvider.refreshAll();
    setReadOnly();
  }
}
