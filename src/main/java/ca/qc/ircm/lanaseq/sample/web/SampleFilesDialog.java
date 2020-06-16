package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
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
 * Sample dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleFilesDialog extends Dialog
    implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "sample-files-dialog";
  public static final String HEADER = "header";
  public static final String FILES = "files";
  public static final String FILENAME = "filename";
  public static final String FILENAME_REGEX = "[\\w-\\.]*";
  public static final String FILENAME_REGEX_ERROR = property("filename", "regex");
  public static final String FILE_RENAME_ERROR = property("filename", "rename", "error");
  public static final String FILE_DELETE_ERROR = property("filename", "delete", "error");
  private static final long serialVersionUID = 166699830639260659L;
  protected H3 header = new H3();
  protected Grid<SampleFile> files = new Grid<>();
  protected Column<SampleFile> filename;
  protected Column<SampleFile> delete;
  protected TextField filenameEdit = new TextField();
  protected Button add = new Button();
  @Autowired
  protected AddSampleFilesDialog addFilesDialog;
  @Autowired
  private transient SampleFilesDialogPresenter presenter;

  protected SampleFilesDialog() {
  }

  protected SampleFilesDialog(AddSampleFilesDialog addFilesDialog,
      SampleFilesDialogPresenter presenter) {
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
    layout.add(header, files, add);
    header.setId(id(HEADER));
    files.setId(id(FILES));
    files.setSizeFull();
    files.getEditor().addCloseListener(e -> presenter.rename(e.getItem(), getLocale()));
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

  private Button deleteButton(SampleFile file) {
    Button button = new Button();
    button.addClassName(DELETE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addThemeVariants(ButtonVariant.LUMO_ERROR);
    button.addClickListener(e -> presenter.deleteFile(file, getLocale()));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(SampleFilesDialog.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER, 0));
    filename.setHeader(resources.message(FILENAME));
    delete.setHeader(webResources.message(DELETE));
    add.setText(webResources.message(ADD));
    updateHeader();
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(SampleFilesDialog.class, getLocale());
    Sample sample = presenter.getSample();
    if (sample != null && sample.getName() != null) {
      header.setText(resources.message(HEADER, sample.getName()));
    } else {
      header.setText(resources.message(HEADER));
    }
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
      addSavedListener(ComponentEventListener<SavedEvent<SampleFilesDialog>> listener) {
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
      addDeletedListener(ComponentEventListener<DeletedEvent<SampleFilesDialog>> listener) {
    return addListener((Class) DeletedEvent.class, listener);
  }

  void fireDeletedEvent() {
    fireEvent(new DeletedEvent<>(this, true));
  }

  public Sample getSample() {
    return presenter.getSample();
  }

  public void setSample(Sample sample) {
    presenter.setSample(sample);
    updateHeader();
  }

  public static class SampleFile {
    private Path path;
    private String filename;

    SampleFile(Path path) {
      this.path = path;
      filename = path.getFileName().toString();
    }

    @Override
    public String toString() {
      return "SampleFile [path=" + path + ", filename=" + filename + "]";
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
