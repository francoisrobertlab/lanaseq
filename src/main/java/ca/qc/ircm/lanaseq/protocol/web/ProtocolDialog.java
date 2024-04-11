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
import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NOTE;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.uploadI18N;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.ByteArrayStreamResourceWriter;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.FileCopyUtils;

/**
 * Protocols dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolDialog extends Dialog implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "protocols-dialog";
  public static final String HEADER = "header";
  public static final String FILES = "files";
  public static final String FILES_ERROR = property(FILES, "error");
  public static final String FILES_REQUIRED = property(FILES, REQUIRED);
  public static final String FILES_IOEXCEPTION = property(FILES, "ioexception");
  public static final String FILES_OVER_MAXIMUM = property(FILES, "overmaximum");
  public static final int MAXIMUM_FILES_SIZE = 200 * 1024 * 1024; // 200MB
  public static final int MAXIMUM_FILES_COUNT = 6;
  public static final String REMOVE_BUTTON = "<vaadin-button class='" + REMOVE + "' theme='"
      + ButtonVariant.LUMO_ERROR.getVariantName() + "' @click='${removeFile}'>"
      + "<vaadin-icon icon='vaadin:trash' slot='prefix'></vaadin-icon>" + "</vaadin-button>";
  public static final String SAVED = "saved";
  public static final String DELETED = "deleted";
  public static final String DELETE_HEADER = property(DELETE, "header");
  public static final String DELETE_MESSAGE = property(DELETE, "message");
  private static final Logger logger = LoggerFactory.getLogger(ProtocolDialog.class);
  private static final long serialVersionUID = -7797831034001410430L;
  protected TextField name = new TextField();
  protected TextArea note = new TextArea();
  protected MultiFileMemoryBuffer uploadBuffer = new MultiFileMemoryBuffer();
  protected Upload upload = new Upload(uploadBuffer);
  protected Grid<ProtocolFile> files = new Grid<>();
  protected Column<ProtocolFile> filename;
  protected Column<ProtocolFile> remove;
  protected Div filesError = new Div();
  protected Button save = new Button();
  protected Button cancel = new Button();
  protected Button delete = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  private Binder<Protocol> binder = new BeanValidationBinder<Protocol>(Protocol.class);
  private transient ProtocolService service;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  ProtocolDialog(ProtocolService service, AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.authenticatedUser = authenticatedUser;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    setWidth("1000px");
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    FormLayout form = new FormLayout(name, note);
    layout.add(form, upload, files, filesError, confirm);
    layout.setSizeFull();
    layout.expand(files);
    getFooter().add(delete, cancel, save);
    name.setId(id(NAME));
    note.setId(id(NOTE));
    note.setHeight("10em");
    upload.setId(id(UPLOAD));
    upload.setMaxFileSize(MAXIMUM_FILES_SIZE);
    upload.setMaxFiles(MAXIMUM_FILES_COUNT);
    upload.setMinHeight("2.5em");
    upload.addSucceededListener(
        event -> addFile(event.getFileName(), uploadBuffer.getInputStream(event.getFileName())));
    upload.addFailedListener(event -> failedFile(event.getFileName()));
    files.setId(id(FILES));
    filename = files.addColumn(new ComponentRenderer<>(file -> filenameAnchor(file)))
        .setKey(FILENAME).setSortProperty(FILENAME)
        .setComparator(NormalizedComparator.of(ProtocolFile::getFilename)).setFlexGrow(10);
    remove = files.addColumn(LitRenderer.<ProtocolFile>of(REMOVE_BUTTON).withFunction("removeFile",
        file -> removeFile(file))).setKey(REMOVE);
    filesError.setId(id(FILES_ERROR));
    filesError.addClassName(ERROR_TEXT);
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> close());
    delete.setId(id(DELETE));
    delete.getStyle().set("margin-inline-end", "auto");
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    delete.setIcon(VaadinIcon.TRASH.create());
    delete.addClickListener(e -> confirm.open());
    confirm.setId(id(CONFIRM));
    confirm.setCancelable(true);
    confirm.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName() + " "
        + ButtonVariant.LUMO_PRIMARY.getVariantName());
    confirm.addConfirmListener(e -> delete());
    setProtocol(null);
  }

  private Anchor filenameAnchor(ProtocolFile file) {
    Anchor link = new Anchor();
    link.getElement().setAttribute("download", file.getFilename());
    link.setText(file.getFilename());
    link.setHref(new StreamResource(file.getFilename(),
        new ByteArrayStreamResourceWriter(file.getContent())));
    return link;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources protocolResources = new AppResources(Protocol.class, getLocale());
    final AppResources protocolFileResources = new AppResources(ProtocolFile.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    final AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
    binder.forField(name).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .withValidator(nameExists()).bind(NAME);
    binder.forField(note).withNullRepresentation("").bind(NOTE);
    updateHeader();
    name.setLabel(protocolResources.message(NAME));
    note.setLabel(protocolResources.message(NOTE));
    upload.setI18n(uploadI18N(getLocale()));
    filename.setHeader(protocolFileResources.message(FILENAME));
    remove.setHeader(webResources.message(REMOVE));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    delete.setText(webResources.message(DELETE));
    confirm.setHeader(resources.message(DELETE_HEADER));
    confirm.setConfirmText(webResources.message(DELETE));
    confirm.setCancelText(webResources.message(CANCEL));
  }

  private Validator<String> nameExists() {
    return (value, context) -> {
      if (service.nameExists(value) && !service.get(binder.getBean().getId())
          .map(pr -> value.equals(pr.getName())).orElse(false)) {
        final AppResources resources = new AppResources(Constants.class, getLocale());
        return ValidationResult.error(resources.message(ALREADY_EXISTS));
      }
      return ValidationResult.ok();
    };
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
    Protocol protocol = binder.getBean();
    if (protocol != null && protocol.getId() != null) {
      setHeaderTitle(resources.message(HEADER, 1, protocol.getName()));
      confirm.setText(resources.message(DELETE_MESSAGE, protocol.getName()));
    } else {
      setHeaderTitle(resources.message(HEADER, 0));
    }
  }

  /**
   * Adds listener to be informed when a protocol was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addSavedListener(ComponentEventListener<SavedEvent<ProtocolDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  /**
   * Adds listener to be informed when a sample was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addDeletedListener(ComponentEventListener<DeletedEvent<ProtocolDialog>> listener) {
    return addListener((Class) DeletedEvent.class, listener);
  }

  void fireDeletedEvent() {
    fireEvent(new DeletedEvent<>(this, true));
  }

  private void setReadOnly() {
    boolean readOnly = false;
    Protocol protocol = binder.getBean();
    if (protocol != null && protocol.getId() != null) {
      readOnly = !authenticatedUser.hasPermission(protocol, Permission.WRITE);
    }
    binder.setReadOnly(readOnly);
    upload.setVisible(!readOnly);
    remove.setVisible(!readOnly);
    save.setVisible(!readOnly);
    cancel.setVisible(!readOnly);
    delete.setVisible(!readOnly && service.isDeletable(protocol));
  }

  void failedFile(String filename) {
    AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
    showNotification(resources.message(FILES_IOEXCEPTION, filename));
  }

  void addFile(String filename, InputStream input) {
    logger.trace("received file {}", filename);
    ProtocolFile file = new ProtocolFile();
    file.setFilename(filename);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      FileCopyUtils.copy(input, output);
    } catch (IOException e) {
      failedFile(filename);
      return;
    }
    file.setContent(output.toByteArray());
    if (files.getListDataView().getItemCount() >= MAXIMUM_FILES_COUNT) {
      AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
      showNotification(resources.message(FILES_OVER_MAXIMUM, MAXIMUM_FILES_COUNT));
      return;
    }
    files.getListDataView().addItem(file);
  }

  void removeFile(ProtocolFile file) {
    files.getListDataView().removeItem(file);
  }

  BinderValidationStatus<Protocol> validateProtocol() {
    return binder.validate();
  }

  boolean isValid() {
    filesError.setVisible(false);
    boolean valid = true;
    valid = validateProtocol().isOk() && valid;
    if (files.getListDataView().getItemCount() == 0) {
      valid = false;
      final AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
      filesError.setVisible(true);
      filesError.setText(resources.message(FILES_REQUIRED));
    }
    return valid;
  }

  void save() {
    if (isValid()) {
      Protocol protocol = binder.getBean();
      logger.debug("save protocol {}", protocol);
      List<ProtocolFile> files = this.files.getListDataView().getItems().toList();
      service.save(protocol, new ArrayList<>(files));
      final AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
      showNotification(resources.message(SAVED, protocol.getName()));
      close();
      fireSavedEvent();
    }
  }

  void delete() {
    Protocol protocol = binder.getBean();
    logger.debug("delete protocol {}", protocol);
    service.delete(protocol);
    AppResources resources = new AppResources(ProtocolDialog.class, getLocale());
    showNotification(resources.message(DELETED, protocol.getName()));
    fireDeletedEvent();
    close();
  }

  Protocol getProtocol() {
    return binder.getBean();
  }

  void setProtocol(Protocol protocol) {
    if (protocol == null) {
      protocol = new Protocol();
    }
    binder.setBean(protocol);
    files.setItems(service.files(protocol));
    setReadOnly();
    updateHeader();
  }
}
