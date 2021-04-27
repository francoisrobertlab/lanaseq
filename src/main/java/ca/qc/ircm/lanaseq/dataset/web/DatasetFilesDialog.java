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

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.SAMPLES;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
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
  public static final String FILE_COUNT = "fileCount";
  private static final long serialVersionUID = 166699830639260659L;
  protected H3 header = new H3();
  protected Div message = new Div();
  protected Grid<EditableFile> files = new Grid<>();
  protected Column<EditableFile> filename;
  protected Column<EditableFile> download;
  protected Column<EditableFile> delete;
  protected TextField filenameEdit = new TextField();
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> fileCount;
  protected Button add = new Button();
  @Autowired
  protected AddDatasetFilesDialog addFilesDialog;
  @Autowired
  protected SampleFilesDialog sampleFilesDialog;
  @Autowired
  private transient DatasetFilesDialogPresenter presenter;

  protected DatasetFilesDialog() {
  }

  protected DatasetFilesDialog(DatasetFilesDialogPresenter presenter,
      AddDatasetFilesDialog addFilesDialog, SampleFilesDialog sampleFilesDialog) {
    this.addFilesDialog = addFilesDialog;
    this.sampleFilesDialog = sampleFilesDialog;
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
    layout.add(header, message, files, samples, add);
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
    download = files.addColumn(new ComponentRenderer<>(file -> downloadButton(file)), DOWNLOAD)
        .setKey(DOWNLOAD).setSortable(false);
    delete = files.addColumn(new ComponentRenderer<>(file -> deleteButton(file)), DELETE)
        .setKey(DELETE).setSortable(false);
    filename.setEditorComponent(filenameEdit);
    filenameEdit.setId(id(FILENAME));
    filenameEdit.addKeyDownListener(Key.ENTER, e -> files.getEditor().closeEditor());
    samples.setId(id(SAMPLES));
    samples.addItemDoubleClickListener(e -> presenter.viewFiles(e.getItem()));
    name = samples.addColumn(sa -> sa.getName(), NAME).setKey(NAME).setWidth("35em");
    fileCount = samples.addColumn(sa -> presenter.fileCount(sa), FILE_COUNT).setKey(FILE_COUNT);
    add.setId(id(ADD));
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> presenter.add());
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
    button.addClickListener(e -> presenter.deleteFile(file));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetFilesDialog.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER, 0));
    message.setText("");
    message.setTitle("");
    filename.setHeader(resources.message(FILENAME));
    download.setHeader(webResources.message(DOWNLOAD));
    delete.setHeader(webResources.message(DELETE));
    name.setHeader(sampleResources.message(NAME));
    fileCount.setHeader(resources.message(FILE_COUNT));
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

  public Dataset getDataset() {
    return presenter.getDataset();
  }

  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset);
    updateHeader();
  }
}
