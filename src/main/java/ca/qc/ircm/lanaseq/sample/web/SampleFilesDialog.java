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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.uploadI18N;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Sample files dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleFilesDialog extends Dialog
    implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "sample-files-dialog";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String FOLDERS = "folders";
  public static final String FILES = "files";
  public static final String FILENAME = "filename";
  public static final String FILENAME_REGEX = "[\\w-\\.]*";
  public static final String FILENAME_REGEX_ERROR = property("filename", "regex");
  public static final String FILE_RENAME_ERROR = property("filename", "rename", "error");
  public static final String ADD_LARGE_FILES = "addLargeFiles";
  public static final String FILES_IOEXCEPTION = property(FILES, "ioexception");
  public static final String FILES_SUCCESS = property(FILES, "success");
  public static final int MAXIMUM_SMALL_FILES_SIZE = 20 * 1024 * 1024; // 20MB
  public static final int MAXIMUM_SMALL_FILES_COUNT = 50;
  private static final long serialVersionUID = 166699830639260659L;
  protected H3 header = new H3();
  protected Div message = new Div();
  protected VerticalLayout folders = new VerticalLayout();
  protected Grid<EditableFile> files = new Grid<>();
  protected Column<EditableFile> filename;
  protected Column<EditableFile> download;
  protected Column<EditableFile> delete;
  protected TextField filenameEdit = new TextField();
  protected MultiFileMemoryBuffer uploadBuffer = new MultiFileMemoryBuffer();
  protected Upload upload = new Upload(uploadBuffer);
  protected Button addLargeFiles = new Button();
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
    setWidth("1000px");
    setHeight("700px");
    setResizable(true);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    VerticalLayout messageAndFolders = new VerticalLayout(message, folders);
    messageAndFolders.setSpacing(false);
    messageAndFolders.setPadding(false);
    HorizontalLayout buttonsLayout = new HorizontalLayout();
    layout.add(header, messageAndFolders, files, buttonsLayout);
    buttonsLayout.add(upload, addLargeFiles);
    buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
    layout.setSizeFull();
    layout.expand(files);
    header.setId(id(HEADER));
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
    filename =
        files.addColumn(file -> file.getFilename(), FILENAME).setKey(FILENAME).setFlexGrow(10);
    download = files.addColumn(new ComponentRenderer<>(file -> downloadButton(file)), DOWNLOAD)
        .setKey(DOWNLOAD).setSortable(false);
    delete = files.addColumn(new ComponentRenderer<>(file -> deleteButton(file)), DELETE)
        .setKey(DELETE).setSortable(false);
    filename.setEditorComponent(filenameEdit);
    filenameEdit.setId(id(FILENAME));
    filenameEdit.addKeyDownListener(Key.ENTER, e -> files.getEditor().closeEditor());
    upload.setId(id(UPLOAD));
    upload.setMaxFileSize(MAXIMUM_SMALL_FILES_SIZE);
    upload.setMaxFiles(MAXIMUM_SMALL_FILES_COUNT);
    upload.setMaxHeight("2.5em"); // Hide name of uploaded files.
    upload.addSucceededListener(event -> presenter.addSmallFile(event.getFileName(),
        uploadBuffer.getInputStream(event.getFileName())));
    addLargeFiles.setId(id(ADD_LARGE_FILES));
    addLargeFiles.setIcon(VaadinIcon.PLUS.create());
    addLargeFiles.addClickListener(e -> presenter.addLargeFiles());
    presenter.init(this);
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
    final AppResources resources = new AppResources(SampleFilesDialog.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER, 0));
    message.setText("");
    message.setTitle("");
    filename.setHeader(resources.message(FILENAME));
    download.setHeader(webResources.message(DOWNLOAD));
    delete.setHeader(webResources.message(DELETE));
    addLargeFiles.setText(resources.message(ADD_LARGE_FILES));
    upload.setI18n(uploadI18N(getLocale()));
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

  public Sample getSample() {
    return presenter.getSample();
  }

  public void setSample(Sample sample) {
    presenter.setSample(sample);
    updateHeader();
  }
}
