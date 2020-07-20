package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

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
  public static final String NETWORK = "network";
  public static final String FILES = "files";
  public static final String FILENAME = "filename";
  public static final String SIZE = "size";
  public static final String SIZE_VALUE = property("size", "value");
  public static final String OVERWRITE = "overwrite";
  public static final String SAVED = "saved";
  public static final String CREATE_FOLDER_ERROR = property("createFolder", "error");
  public static final String OVERWRITE_ERROR = property(OVERWRITE, "error");
  private static final long serialVersionUID = 166699830639260659L;
  protected H3 header = new H3();
  protected Div message = new Div();
  protected Div network = new Div();
  protected Grid<File> files = new Grid<>();
  protected Column<File> filename;
  protected Column<File> size;
  protected Column<File> overwrite;
  protected Checkbox overwriteAll = new Checkbox();
  protected Div error = new Div();
  protected Button save = new Button();
  private Map<File, Checkbox> overwriteFields = new HashMap<>();
  @Autowired
  private transient AddDatasetFilesDialogPresenter presenter;

  protected AddDatasetFilesDialog() {
  }

  protected AddDatasetFilesDialog(AddDatasetFilesDialogPresenter presenter) {
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
    layout.add(header, message, network, files, error, save);
    header.setId(id(HEADER));
    message.setId(id(MESSAGE));
    network.setId(id(NETWORK));
    files.setId(id(FILES));
    filename = files.addColumn(new ComponentRenderer<>(file -> filename(file)), FILENAME)
        .setKey(FILENAME).setComparator(NormalizedComparator.of(file -> file.getName()));
    overwrite = files.addColumn(new ComponentRenderer<>(file -> overwrite(file)), OVERWRITE)
        .setKey(OVERWRITE).setSortable(false);
    files.appendHeaderRow(); // Headers.
    HeaderRow headerRow = files.appendHeaderRow();
    headerRow.getCell(overwrite).setComponent(overwriteAll);
    overwriteAll.addValueChangeListener(e -> {
      if (e.isFromClient()) {
        overwriteFields.values().stream().forEach(c -> c.setValue(e.getValue()));
      }
    });
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save());
    presenter.init(this);
  }

  private Span filename(File file) {
    Span span = new Span();
    span.setText(file.getName());
    if (presenter.exists(file)) {
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
  @SuppressWarnings("unchecked")
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(AddDatasetFilesDialog.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER, 0));
    message.setText("");
    network.setText("");
    filename.setHeader(resources.message(FILENAME));
    if (size != null) {
      files.removeColumn(size);
    }
    NumberFormat sizeFormat = NumberFormat.getIntegerInstance(getLocale());
    size = files.addColumn(file -> {
      return resources.message(SIZE_VALUE, sizeFormat.format(file.length() / Math.pow(1024, 2)));
    }, SIZE).setKey(SIZE);
    files.setColumnOrder(filename, size, overwrite);
    size.setHeader(resources.message(SIZE));
    overwrite.setHeader(resources.message(OVERWRITE));
    save.setText(webResources.message(SAVE));
    presenter.localeChange(getLocale());
    updateHeader();
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(AddDatasetFilesDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getName() != null) {
      header.setText(resources.message(HEADER, dataset.getName()));
    } else {
      header.setText(resources.message(HEADER));
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

  public Dataset getDataset() {
    return presenter.getDataset();
  }

  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset);
    updateHeader();
  }
}
