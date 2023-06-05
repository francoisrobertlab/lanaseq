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

import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.SAMPLES;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.uploadI18N;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog;
import ca.qc.ircm.lanaseq.web.EditableFile;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Comparator;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectFactory;
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
  public static final String FOLDERS = "folders";
  public static final String FILES = "files";
  public static final String FILENAME = "filename";
  public static final String FILENAME_REGEX = "[\\w-\\.]*";
  public static final String FILENAME_REGEX_ERROR = property("filename", "regex");
  public static final String FILE_RENAME_ERROR = property("filename", "rename", "error");
  public static final String FILE_COUNT = "fileCount";
  public static final String ADD_LARGE_FILES = "addLargeFiles";
  public static final String FILES_IOEXCEPTION = property(FILES, "ioexception");
  public static final String FILES_SUCCESS = property(FILES, "success");
  public static final int MAXIMUM_SMALL_FILES_SIZE = 200 * 1024 * 1024; // 200MB
  public static final int MAXIMUM_SMALL_FILES_COUNT = 50;
  public static final String FILENAME_HTML = "<span title='${item.title}'>${item.filename}</span>";
  private static final long serialVersionUID = 166699830639260659L;
  protected Div message = new Div();
  protected VerticalLayout folders = new VerticalLayout();
  protected Grid<EditableFile> files = new Grid<>();
  protected Column<EditableFile> filename;
  protected Column<EditableFile> download;
  protected Column<EditableFile> delete;
  protected TextField filenameEdit = new TextField();
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> fileCount;
  protected MultiFileMemoryBuffer uploadBuffer = new MultiFileMemoryBuffer();
  protected Upload upload = new Upload(uploadBuffer);
  protected Button addLargeFiles = new Button();
  @Autowired
  protected ObjectFactory<AddDatasetFilesDialog> addFilesDialogFactory;
  @Autowired
  protected ObjectFactory<SampleFilesDialog> sampleFilesDialogFactory;
  @Autowired
  private transient DatasetFilesDialogPresenter presenter;

  protected DatasetFilesDialog() {
  }

  protected DatasetFilesDialog(DatasetFilesDialogPresenter presenter,
      ObjectFactory<AddDatasetFilesDialog> addFilesDialogFactory,
      ObjectFactory<SampleFilesDialog> sampleFilesDialogFactory) {
    this.addFilesDialogFactory = addFilesDialogFactory;
    this.sampleFilesDialogFactory = sampleFilesDialogFactory;
    this.presenter = presenter;
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
    VerticalLayout messageAndFolders = new VerticalLayout(message, folders);
    messageAndFolders.setSpacing(false);
    messageAndFolders.setPadding(false);
    layout.add(messageAndFolders, files, samples);
    layout.setSizeFull();
    layout.expand(files);
    getFooter().add(upload, addLargeFiles);
    message.setId(id(MESSAGE));
    folders.setId(id(FOLDERS));
    folders.setPadding(false);
    folders.setSpacing(false);
    files.setId(id(FILES));
    files.getEditor().addCloseListener(e -> presenter.rename(e.getItem()));
    files.addItemDoubleClickListener(e -> {
      files.getEditor().editItem(e.getItem());
      filenameEdit.focus();
    });
    filename = files
        .addColumn(LitRenderer.<EditableFile>of(FILENAME_HTML)
            .withProperty("filename", file -> shortFilename(file.getFilename()))
            .withProperty("title", file -> file.getFilename()))
        .setKey(FILENAME).setComparator(Comparator.comparing(EditableFile::getFilename))
        .setFlexGrow(10);
    download = files.addColumn(new ComponentRenderer<>(file -> downloadButton(file)))
        .setKey(DOWNLOAD).setSortable(false);
    delete = files.addColumn(new ComponentRenderer<>(file -> deleteButton(file))).setKey(DELETE)
        .setSortable(false);
    filename.setEditorComponent(filenameEdit);
    filenameEdit.setId(id(FILENAME));
    filenameEdit.addKeyDownListener(Key.ENTER, e -> files.getEditor().closeEditor());
    samples.setId(id(SAMPLES));
    samples.addItemDoubleClickListener(e -> presenter.viewFiles(e.getItem()));
    name = samples.addColumn(sa -> sa.getName(), NAME).setKey(NAME).setFlexGrow(10);
    fileCount = samples.addColumn(sa -> presenter.fileCount(sa), FILE_COUNT).setKey(FILE_COUNT);
    upload.setId(id(UPLOAD));
    upload.setMaxFileSize(MAXIMUM_SMALL_FILES_SIZE);
    upload.setMaxFiles(MAXIMUM_SMALL_FILES_COUNT);
    upload.setMaxHeight("5em"); // Hide name of uploaded files.
    upload.addSucceededListener(event -> presenter.addSmallFile(event.getFileName(),
        uploadBuffer.getInputStream(event.getFileName())));
    addLargeFiles.setId(id(ADD_LARGE_FILES));
    addLargeFiles.setIcon(VaadinIcon.PLUS.create());
    addLargeFiles.addClickListener(e -> presenter.addLargeFiles());
    presenter.init(this);
  }

  private String shortFilename(String filename) {
    String name = Optional.ofNullable(presenter.getDataset()).map(Dataset::getName).orElse("");
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
    anchor.setHref(presenter.download(file));
    Button button = new Button();
    anchor.add(button);
    button.setIcon(VaadinIcon.DOWNLOAD.create());
    return anchor;
  }

  private Button deleteButton(EditableFile file) {
    Button button = new Button();
    button.addClassName(DELETE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addThemeVariants(ButtonVariant.LUMO_ERROR);
    button.setEnabled(!presenter.isArchive(file));
    button.addClickListener(e -> presenter.deleteFile(file));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetFilesDialog.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    setHeaderTitle(resources.message(HEADER, 0));
    message.setText("");
    message.setTitle("");
    filename.setHeader(resources.message(FILENAME));
    download.setHeader(webResources.message(DOWNLOAD));
    delete.setHeader(webResources.message(DELETE));
    name.setHeader(sampleResources.message(NAME));
    fileCount.setHeader(resources.message(FILE_COUNT));
    upload.setI18n(uploadI18N(getLocale()));
    addLargeFiles.setText(resources.message(ADD_LARGE_FILES));
    updateHeader();
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(DatasetFilesDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getName() != null) {
      setHeaderTitle(resources.message(HEADER, dataset.getName()));
    } else {
      setHeaderTitle(resources.message(HEADER));
    }
  }

  public Dataset getDataset() {
    return presenter.getDataset();
  }

  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset);
    updateHeader();
  }
}
