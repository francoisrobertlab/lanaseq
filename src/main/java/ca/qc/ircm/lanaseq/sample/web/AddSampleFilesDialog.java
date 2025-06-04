package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REFRESH;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.normalizedCollator;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.WarningNotification;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Add sample files dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AddSampleFilesDialog extends Dialog implements LocaleChangeObserver {

  public static final String ID = "add-sample-files-dialog";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String FILES = "files";
  public static final String FILENAME = "filename";
  public static final String SIZE = "size";
  public static final String SIZE_VALUE = property("size", "value");
  public static final String OVERWRITE = "overwrite";
  public static final String SAVED = "saved";
  public static final String CREATE_FOLDER_ERROR = property("createFolder", "error");
  public static final String OVERWRITE_ERROR = property(OVERWRITE, "error");
  private static final String MESSAGE_PREFIX = messagePrefix(AddSampleFilesDialog.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 166699830639260659L;
  private static final Logger logger = LoggerFactory.getLogger(AddSampleFilesDialog.class);
  protected Div message = new Div();
  protected Grid<File> files = new Grid<>();
  protected Column<File> filename;
  protected Column<File> size;
  protected Column<File> overwrite;
  protected Checkbox overwriteAll = new Checkbox();
  protected Div error = new Div();
  protected Button refresh = new Button();
  protected Button save = new Button();
  private final Map<File, Checkbox> overwriteFields = new HashMap<>();
  private Sample sample;
  private Set<String> existingFilenames = new HashSet<>();
  private final transient SampleService service;
  private final transient AppConfiguration configuration;

  @Autowired
  protected AddSampleFilesDialog(SampleService service, AppConfiguration configuration) {
    this.service = service;
    this.configuration = configuration;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    setWidth("1000px");
    setHeight("700px");
    setResizable(true);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(message, files, error);
    layout.setSizeFull();
    layout.expand(files);
    getFooter().add(refresh, save);
    message.setId(id(MESSAGE));
    files.setId(id(FILES));
    filename = files.addColumn(new ComponentRenderer<>(this::filename)).setKey(FILENAME)
        .setSortProperty(FILENAME)
        .setComparator(Comparator.comparing(File::getName, normalizedCollator())).setFlexGrow(10);
    overwrite = files.addColumn(new ComponentRenderer<>(this::overwrite)).setKey(OVERWRITE)
        .setSortable(false);
    files.appendHeaderRow(); // Headers.
    HeaderRow headerRow = files.appendHeaderRow();
    headerRow.getCell(overwrite).setComponent(overwriteAll);
    overwriteAll.addValueChangeListener(
        e -> overwriteFields.values().forEach(c -> c.setValue(e.getValue())));
    refresh.setId(id(REFRESH));
    refresh.setIcon(VaadinIcon.REFRESH.create());
    refresh.addClickListener(e -> updateFiles());
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
  }

  private Span filename(File file) {
    Span span = new Span();
    span.setText(file.getName());
    if (exists(file)) {
      span.addClassName(ERROR_TEXT);
    }
    return span;
  }

  Checkbox overwrite(File file) {
    if (overwriteFields.containsKey(file)) {
      return overwriteFields.get(file);
    }
    Checkbox checkbox = new Checkbox();
    checkbox.addValueChangeListener(e -> {
      if (!e.getValue()) {
        overwriteAll.setValue(false);
      }
    });
    overwriteFields.put(file, checkbox);
    return checkbox;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, 0));
    message.setText("");
    filename.setHeader(getTranslation(MESSAGE_PREFIX + FILENAME));
    if (size != null) {
      files.removeColumn(size);
    }
    NumberFormat sizeFormat = NumberFormat.getIntegerInstance(getLocale());
    size = files.addColumn(file -> getTranslation(MESSAGE_PREFIX + SIZE_VALUE,
        sizeFormat.format(file.length() / Math.pow(1024, 2))), SIZE).setKey(SIZE);
    @SuppressWarnings("unchecked") Column<File>[] sortOrder = new Column[]{filename, size,
        overwrite};
    files.setColumnOrder(sortOrder);
    size.setHeader(getTranslation(MESSAGE_PREFIX + SIZE));
    overwrite.setHeader(getTranslation(MESSAGE_PREFIX + OVERWRITE));
    refresh.setText(getTranslation(CONSTANTS_PREFIX + REFRESH));
    save.setText(getTranslation(CONSTANTS_PREFIX + SAVE));
    updateHeader();
  }

  private void updateHeader() {
    if (sample != null) {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, sample.getName()));
      getUI().ifPresent(ui -> {
        WebBrowser browser = ui.getSession().getBrowser();
        boolean unix = browser.isMacOSX() || browser.isLinux();
        if (sample != null) {
          message.setText(getTranslation(MESSAGE_PREFIX + MESSAGE,
              configuration.getUpload().label(sample, unix)));
        }
      });
    } else {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER));
    }
  }

  /**
   * Adds listener to be informed when sample files were saved.
   *
   * @param listener listener
   * @return listener registration
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Registration addSavedListener(
      ComponentEventListener<SavedEvent<AddSampleFilesDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  private void updateFiles() {
    existingFilenames = service.files(sample).stream().map(f -> f.toFile().getName())
        .collect(Collectors.toSet());
    files.setItems(
        service.uploadFiles(sample).stream().map(Path::toFile).collect(Collectors.toList()));
  }

  boolean exists(File file) {
    return existingFilenames.contains(file.getName());
  }

  private Path folder() {
    return configuration.getUpload().folder(sample);
  }

  private boolean validate(Collection<Path> files) {
    error.setVisible(false);
    boolean anyExists = files.stream()
        .anyMatch(file -> exists(file.toFile()) && !overwrite(file.toFile()).getValue());
    if (anyExists) {
      error.setVisible(true);
      error.setText(getTranslation(MESSAGE_PREFIX + OVERWRITE_ERROR));
    }
    return !anyExists;
  }

  void save() {
    Collection<Path> files = service.uploadFiles(sample);
    if (validate(files)) {
      logger.debug("save new files {} for sample {}", files, sample);
      service.saveFiles(sample, files);
      Notification.show(getTranslation(MESSAGE_PREFIX + SAVED, files.size(), sample.getName()));
      fireSavedEvent();
      close();
    }
  }

  long getSampleId() {
    return sample.getId();
  }

  void setSampleId(long id) {
    sample = service.get(id).orElseThrow();
    updateHeader();
    createFolder();
    updateFiles();
  }

  private void createFolder() {
    Path folder = folder();
    try {
      logger.debug("creating upload folder {} for sample {}", folder, sample);
      Files.createDirectories(folder);
    } catch (IOException e) {
      new WarningNotification(getTranslation(MESSAGE_PREFIX + CREATE_FOLDER_ERROR, folder)).open();
    }
  }
}
