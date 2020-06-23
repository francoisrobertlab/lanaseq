package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME_REGEX;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILE_RENAME_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE_TITLE;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.DatasetFile;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.Permission;
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
  private AuthorizationService authorizationService;
  private AppConfiguration configuration;
  private Binder<DatasetFile> fileBinder = new BeanValidationBinder<>(DatasetFile.class);

  @Autowired
  protected DatasetFilesDialogPresenter(DatasetService service,
      AuthorizationService authorizationService, AppConfiguration configuration) {
    this.service = service;
    this.authorizationService = authorizationService;
    this.configuration = configuration;
  }

  void init(DatasetFilesDialog dialog) {
    this.dialog = dialog;
    dialog.files.getEditor().setBinder(fileBinder);
    dialog.addFilesDialog.addSavedListener(e -> updateFiles());
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
      DatasetFile item = dialog.files.getEditor().getItem();
      if (value != null && item != null && !value.equals(item.getPath().getFileName().toString())
          && Files.exists(item.getPath().resolveSibling(value))) {
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
    dialog.files.setItems(service.files(dataset).stream().map(file -> new DatasetFile(file)));
  }

  boolean isReadOnly() {
    return dataset == null || !dataset.isEditable()
        || !authorizationService.hasPermission(dataset, Permission.WRITE);
  }

  void add() {
    if (!isReadOnly()) {
      dialog.addFilesDialog.open();
    }
  }

  void rename(DatasetFile file) {
    Path source = file.getPath();
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

  void deleteFile(DatasetFile file) {
    Path path = file.getPath();
    try {
      logger.debug("delete file {}", path);
      Files.delete(path);
      updateFiles();
    } catch (IOException e) {
      logger.error("deleting file {} failed", path);
      final AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
      dialog.showNotification(resources.message(FILE_RENAME_ERROR, path.getFileName()));
    }
  }

  BinderValidationStatus<DatasetFile> validateDatasetFile() {
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
    dialog.addFilesDialog.setDataset(dataset);
    updateMessage();
    updateFiles();
  }
}
