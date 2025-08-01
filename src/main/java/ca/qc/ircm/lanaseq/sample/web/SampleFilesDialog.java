package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.Constants.REFRESH;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.uploadI18N;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
import ca.qc.ircm.lanaseq.web.EditableFile;
import ca.qc.ircm.lanaseq.web.WarningNotification;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Sample files dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleFilesDialog extends Dialog implements LocaleChangeObserver {

  public static final String ID = "sample-files-dialog";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String FOLDERS = "folders";
  public static final String FILES = "files";
  public static final String PUBLIC_FILE = "publicFile";
  public static final String FILENAME = "filename";
  public static final String FILENAME_REGEX = "[\\w-\\.]*";
  public static final String FILENAME_REGEX_ERROR = property("filename", "regex");
  public static final String FILE_RENAME_ERROR = property("filename", "rename", "error");
  public static final String ADD_LARGE_FILES = "addLargeFiles";
  public static final String FILES_IOEXCEPTION = property(FILES, "ioexception");
  public static final String FILES_SUCCESS = property(FILES, "success");
  public static final int MAXIMUM_SMALL_FILES_SIZE = 200 * 1024 * 1024; // 200MB
  public static final int MAXIMUM_SMALL_FILES_COUNT = 50;
  public static final String FILENAME_HTML = "<span title='${item.title}'>${item.filename}</span>";
  private static final String MESSAGE_PREFIX = messagePrefix(SampleFilesDialog.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final Logger logger = LoggerFactory.getLogger(SampleFilesDialog.class);
  @Serial
  private static final long serialVersionUID = 166699830639260659L;
  protected Div message = new Div();
  protected VerticalLayout folders = new VerticalLayout();
  protected Grid<EditableFile> files = new Grid<>();
  protected Column<EditableFile> filename;
  protected Column<EditableFile> download;
  protected Column<EditableFile> publicFile;
  protected Column<EditableFile> delete;
  protected TextField filenameEdit = new TextField();
  protected MultiFileBuffer uploadBuffer = new MultiFileBuffer();
  protected Button refresh = new Button();
  protected Upload upload = new Upload(uploadBuffer);
  protected Button addLargeFiles = new Button();
  private Sample sample;
  private final Binder<EditableFile> fileBinder = new BeanValidationBinder<>(EditableFile.class);
  private final transient ObjectFactory<AddSampleFilesDialog> addFilesDialogFactory;
  private final transient SampleService service;
  private final transient AuthenticatedUser authenticatedUser;
  private final transient AppConfiguration configuration;
  /**
   * Currently authenticated user.
   * <p>
   * This is needed because Vaadin's upload does not contain authentication information.
   * </p>
   */
  private Authentication authentication;

  protected SampleFilesDialog(ObjectFactory<AddSampleFilesDialog> addFilesDialogFactory,
      SampleService service, AuthenticatedUser authenticatedUser, AppConfiguration configuration) {
    this.addFilesDialogFactory = addFilesDialogFactory;
    this.service = service;
    this.authenticatedUser = authenticatedUser;
    this.configuration = configuration;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    this.authentication = SecurityContextHolder.getContext().getAuthentication();
    setId(ID);
    setWidth("1000px");
    setHeight("700px");
    setResizable(true);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    VerticalLayout messageAndFolders = new VerticalLayout(message, folders);
    messageAndFolders.setSpacing(false);
    messageAndFolders.setPadding(false);
    layout.add(messageAndFolders, files);
    layout.setSizeFull();
    layout.expand(files);
    getFooter().add(refresh, upload, addLargeFiles);
    message.setId(id(MESSAGE));
    folders.setId(id(FOLDERS));
    folders.setPadding(false);
    folders.setSpacing(false);
    files.setId(id(FILES));
    files.getEditor().addCloseListener(e -> {
      if (fileBinder.validate().isOk()) {
        rename(e.getItem());
      } else {
        e.getItem().setFilename(e.getItem().getFile().getName());
      }
    });
    files.addItemDoubleClickListener(e -> {
      files.getEditor().editItem(e.getItem());
      filenameEdit.focus();
    });
    files.getEditor().setBinder(fileBinder);
    filename = files.addColumn(LitRenderer.<EditableFile>of(FILENAME_HTML)
            .withProperty("filename", file -> shortFilename(file.getFilename()))
            .withProperty("title", EditableFile::getFilename)).setKey(FILENAME)
        .setComparator(Comparator.comparing(EditableFile::getFilename)).setFlexGrow(10);
    download = files.addColumn(new ComponentRenderer<>(this::downloadButton)).setKey(DOWNLOAD)
        .setSortable(false);
    publicFile = files.addColumn(new ComponentRenderer<>(this::publicFileCheckbox))
        .setKey(PUBLIC_FILE).setSortable(false);
    delete = files.addColumn(new ComponentRenderer<>(this::deleteButton)).setKey(DELETE)
        .setSortable(false);
    filename.setEditorComponent(filenameEdit);
    filenameEdit.setId(id(FILENAME));
    filenameEdit.addKeyDownListener(Key.ENTER, e -> {
      if (fileBinder.validate().isOk()) {
        files.getEditor().closeEditor();
      }
    });
    filenameEdit.addValueChangeListener(e -> fileBinder.validate());
    filenameEdit.setValueChangeMode(ValueChangeMode.LAZY);
    filenameEdit.setWidthFull();
    refresh.setId(id(REFRESH));
    refresh.setIcon(VaadinIcon.REFRESH.create());
    refresh.addClickListener(e -> updateFiles());
    upload.setId(id(UPLOAD));
    upload.setMaxFileSize(MAXIMUM_SMALL_FILES_SIZE);
    upload.setMaxFiles(MAXIMUM_SMALL_FILES_COUNT);
    upload.setMaxHeight("5em"); // Hide name of uploaded files.
    upload.addSucceededListener(event -> addSmallFile(event.getFileName(),
        uploadBuffer.getFileData(event.getFileName()).getFile()));
    addLargeFiles.setId(id(ADD_LARGE_FILES));
    addLargeFiles.setIcon(VaadinIcon.PLUS.create());
    addLargeFiles.addClickListener(e -> addLargeFiles());
  }

  private String shortFilename(String filename) {
    String name = Optional.ofNullable(sample).map(Sample::getName).orElse("");
    if (name.length() > 20 && filename.contains(name)) {
      String start = name.substring(0, 11);
      String end = name.substring(name.length() - 9);
      filename = filename.replaceAll(name, start + "..." + end);
    }
    return filename;
  }

  private Anchor downloadButton(EditableFile file) {
    Anchor anchor = new Anchor();
    anchor.addClassName(DOWNLOAD);
    anchor.getElement().setAttribute("download", true);
    anchor.setHref(download(file));
    Button button = new Button();
    anchor.add(button);
    button.setIcon(VaadinIcon.DOWNLOAD.create());
    return anchor;
  }

  private Checkbox publicFileCheckbox(EditableFile file) {
    boolean fileIsPublic = service.isFilePublic(sample, file.getFile().toPath());
    Checkbox checkbox = new Checkbox();
    checkbox.addClassName(PUBLIC_FILE);
    checkbox.setValue(fileIsPublic);
    checkbox.addValueChangeListener(e -> changePublicFile(file, e.getValue()));
    return checkbox;
  }

  private Button deleteButton(EditableFile file) {
    Button button = new Button();
    button.addClassName(DELETE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addThemeVariants(ButtonVariant.LUMO_ERROR);
    button.setEnabled(!isArchive(file));
    button.addClickListener(e -> deleteFile(file));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    fileBinder.forField(filenameEdit).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("").withValidator(
            new RegexpValidator(getTranslation(MESSAGE_PREFIX + FILENAME_REGEX_ERROR), FILENAME_REGEX))
        .withValidator(exists()).bind(FILENAME);
    setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, 0));
    message.setText("");
    message.setTitle("");
    filename.setHeader(getTranslation(MESSAGE_PREFIX + FILENAME));
    download.setHeader(getTranslation(CONSTANTS_PREFIX + DOWNLOAD));
    publicFile.setHeader(getTranslation(MESSAGE_PREFIX + PUBLIC_FILE));
    delete.setHeader(getTranslation(CONSTANTS_PREFIX + DELETE));
    refresh.setText(getTranslation(CONSTANTS_PREFIX + REFRESH));
    addLargeFiles.setText(getTranslation(MESSAGE_PREFIX + ADD_LARGE_FILES));
    upload.setI18n(uploadI18N(getLocale()));
    updateHeader();
    updateMessage();
  }

  private Validator<String> exists() {
    return (value, context) -> {
      EditableFile item = files.getEditor().getItem();
      if (value != null && item != null && !value.equals(item.getFile().getName()) && Files.exists(
          item.getFile().toPath().resolveSibling(value))) {
        return ValidationResult.error(getTranslation(CONSTANTS_PREFIX + ALREADY_EXISTS));
      }
      return ValidationResult.ok();
    };
  }

  private void updateHeader() {
    if (sample != null) {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, sample.getName()));
    } else {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER));
    }
  }

  private void updateMessage() {
    getUI().ifPresent(ui -> {
      WebBrowser browser = ui.getSession().getBrowser();
      boolean unix = browser.isMacOSX() || browser.isLinux();
      if (sample != null) {
        List<String> labels = service.folderLabels(sample, unix);
        message.setText(getTranslation(MESSAGE_PREFIX + DatasetFilesDialog.MESSAGE, labels.size()));
        folders.removeAll();
        labels.forEach(label -> folders.add(new Span(label)));
      }
    });
  }

  private void updateFiles() {
    files.setItems(service.files(sample).stream().map(file -> new EditableFile(file.toFile()))
        .collect(Collectors.toList()));
  }

  boolean isArchive(EditableFile file) {
    return !configuration.getHome().folder(sample).equals(file.getFile().toPath().getParent());
  }

  void addSmallFile(String filename, File file) {
    logger.debug("saving file {} to dataset {}", filename, sample);
    SecurityContextHolder.getContext()
        .setAuthentication(authentication); // Sets user for current thread.
    // TODO Fix filename is given by the user and may be used to hack the system.
    final UI ui = UI.getCurrent();
    service.saveFiles(sample, Collections.nCopies(1, file.toPath()), f -> filename)
        .thenAccept(e -> {
          ui.accessLater(
              () -> Notification.show(getTranslation(MESSAGE_PREFIX + FILES_SUCCESS, filename)),
              null).run();
        }).exceptionally(e -> {
          logger.warn("Exception thrown while copying file {} for sample {}", file, sample, e);
          ui.accessLater(() -> new WarningNotification(
              getTranslation(MESSAGE_PREFIX + FILES_IOEXCEPTION, filename)).open(), null).run();
          try {
            Files.deleteIfExists(file.toPath());
          } catch (IOException ex) {
            // Ignore error since file was probably already deleted.
          }
          return null;
        });
    updateFiles();
  }

  void addLargeFiles() {
    AddSampleFilesDialog addFilesDialog = addFilesDialogFactory.getObject();
    addFilesDialog.setSampleId(sample.getId());
    addFilesDialog.addSavedListener(e -> updateFiles());
    addFilesDialog.open();
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
      new WarningNotification(
          getTranslation(MESSAGE_PREFIX + FILE_RENAME_ERROR, source.getFileName(),
              file.getFilename())).open();
    }
  }

  StreamResource download(EditableFile file) {
    return new StreamResource(file.getFilename(),
        (output, session) -> Files.copy(file.getFile().toPath(), output));
  }

  void changePublicFile(EditableFile file, boolean makePublic) {
    if (makePublic) {
      service.allowPublicFileAccess(sample, file.getFile().toPath(),
          LocalDate.now().plus(configuration.getPublicFilePeriod()));
    } else {
      service.revokePublicFileAccess(sample, file.getFile().toPath());
    }
    files.getDataProvider().refreshItem(file);
  }

  void deleteFile(EditableFile file) {
    File path = file.getFile();
    logger.debug("delete file {}", path);
    service.deleteFile(sample, path.toPath());
    updateFiles();
  }

  BinderValidationStatus<EditableFile> validateSampleFile() {
    return fileBinder.validate();
  }

  public long getSampleId() {
    return sample.getId();
  }

  public void setSampleId(long id) {
    sample = service.get(id).orElseThrow();
    boolean readOnly =
        !sample.isEditable() || !authenticatedUser.hasPermission(sample, Permission.WRITE);
    fileBinder.setReadOnly(readOnly);
    delete.setVisible(!readOnly);
    upload.setVisible(!readOnly);
    addLargeFiles.setVisible(!readOnly);
    updateHeader();
    updateMessage();
    updateFiles();
  }
}
