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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME_REGEX;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILE_RENAME_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE_TITLE;
import static ca.qc.ircm.lanaseq.web.EditableFileProperties.FILENAME;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.Permission;
import ca.qc.ircm.lanaseq.web.EditableFile;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset dialog presenter.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetFilesDialogPresenter {
  private static final Logger logger = LoggerFactory.getLogger(DatasetFilesDialogPresenter.class);
  private DatasetFilesDialog dialog;
  private Dataset dataset;
  private Locale locale;
  private DatasetService service;
  private SampleService sampleService;
  private AuthorizationService authorizationService;
  private AppConfiguration configuration;
  private Binder<EditableFile> fileBinder = new BeanValidationBinder<>(EditableFile.class);

  @Autowired
  protected DatasetFilesDialogPresenter(DatasetService service, SampleService sampleService,
      AuthorizationService authorizationService, AppConfiguration configuration) {
    this.service = service;
    this.sampleService = sampleService;
    this.authorizationService = authorizationService;
    this.configuration = configuration;
  }

  void init(DatasetFilesDialog dialog) {
    this.dialog = dialog;
    dialog.files.getEditor().setBinder(fileBinder);
    dialog.addFilesDialog.addSavedListener(e -> updateFiles());
    dialog.sampleFilesDialog.addOpenedChangeListener(e -> {
      if (!e.isOpened()) {
        dialog.samples.getDataProvider().refreshAll();
      }
    });
    localeChange(Constants.DEFAULT_LOCALE);
  }

  void localeChange(Locale locale) {
    this.locale = locale;
    final AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    fileBinder.forField(dialog.filenameEdit).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(new RegexpValidator(resources.message(FILENAME_REGEX_ERROR), FILENAME_REGEX))
        .withValidator(exists()).bind(FILENAME);
    updateMessage();
  }

  private Validator<String> exists() {
    return (value, context) -> {
      EditableFile item = dialog.files.getEditor().getItem();
      if (value != null && item != null && !value.equals(item.getFile().getName())
          && Files.exists(item.getFile().toPath().resolveSibling(value))) {
        final AppResources webResources = new AppResources(Constants.class, locale);
        return ValidationResult.error(webResources.message(ALREADY_EXISTS));
      }
      return ValidationResult.ok();
    };
  }

  private void updateMessage() {
    dialog.getUI().ifPresent(ui -> {
      final AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
      WebBrowser browser = ui.getSession().getBrowser();
      boolean unix = browser.isMacOSX() || browser.isLinux();
      if (dataset != null) {
        dialog.message
            .setText(resources.message(MESSAGE, configuration.folderLabel(dataset, unix)));
      }
      String network = configuration.folderNetwork(unix);
      dialog.message.setTitle(
          network != null && !network.isEmpty() ? resources.message(MESSAGE_TITLE, network) : "");
    });
  }

  private void updateFiles() {
    dialog.files
        .setItems(service.files(dataset).stream().map(file -> new EditableFile(file.toFile())));
  }

  boolean isReadOnly() {
    return dataset == null || !dataset.isEditable()
        || !authorizationService.hasPermission(dataset, Permission.WRITE);
  }

  int fileCount(Sample sample) {
    return sampleService.files(sample).size();
  }

  void viewFiles(Sample sample) {
    dialog.sampleFilesDialog.setSample(sample);
    dialog.sampleFilesDialog.open();
  }

  void add() {
    if (!isReadOnly()) {
      dialog.addFilesDialog.open();
    }
  }

  void rename(EditableFile file) {
    Path source = file.getFile().toPath();
    Path target = source.resolveSibling(file.getFilename());
    try {
      logger.debug("rename file {} to {}", source, target);
      Files.move(source, target);
      updateFiles();
    } catch (IOException e) {
      logger.error("renaming of file {} to {} failed", source, target);
      final AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
      dialog.showNotification(
          resources.message(FILE_RENAME_ERROR, source.getFileName(), file.getFilename()));
    }
  }

  void deleteFile(EditableFile file) {
    Path path = file.getFile().toPath();
    logger.debug("delete file {}", path);
    service.deleteFile(dataset, path);
    updateFiles();
  }

  BinderValidationStatus<EditableFile> validateDatasetFile() {
    return fileBinder.validate();
  }

  Dataset getDataset() {
    return dataset;
  }

  void setDataset(Dataset dataset) {
    Objects.requireNonNull(dataset);
    Objects.requireNonNull(dataset.getId());
    this.dataset = dataset;
    boolean readOnly = isReadOnly();
    fileBinder.setReadOnly(readOnly);
    dialog.delete.setVisible(!readOnly);
    dialog.samples.setItems(dataset.getSamples());
    dialog.addFilesDialog.setDataset(dataset);
    updateMessage();
    updateFiles();
  }
}
