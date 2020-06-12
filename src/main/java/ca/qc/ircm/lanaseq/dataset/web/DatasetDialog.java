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
import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleProperties;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.TagsField;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetDialog extends Dialog implements LocaleChangeObserver, NotificationComponent {
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String ID = "dataset-dialog";
  public static final String HEADER = "header";
  public static final String SAMPLES_HEADER = "samplesHeader";
  public static final String ADD_SAMPLE = "addSample";
  public static final String SAMPLES = "samples";
  public static final String FILES = "files";
  public static final String FILENAME = "filename";
  public static final String SAVED = "saved";
  public static final String DELETED = "deleted";
  public static final String DELETE_HEADER = property(DELETE, "header");
  public static final String DELETE_MESSAGE = property(DELETE, "message");
  public static final String FILENAME_REGEX = "[\\w-\\.]*";
  public static final String FILENAME_REGEX_ERROR = property("filename", "regex");
  public static final String FILE_RENAME_ERROR = property("filename", "rename", "error");
  public static final String FILE_DELETE_ERROR = property("filename", "delete", "error");
  protected H3 header = new H3();
  protected TagsField tags = new TagsField();
  protected ComboBox<Protocol> protocol = new ComboBox<>();
  protected Select<Assay> assay = new Select<>();
  protected Select<SampleType> type = new Select<>();
  protected TextField target = new TextField();
  protected TextField strain = new TextField();
  protected TextField strainDescription = new TextField();
  protected TextField treatment = new TextField();
  protected H4 samplesHeader = new H4();
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> sampleId;
  protected Column<Sample> sampleReplicate;
  protected Column<Sample> sampleName;
  protected Column<Sample> sampleRemove;
  protected Button addSample = new Button();
  protected Grid<DatasetFile> files = new Grid<>();
  protected Column<DatasetFile> filename;
  protected Column<DatasetFile> sample;
  protected Column<DatasetFile> deleteFile;
  protected TextField filenameEdit = new TextField();
  protected Button save = new Button();
  protected Button cancel = new Button();
  protected Button delete = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  @Autowired
  private transient DatasetDialogPresenter presenter;
  private Map<Sample, TextField> sampleIdFields = new HashMap<>();
  private Map<Sample, TextField> sampleReplicateFields = new HashMap<>();

  protected DatasetDialog() {
  }

  protected DatasetDialog(DatasetDialogPresenter presenter) {
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
    FormLayout datasetForm = new FormLayout(protocol, assay, type);
    datasetForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout strainForm = new FormLayout(target, strain, strainDescription, treatment);
    strainForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout form = new FormLayout(datasetForm, strainForm);
    form.setResponsiveSteps(new ResponsiveStep("30em", 1), new ResponsiveStep("30em", 2));
    HorizontalLayout endButtons = new HorizontalLayout(delete);
    endButtons.setJustifyContentMode(JustifyContentMode.END);
    endButtons.setWidthFull();
    HorizontalLayout buttons = new HorizontalLayout(new HorizontalLayout(save, cancel), endButtons);
    buttons.setWidthFull();
    layout.add(header, tags, form, samplesHeader, samples, files, buttons);
    header.setId(id(HEADER));
    tags.setId(id(TAGS));
    protocol.setId(id(PROTOCOL));
    protocol.setItemLabelGenerator(Protocol::getName);
    protocol.setPreventInvalidInput(true);
    assay.setId(id(ASSAY));
    assay.setItemLabelGenerator(a -> a.getLabel(getLocale()));
    assay.setItems(Assay.values());
    type.setId(id(TYPE));
    type.setItemLabelGenerator(t -> t.getLabel(getLocale()));
    type.setItems(SampleType.values());
    target.setId(id(TARGET));
    strain.setId(id(STRAIN));
    strainDescription.setId(id(STRAIN_DESCRIPTION));
    treatment.setId(id(TREATMENT));
    samplesHeader.setId(id(SAMPLES_HEADER));
    samples.setId(id(SAMPLES));
    samples.setHeight("14em");
    sampleId = samples
        .addColumn(new ComponentRenderer<>(sample -> sampleIdField(sample)),
            SampleProperties.SAMPLE_ID)
        .setKey(SampleProperties.SAMPLE_ID)
        .setComparator(NormalizedComparator.of(sample -> sample.getSampleId()));
    sampleReplicate = samples
        .addColumn(new ComponentRenderer<>(sample -> sampleReplicateField(sample)),
            SampleProperties.REPLICATE)
        .setKey(SampleProperties.REPLICATE)
        .setComparator(NormalizedComparator.of(sample -> sample.getReplicate()));
    sampleName = samples.addColumn(sample -> sample.getName(), SampleProperties.NAME)
        .setKey(SampleProperties.NAME)
        .setComparator(NormalizedComparator.of(sample -> sample.getName()));
    sampleRemove =
        samples.addColumn(new ComponentRenderer<>(sample -> sampleDelete(sample)), REMOVE)
            .setKey(REMOVE).setSortable(false);
    FooterRow footer = samples.appendFooterRow();
    footer.getCell(sampleId).setComponent(addSample);
    addSample.setId(id(ADD_SAMPLE));
    addSample.setIcon(VaadinIcon.PLUS.create());
    addSample.addClickListener(e -> presenter.addSample(getLocale()));
    files.setId(id(FILES));
    files.setHeight("12em");
    files.getEditor().addCloseListener(e -> presenter.rename(e.getItem(), getLocale()));
    files.addItemDoubleClickListener(e -> {
      files.getEditor().editItem(e.getItem());
      filenameEdit.focus();
    });
    filename = files.addColumn(file -> file.getFilename(), FILENAME).setKey(FILENAME)
        .setComparator(NormalizedComparator.of(file -> file.getFilename()));
    deleteFile =
        files.addColumn(new ComponentRenderer<>(file -> deleteButton(file)), DELETE).setKey(DELETE);
    filename.setEditorComponent(filenameEdit);
    filenameEdit.setId(id(FILENAME));
    filenameEdit.addKeyDownListener(Key.ENTER, e -> files.getEditor().closeEditor());
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save(getLocale()));
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    delete.setId(id(DELETE));
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    delete.setIcon(VaadinIcon.TRASH.create());
    delete.addClickListener(e -> confirm.open());
    confirm.setCancelable(true);
    confirm.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName() + " "
        + ButtonVariant.LUMO_PRIMARY.getVariantName());
    confirm.addConfirmListener(e -> presenter.delete(getLocale()));
    presenter.init(this);
  }

  TextField sampleIdField(Sample sample) {
    if (sampleIdFields.containsKey(sample)) {
      return sampleIdFields.get(sample);
    }
    TextField field = new TextField();
    field.addClassName(SampleProperties.SAMPLE_ID);
    sampleIdFields.put(sample, field);
    return field;
  }

  TextField sampleReplicateField(Sample sample) {
    if (sampleReplicateFields.containsKey(sample)) {
      return sampleReplicateFields.get(sample);
    }
    TextField field = new TextField();
    field.addClassName(SampleProperties.REPLICATE);
    sampleReplicateFields.put(sample, field);
    return field;
  }

  Button sampleDelete(Sample sample) {
    Button button = new Button();
    button.addClassName(REMOVE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addClickListener(e -> presenter.removeSample(sample));
    return button;
  }

  private Button deleteButton(DatasetFile file) {
    Button button = new Button();
    button.addClassName(DELETE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addThemeVariants(ButtonVariant.LUMO_ERROR);
    button.addClickListener(e -> presenter.deleteFile(file, getLocale()));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    updateHeader();
    tags.setLabel(datasetResources.message(TAGS));
    protocol.setLabel(sampleResources.message(PROTOCOL));
    assay.setLabel(sampleResources.message(ASSAY));
    type.setLabel(sampleResources.message(TYPE));
    target.setLabel(sampleResources.message(TARGET));
    target.setPlaceholder(sampleResources.message(property(TARGET, PLACEHOLDER)));
    strain.setLabel(sampleResources.message(STRAIN));
    strain.setPlaceholder(sampleResources.message(property(STRAIN, PLACEHOLDER)));
    strainDescription.setLabel(sampleResources.message(STRAIN_DESCRIPTION));
    strainDescription
        .setPlaceholder(sampleResources.message(property(STRAIN_DESCRIPTION, PLACEHOLDER)));
    treatment.setLabel(sampleResources.message(TREATMENT));
    treatment.setPlaceholder(sampleResources.message(property(TREATMENT, PLACEHOLDER)));
    samplesHeader.setText(resources.message(SAMPLES_HEADER));
    sampleId.setHeader(sampleResources.message(SampleProperties.SAMPLE_ID));
    sampleReplicate.setHeader(sampleResources.message(SampleProperties.REPLICATE));
    sampleName.setHeader(sampleResources.message(SampleProperties.NAME));
    addSample.setText(webResources.message(ADD));
    filename.setHeader(resources.message(FILENAME));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    delete.setText(webResources.message(DELETE));
    confirm.setHeader(resources.message(DELETE_HEADER));
    confirm.setConfirmText(webResources.message(DELETE));
    confirm.setCancelText(webResources.message(CANCEL));
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getId() != null) {
      header.setText(resources.message(HEADER, 1, dataset.getName()));
      confirm.setText(resources.message(DELETE_MESSAGE, dataset.getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
  }

  /**
   * Adds listener to be informed when an dataset was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration addSavedListener(ComponentEventListener<SavedEvent<DatasetDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  /**
   * Adds listener to be informed when an dataset was deleted.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration
      addDeletedListener(ComponentEventListener<DeletedEvent<DatasetDialog>> listener) {
    return addListener((Class) DeletedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  void fireDeletedEvent() {
    fireEvent(new DeletedEvent<>(this, true));
  }

  public Dataset getDataset() {
    return presenter.getDataset();
  }

  /**
   * Sets dataset.
   *
   * @param dataset
   *          dataset
   */
  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset, getLocale());
    updateHeader();
  }

  public static class DatasetFile {
    private Path path;
    private String filename;

    DatasetFile(Path path) {
      this.path = path;
      filename = path.getFileName().toString();
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
