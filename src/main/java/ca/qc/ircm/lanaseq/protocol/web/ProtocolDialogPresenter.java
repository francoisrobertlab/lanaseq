package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
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
  private ProtocolService service;

  @Autowired
  ProtocolDialogPresenter(ProtocolService service) {
    this.service = service;
  }

  void init(ProtocolDialog dialog) {
    this.dialog = dialog;
    dialog.files.setDataProvider(filesDataProvider);
    setProtocol(null);
  }

  void localeChange(Locale locale) {
    AppResources resources = new AppResources(Constants.class, locale);
    binder.forField(dialog.name).asRequired(resources.message(REQUIRED)).bind(NAME);
  }

  void addFile(String filename, InputStream input, Locale locale) {
    logger.debug("received file {}", filename);
    ProtocolFile file = new ProtocolFile();
    file.setFilename(filename);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      FileCopyUtils.copy(input, output);
    } catch (IOException e) {
      AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.showNotification(resources.message(FILES_IOEXCEPTION, filename));
    }
    file.setContent(output.toByteArray());
    if (filesDataProvider.getItems().size() >= MAXIMUM_FILES_COUNT) {
      AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.showNotification(resources.message(FILES_OVER_MAXIMUM, MAXIMUM_FILES_COUNT));
    }
    filesDataProvider.getItems().add(file);
    filesDataProvider.refreshAll();
  }

  void removeFile(ProtocolFile file) {
    filesDataProvider.getItems().remove(file);
    filesDataProvider.refreshAll();
  }

  boolean isValid(Locale locale) {
    dialog.filesError.setVisible(false);
    boolean valid = true;
    valid = binder.isValid() && valid;
    if (filesDataProvider.getItems().isEmpty()) {
      valid = false;
      final AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.filesError.setVisible(true);
      dialog.filesError.setText(resources.message(FILES_REQUIRED));
    }
    return valid;
  }

  void save(Locale locale) {
    if (isValid(locale)) {
      Protocol protocol = new Protocol();
      protocol.setFiles(new ArrayList<>(filesDataProvider.getItems()));
      service.save(protocol);
      final AppResources resources = new AppResources(ProtocolDialog.class, locale);
      dialog.showNotification(resources.message(SAVED, protocol.getName()));
      UI.getCurrent().navigate(ProtocolsView.class);
    }
  }

  Protocol getProtocol() {
    return binder.getBean();
  }

  void setProtocol(Protocol protocol) {
    if (protocol == null) {
      protocol = new Protocol();
    }
    if (protocol.getFiles() == null) {
      protocol.setFiles(new ArrayList<>());
    }
    binder.setBean(protocol);
    filesDataProvider.getItems().clear();
    filesDataProvider.getItems().addAll(protocol.getFiles());
    filesDataProvider.refreshAll();
  }
}
