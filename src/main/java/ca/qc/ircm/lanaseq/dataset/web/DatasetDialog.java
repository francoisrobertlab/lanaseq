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

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleProperties;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.TagsField;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static ca.qc.ircm.lanaseq.Constants.*;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NOTE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.*;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.*;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.datePickerI18n;

/**
 * Dataset dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetDialog extends Dialog implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "dataset-dialog";
  public static final String HEADER = "header";
  public static final String NAME_PREFIX = "namePrefix";
  public static final String NAME_PREFIX_REGEX = "[\\w-\\.]*";
  public static final String NAME_PREFIX_REGEX_ERROR = "namePrefix.regex";
  public static final String GENERATE_NAME = "generateName";
  public static final String ADD_NEW_SAMPLE = "addNewSample";
  public static final String ADD_SAMPLE = "addSample";
  public static final String SAMPLES = "samples";
  public static final String SAVED = "saved";
  public static final String DELETED = "deleted";
  public static final String DELETE_HEADER = property(DELETE, "header");
  public static final String DELETE_MESSAGE = property(DELETE, "message");
  private static final long serialVersionUID = 3285639770914046262L;
  protected TextField namePrefix = new TextField();
  protected Button generateName = new Button();
  protected TagsField tags = new TagsField();
  protected TextField protocol = new TextField();
  protected TextField assay = new TextField();
  protected TextField type = new TextField();
  protected TextField target = new TextField();
  protected TextField strain = new TextField();
  protected TextField strainDescription = new TextField();
  protected TextField treatment = new TextField();
  protected TextArea note = new TextArea();
  protected DatePicker date = new DatePicker();
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> sampleName;
  protected Column<Sample> sampleRemove;
  protected Button addSample = new Button();
  protected Div error = new Div();
  protected Button save = new Button();
  protected Button cancel = new Button();
  protected Button delete = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  @Autowired
  protected ObjectFactory<SelectSampleDialog> selectSampleDialogFactory;
  @Autowired
  private transient DatasetDialogPresenter presenter;
  private Map<Sample, Label> sampleIdFields = new HashMap<>();
  private Map<Sample, Label> sampleReplicateFields = new HashMap<>();
  private Sample draggedSample;

  protected DatasetDialog() {
  }

  protected DatasetDialog(DatasetDialogPresenter presenter,
      ObjectFactory<SelectSampleDialog> selectSampleDialogFactory) {
    this.presenter = presenter;
    this.selectSampleDialogFactory = selectSampleDialogFactory;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    setWidth("1000px");
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    FormLayout datasetForm = new FormLayout(namePrefix, generateName, date, tags, note);
    datasetForm.setResponsiveSteps(new ResponsiveStep("30em", 1), new ResponsiveStep("15em", 4));
    datasetForm.setColspan(namePrefix, 3);
    datasetForm.setColspan(tags, 3);
    datasetForm.setColspan(note, 4);
    FormLayout sampleForm = new FormLayout(protocol, assay, type, target);
    sampleForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout strainForm = new FormLayout(strain, strainDescription, treatment);
    strainForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout form = new FormLayout(sampleForm, strainForm);
    form.setResponsiveSteps(new ResponsiveStep("30em", 1), new ResponsiveStep("30em", 2));
    layout.add(datasetForm, form, samples, error, confirm);
    layout.setSizeFull();
    getFooter().add(delete, cancel, save);
    namePrefix.setId(id(NAME_PREFIX));
    generateName.setId(id(GENERATE_NAME));
    generateName.addClickListener(e -> presenter.generateName());
    tags.setId(id(TAGS));
    protocol.setId(id(PROTOCOL));
    assay.setId(id(ASSAY));
    type.setId(id(TYPE));
    target.setId(id(TARGET));
    strain.setId(id(STRAIN));
    strainDescription.setId(id(STRAIN_DESCRIPTION));
    treatment.setId(id(TREATMENT));
    date.setId(id(DATE));
    note.setId(id(NOTE));
    note.setHeight("6em");
    samples.setId(id(SAMPLES));
    samples.setMinHeight("15em");
    samples.setHeight("15em");
    samples.setSelectionMode(SelectionMode.NONE);
    sampleName = samples.addColumn(sample -> sample.getName(), SampleProperties.NAME)
        .setKey(SampleProperties.NAME)
        .setComparator(NormalizedComparator.of(sample -> sample.getName()));
    sampleRemove = samples.addColumn(new ComponentRenderer<>(sample -> sampleDelete(sample)))
        .setKey(REMOVE).setSortable(false);
    samples.setRowsDraggable(true);
    samples.addDragStartListener(e -> {
      draggedSample = e.getDraggedItems().get(0);
      samples.setDropMode(GridDropMode.BETWEEN);
    });
    samples.addDragEndListener(e -> {
      draggedSample = null;
      samples.setDropMode(null);
    });
    samples.addDropListener(e -> {
      Sample dropped = e.getDropTargetItem().orElse(null);
      if (dropped != null && draggedSample != null) {
        presenter.dropSample(draggedSample, dropped, e.getDropLocation());
      }
    });
    samples.appendFooterRow(); // Footers
    FooterRow footer = samples.appendFooterRow();
    footer.join(footer.getCell(sampleName), footer.getCell(sampleRemove));
    footer.getCell(sampleName).setComponent(new HorizontalLayout(addSample));
    addSample.setId(id(ADD_SAMPLE));
    addSample.setIcon(VaadinIcon.PLUS.create());
    addSample.addClickListener(e -> presenter.addSample());
    error.setId(id(ERROR_TEXT));
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save());
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    delete.setId(id(DELETE));
    delete.getStyle().set("margin-inline-end", "auto");
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    delete.setIcon(VaadinIcon.TRASH.create());
    delete.addClickListener(e -> confirm.open());
    confirm.setId(id(CONFIRM));
    confirm.setCancelable(true);
    confirm.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName() + " "
        + ButtonVariant.LUMO_PRIMARY.getVariantName());
    confirm.addConfirmListener(e -> presenter.delete());
    presenter.init(this);
  }

  Button sampleDelete(Sample sample) {
    Button button = new Button();
    button.addClassName(REMOVE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addClickListener(e -> presenter.removeSample(sample));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    updateHeader();
    namePrefix.setLabel(resources.message(NAME_PREFIX));
    generateName.setText(resources.message(GENERATE_NAME));
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
    date.setLabel(datasetResources.message(DATE));
    date.setI18n(datePickerI18n(getLocale()));
    date.setLocale(Locale.CANADA);
    note.setLabel(datasetResources.message(NOTE));
    String sampleNameHeader = sampleResources.message(SampleProperties.NAME);
    sampleName.setHeader(sampleNameHeader).setFooter(sampleNameHeader);
    addSample.setText(resources.message(ADD_SAMPLE));
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
      setHeaderTitle(resources.message(HEADER, 1, dataset.getName()));
      confirm.setText(resources.message(DELETE_MESSAGE, dataset.getName()));
    } else {
      setHeaderTitle(resources.message(HEADER, 0));
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
    presenter.setDataset(dataset);
    updateHeader();
  }
}
