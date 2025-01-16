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
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NOTE;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.uploadI18N;

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
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolDialog.class);
  private static final String PROTOCOL_PREFIX = messagePrefix(Protocol.class);
  private static final String PROTOCOL_FILE_PREFIX = messagePrefix(ProtocolFile.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
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
    setProtocolId(0);
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
    binder.forField(name).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("").withValidator(nameExists()).bind(NAME);
    binder.forField(note).withNullRepresentation("").bind(NOTE);
    updateHeader();
    name.setLabel(getTranslation(PROTOCOL_PREFIX + NAME));
    note.setLabel(getTranslation(PROTOCOL_PREFIX + NOTE));
    upload.setI18n(uploadI18N(getLocale()));
    filename.setHeader(getTranslation(PROTOCOL_FILE_PREFIX + FILENAME));
    remove.setHeader(getTranslation(CONSTANTS_PREFIX + REMOVE));
    save.setText(getTranslation(CONSTANTS_PREFIX + SAVE));
    cancel.setText(getTranslation(CONSTANTS_PREFIX + CANCEL));
    delete.setText(getTranslation(CONSTANTS_PREFIX + DELETE));
    confirm.setHeader(getTranslation(MESSAGE_PREFIX + DELETE_HEADER));
    confirm.setConfirmText(getTranslation(CONSTANTS_PREFIX + DELETE));
    confirm.setCancelText(getTranslation(CONSTANTS_PREFIX + CANCEL));
  }

  private Validator<String> nameExists() {
    return (value, context) -> {
      if (service.nameExists(value) && (binder.getBean().getId() == 0 || !value.equalsIgnoreCase(
          service.get(binder.getBean().getId()).map(Protocol::getName).orElse("")))) {
        return ValidationResult.error(getTranslation(CONSTANTS_PREFIX + ALREADY_EXISTS));
      }
      return ValidationResult.ok();
    };
  }

  private void updateHeader() {
    Protocol protocol = binder.getBean();
    if (protocol != null && protocol.getId() != 0) {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, 1, protocol.getName()));
      confirm.setText(getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, protocol.getName()));
    } else {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, 0));
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
    if (protocol.getId() != 0) {
      readOnly = !authenticatedUser.hasPermission(protocol, Permission.WRITE);
    }
    binder.setReadOnly(readOnly);
    upload.setVisible(!readOnly);
    remove.setVisible(!readOnly);
    save.setVisible(!readOnly);
    cancel.setVisible(!readOnly);
    delete.setVisible(!readOnly && protocol.getId() != 0 && service.isDeletable(protocol));
  }

  void failedFile(String filename) {
    showNotification(getTranslation(MESSAGE_PREFIX + FILES_IOEXCEPTION, filename));
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
      showNotification(getTranslation(MESSAGE_PREFIX + FILES_OVER_MAXIMUM, MAXIMUM_FILES_COUNT));
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
    boolean valid = validateProtocol().isOk();
    if (files.getListDataView().getItemCount() == 0) {
      valid = false;
      filesError.setVisible(true);
      filesError.setText(getTranslation(MESSAGE_PREFIX + FILES_REQUIRED));
    }
    return valid;
  }

  void save() {
    if (isValid()) {
      Protocol protocol = binder.getBean();
      logger.debug("save protocol {}", protocol);
      List<ProtocolFile> files = this.files.getListDataView().getItems().toList();
      service.save(protocol, new ArrayList<>(files));
      showNotification(getTranslation(MESSAGE_PREFIX + SAVED, protocol.getName()));
      close();
      fireSavedEvent();
    }
  }

  void delete() {
    Protocol protocol = binder.getBean();
    logger.debug("delete protocol {}", protocol);
    service.delete(protocol);
    showNotification(getTranslation(MESSAGE_PREFIX + DELETED, protocol.getName()));
    fireDeletedEvent();
    close();
  }

  long getProtocolId() {
    return binder.getBean().getId();
  }

  void setProtocolId(long id) {
    Protocol protocol;
    if (id == 0) {
      protocol = new Protocol();
      protocol.setOwner(authenticatedUser.getUser().orElseThrow());
      files.setItems();
    } else {
      protocol = service.get(id).orElseThrow();
      files.setItems(service.files(protocol));
    }
    binder.setBean(protocol);
    setReadOnly();
    updateHeader();
  }
}
