package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetFilesDialog extends Dialog
    implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "dataset-files-dialog";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String MESSAGE_TITLE = property(MESSAGE, "title");
  public static final String FILES = "files";
  public static final String FILENAME = "filename";
  public static final String FILENAME_REGEX = "[\\w-\\.]*";
  public static final String FILENAME_REGEX_ERROR = property("filename", "regex");
  public static final String FILE_RENAME_ERROR = property("filename", "rename", "error");
  private static final long serialVersionUID = 166699830639260659L;
  protected H3 header = new H3();
  protected Div message = new Div();
  protected Grid<DatasetFile> files = new Grid<>();
  protected Column<DatasetFile> filename;
  protected Column<DatasetFile> delete;
  protected TextField filenameEdit = new TextField();
  protected Button add = new Button();
  @Autowired
  protected AddDatasetFilesDialog addFilesDialog;
  @Autowired
  private transient DatasetFilesDialogPresenter presenter;

  protected DatasetFilesDialog() {
  }

  protected DatasetFilesDialog(AddDatasetFilesDialog addFilesDialog,
      DatasetFilesDialogPresenter presenter) {
    this.addFilesDialog = addFilesDialog;
    this.presenter = presenter;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.setMaxWidth("60em");
    layout.setMinWidth("22em");
    layout.setHeight("40em");
    layout.add(header, message, files, add, addFilesDialog);
    header.setId(id(HEADER));
    message.setId(id(MESSAGE));
    files.setId(id(FILES));
    files.setSizeFull();
    files.getEditor().addCloseListener(e -> presenter.rename(e.getItem()));
    files.addItemDoubleClickListener(e -> {
      files.getEditor().editItem(e.getItem());
      filenameEdit.focus();
    });
    filename =
        files.addColumn(file -> file.getFilename(), FILENAME).setKey(FILENAME).setWidth("35em");
    delete =
        files.addColumn(new ComponentRenderer<>(file -> deleteButton(file)), DELETE).setKey(DELETE);
    filename.setEditorComponent(filenameEdit);
    filenameEdit.setId(id(FILENAME));
    filenameEdit.addKeyDownListener(Key.ENTER, e -> files.getEditor().closeEditor());
    add.setId(id(ADD));
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> presenter.add());
    presenter.init(this);
  }

  private Button deleteButton(DatasetFile file) {
    Button button = new Button();
    button.addClassName(DELETE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addThemeVariants(ButtonVariant.LUMO_ERROR);
    button.addClickListener(e -> presenter.deleteFile(file));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetFilesDialog.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER, 0));
    message.setText("");
    message.setTitle("");
    filename.setHeader(resources.message(FILENAME));
    delete.setHeader(webResources.message(DELETE));
    add.setText(webResources.message(ADD));
    updateHeader();
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(DatasetFilesDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getName() != null) {
      header.setText(resources.message(HEADER, dataset.getName()));
    } else {
      header.setText(resources.message(HEADER));
    }
  }

  /**
   * Adds listener to be informed when a dataset was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addSavedListener(ComponentEventListener<SavedEvent<DatasetFilesDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  /**
   * Adds listener to be informed when a dataset was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addDeletedListener(ComponentEventListener<DeletedEvent<DatasetFilesDialog>> listener) {
    return addListener((Class) DeletedEvent.class, listener);
  }

  void fireDeletedEvent() {
    fireEvent(new DeletedEvent<>(this, true));
  }

  public Dataset getDataset() {
    return presenter.getDataset();
  }

  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset);
    updateHeader();
  }

  public static class DatasetFile {
    private Path path;
    private String filename;

    DatasetFile(Path path) {
      this.path = path;
      filename = path.getFileName().toString();
    }

    @Override
    public String toString() {
      return "DatasetFile [path=" + path + ", filename=" + filename + "]";
    }

    public Path getPath() {
      return path;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }
  }
}
