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

import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UIDetachedException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Add dataset files dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AddDatasetFilesDialog extends Dialog
    implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "add-dataset-files-dialog";
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
  private static final String MESSAGE_PREFIX = messagePrefix(AddDatasetFilesDialog.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private static final long serialVersionUID = 166699830639260659L;
  private static final Logger logger = LoggerFactory.getLogger(AddDatasetFilesDialog.class);
  protected Div message = new Div();
  protected Grid<File> files = new Grid<>();
  protected Column<File> filename;
  protected Column<File> size;
  protected Column<File> overwrite;
  protected Checkbox overwriteAll = new Checkbox();
  protected Div error = new Div();
  protected Button save = new Button();
  private Map<File, Checkbox> overwriteFields = new HashMap<>();
  private Dataset dataset;
  private Set<String> existingFilenames = new HashSet<>();
  private transient Thread updateFilesThread;
  private transient DatasetService service;
  private transient AppConfiguration configuration;

  @Autowired
  protected AddDatasetFilesDialog(DatasetService service, AppConfiguration configuration) {
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
    setResizable(true);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(message, files, error);
    layout.setSizeFull();
    layout.expand(files);
    getFooter().add(save);
    message.setId(id(MESSAGE));
    files.setId(id(FILES));
    filename = files.addColumn(new ComponentRenderer<>(file -> filename(file))).setKey(FILENAME)
        .setSortProperty(FILENAME).setComparator(NormalizedComparator.of(file -> file.getName()))
        .setFlexGrow(10);
    overwrite = files.addColumn(new ComponentRenderer<>(file -> overwrite(file))).setKey(OVERWRITE)
        .setSortable(false);
    files.appendHeaderRow(); // Headers.
    HeaderRow headerRow = files.appendHeaderRow();
    headerRow.getCell(overwrite).setComponent(overwriteAll);
    overwriteAll.addValueChangeListener(
        e -> overwriteFields.values().stream().forEach(c -> c.setValue(e.getValue())));
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    addOpenedChangeListener(event -> {
      if (event.isOpened()) {
        if (updateFilesThread != null) {
          updateFilesThread.interrupt();
        }
        updateFilesThread = createUpdateFilesThread();
        updateFilesThread.start();
      } else {
        if (updateFilesThread != null) {
          updateFilesThread.interrupt();
          try {
            updateFilesThread.join(5000);
          } catch (InterruptedException e) {
            // Assume interrupted.
          }
        }
        files.setItems(new ArrayList<>());
      }
    });
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
    size = files.addColumn(file -> {
      return getTranslation(MESSAGE_PREFIX + SIZE_VALUE,
          sizeFormat.format(file.length() / Math.pow(1024, 2)));
    }, SIZE).setKey(SIZE);
    files.setColumnOrder(filename, size, overwrite);
    size.setHeader(getTranslation(MESSAGE_PREFIX + SIZE));
    overwrite.setHeader(getTranslation(MESSAGE_PREFIX + OVERWRITE));
    save.setText(getTranslation(CONSTANTS_PREFIX + SAVE));
    updateHeader();
  }

  private void updateHeader() {
    if (dataset != null && dataset.getName() != null) {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, dataset.getName()));
      getUI().ifPresent(ui -> {
        WebBrowser browser = ui.getSession().getBrowser();
        boolean unix = browser.isMacOSX() || browser.isLinux();
        if (dataset != null) {
          message.setText(getTranslation(MESSAGE_PREFIX + MESSAGE,
              configuration.getUpload().label(dataset, unix)));
        }
      });
    } else {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER));
    }
  }

  /**
   * Adds listener to be informed when sample files were saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addSavedListener(ComponentEventListener<SavedEvent<AddDatasetFilesDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  private Thread createUpdateFilesThread() {
    Runnable updateFilesRunnable = () -> {
      logger.debug("start checking files in dataset upload folder {}", folder());
      while (true) {
        getUI().ifPresent(ui -> {
          try {
            ui.access(() -> {
              updateFiles();
              ui.push();
            });
          } catch (IllegalStateException | UIDetachedException e) {
            return;
          }
        });
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          logger.debug("stop checking files in dataset upload folder {}", folder());
          return;
        }
      }
    };
    DelegatingSecurityContextRunnable wrappedRunnable = new DelegatingSecurityContextRunnable(
        updateFilesRunnable, SecurityContextHolder.getContext());
    Thread thread = new Thread(wrappedRunnable);
    thread.setDaemon(true);
    return thread;
  }

  void updateFiles() {
    existingFilenames = service.files(dataset).stream().map(file -> file.toFile().getName())
        .collect(Collectors.toSet());
    files.setItems(service.uploadFiles(dataset).stream().map(file -> file.toFile())
        .collect(Collectors.toList()));
  }

  boolean exists(File file) {
    return existingFilenames.contains(file.getName());
  }

  private Path folder() {
    return dataset != null ? configuration.getUpload().folder(dataset) : null;
  }

  private boolean validate(Collection<Path> files) {
    error.setVisible(false);
    boolean anyExists =
        files.stream().filter(file -> exists(file.toFile()) && !overwrite(file.toFile()).getValue())
            .findAny().isPresent();
    if (anyExists) {
      error.setVisible(true);
      error.setText(getTranslation(MESSAGE_PREFIX + OVERWRITE_ERROR));
    }
    return !anyExists;
  }

  void save() {
    Collection<Path> files = service.uploadFiles(dataset);
    if (validate(files)) {
      logger.debug("save new files {} for dataset {}", files, dataset);
      service.saveFiles(dataset, files);
      showNotification(getTranslation(MESSAGE_PREFIX + SAVED, files.size(), dataset.getName()));
      fireSavedEvent();
      close();
    }
  }

  Long getDatasetId() {
    return dataset.getId();
  }

  void setDatasetId(Long id) {
    dataset = service.get(id).orElseThrow();
    updateHeader();
    createFolder();
    updateFiles();
  }

  private void createFolder() {
    Path folder = folder();
    if (folder != null) {
      try {
        logger.debug("creating upload folder {} for dataset {}", folder, dataset);
        Files.createDirectories(folder);
      } catch (IOException e) {
        showNotification(getTranslation(MESSAGE_PREFIX + CREATE_FOLDER_ERROR, folder));
      }
    }
  }

  Thread updateFilesThread() {
    return updateFilesThread;
  }
}
