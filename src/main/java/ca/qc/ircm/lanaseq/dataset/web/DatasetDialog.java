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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NOTE;
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
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.datePickerI18n;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleProperties;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
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
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

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
  public static final String ADD_SAMPLE = "addSample";
  public static final String SAMPLES = "samples";
  public static final String SAVED = "saved";
  public static final String DELETED = "deleted";
  public static final String DELETE_HEADER = property(DELETE, "header");
  public static final String DELETE_MESSAGE = property(DELETE, "message");
  private static final Logger logger = LoggerFactory.getLogger(DatasetDialog.class);
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
  protected ObjectFactory<SelectSampleDialog> selectSampleDialogFactory;
  private Binder<Dataset> binder = new BeanValidationBinder<>(Dataset.class);
  private Sample draggedSample;
  private transient DatasetService service;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected DatasetDialog(ObjectFactory<SelectSampleDialog> selectSampleDialogFactory,
      DatasetService service, AuthenticatedUser authenticatedUser) {
    this.selectSampleDialogFactory = selectSampleDialogFactory;
    this.service = service;
    this.authenticatedUser = authenticatedUser;
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
    generateName.addClickListener(e -> generateName());
    tags.setId(id(TAGS));
    protocol.setId(id(PROTOCOL));
    protocol.setReadOnly(true);
    assay.setId(id(ASSAY));
    assay.setReadOnly(true);
    type.setId(id(TYPE));
    type.setReadOnly(true);
    target.setId(id(TARGET));
    target.setReadOnly(true);
    strain.setId(id(STRAIN));
    strain.setReadOnly(true);
    strainDescription.setId(id(STRAIN_DESCRIPTION));
    strainDescription.setReadOnly(true);
    treatment.setId(id(TREATMENT));
    treatment.setReadOnly(true);
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
        dropSample(draggedSample, dropped, e.getDropLocation());
      }
    });
    samples.appendFooterRow(); // Footers
    FooterRow footer = samples.appendFooterRow();
    footer.join(footer.getCell(sampleName), footer.getCell(sampleRemove));
    footer.getCell(sampleName).setComponent(new HorizontalLayout(addSample));
    addSample.setId(id(ADD_SAMPLE));
    addSample.setIcon(VaadinIcon.PLUS.create());
    addSample.addClickListener(e -> addSample());
    error.setId(id(ERROR_TEXT));
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> close());
    delete.setId(id(DELETE));
    delete.getStyle().set("margin-inline-end", "auto");
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    delete.setIcon(VaadinIcon.TRASH.create());
    delete.addClickListener(e -> confirm.open());
    confirm.setId(id(CONFIRM));
    confirm.setCancelable(true);
    confirm.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName() + " "
        + ButtonVariant.LUMO_PRIMARY.getVariantName());
    confirm.addConfirmListener(e -> delete());
    tags.setTagSuggestions(service.topTags(50));
    error.setVisible(false);
    setDataset(null);
  }

  Button sampleDelete(Sample sample) {
    Button button = new Button();
    button.addClassName(REMOVE);
    button.setIcon(VaadinIcon.TRASH.create());
    button.addClickListener(e -> removeSample(sample));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
    binder.forField(namePrefix).asRequired(webResources.message(REQUIRED))
        .withNullRepresentation("")
        .withValidator(
            new RegexpValidator(resources.message(NAME_PREFIX_REGEX_ERROR), NAME_PREFIX_REGEX))
        .withConverter(
            namePrefix -> namePrefix
                + date.getOptionalValue().map(date -> "_" + dateFormatter.format(date)).orElse(""),
            name -> nameToNamePrefix(name))
        .bind(NAME);
    date.addValueChangeListener(e -> {
      // Force update of dataset name.
      String value = namePrefix.getValue();
      namePrefix.setValue("");
      namePrefix.setValue(value);
    });
    binder.forField(tags).bind(TAGS);
    binder.forField(date).asRequired(webResources.message(REQUIRED)).bind(DATE);
    binder.forField(note).withNullRepresentation("").bind(NOTE);
    namePrefix.setLabel(resources.message(NAME_PREFIX));
    generateName.setText(resources.message(GENERATE_NAME));
    tags.setLabel(datasetResources.message(TAGS));
    protocol.setLabel(sampleResources.message(PROTOCOL));
    assay.setLabel(sampleResources.message(ASSAY));
    type.setLabel(sampleResources.message(TYPE));
    target.setLabel(sampleResources.message(TARGET));
    strain.setLabel(sampleResources.message(STRAIN));
    strainDescription.setLabel(sampleResources.message(STRAIN_DESCRIPTION));
    treatment.setLabel(sampleResources.message(TREATMENT));
    date.setLabel(datasetResources.message(DATE));
    date.setI18n(datePickerI18n(getLocale()));
    date.setLocale(Locale.CANADA);
    note.setLabel(datasetResources.message(NOTE));
    String sampleNameHeader = sampleResources.message(SampleProperties.NAME);
    sampleName.setHeader(sampleNameHeader);
    addSample.setText(resources.message(ADD_SAMPLE));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    delete.setText(webResources.message(DELETE));
    confirm.setHeader(resources.message(DELETE_HEADER));
    confirm.setConfirmText(webResources.message(DELETE));
    confirm.setCancelText(webResources.message(CANCEL));
    updateSamplesFields();
    updateHeader();
  }

  private String nameToNamePrefix(String name) {
    Pattern namePattern = Pattern.compile("(?:(.*)_)?\\d{8}");
    return Optional.ofNullable(name).map(namePattern::matcher).filter(Matcher::matches)
        .map(matcher -> matcher.group(1)).orElse("");
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    Dataset dataset = getDataset();
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

  private void updateSamplesInBinderBean() {
    Dataset dataset = binder.getBean();
    dataset.setSamples(samples.getListDataView().getItems().toList());
  }

  void generateName() {
    updateSamplesInBinderBean();
    Dataset dataset = binder.getBean();
    dataset.generateName();
    namePrefix.setValue(nameToNamePrefix(dataset.getName()));
  }

  void addSample() {
    SelectSampleDialog selectSampleDialog = selectSampleDialogFactory.getObject();
    selectSampleDialog.addSelectedListener(e -> addSample(e.getSelection()));
    selectSampleDialog.open();
  }

  private void addSample(Sample sample) {
    if (!samples.getListDataView().getItems()
        .anyMatch(sa -> sa.getId() != null && sa.getId().equals(sample.getId()))) {
      samples.getListDataView().addItem(sample);
      updateSamplesFields();
    }
  }

  void removeSample(Sample sample) {
    samples.getListDataView().removeItem(sample);
    updateSamplesFields();
  }

  void dropSample(Sample dragged, Sample drop, GridDropLocation dropLocation) {
    if (!dragged.equals(drop)
        && (dropLocation == GridDropLocation.ABOVE || dropLocation == GridDropLocation.BELOW)) {
      samples.getListDataView().removeItem(dragged);
      switch (dropLocation) {
        case ABOVE -> samples.getListDataView().addItemBefore(dragged, drop);
        case BELOW -> samples.getListDataView().addItemAfter(dragged, drop);
      }
      updateSamplesFields();
    }
  }

  BinderValidationStatus<Dataset> validateDataset() {
    return binder.validate();
  }

  private boolean validate() {
    error.setVisible(false);
    boolean valid = validateDataset().isOk();
    if (valid) {
      Dataset dataset = binder.getBean();
      if (service.exists(dataset.getName()) && (dataset.getId() == null || !dataset.getName()
          .equalsIgnoreCase(service.get(dataset.getId()).map(Dataset::getName).orElse("")))) {
        valid = false;
        AppResources datasetResources = new AppResources(Dataset.class, getLocale());
        error.setText(datasetResources.message(NAME_ALREADY_EXISTS, dataset.getName()));
        error.setVisible(true);
      }
    }
    return valid;
  }

  void save() {
    updateSamplesInBinderBean();
    if (validate()) {
      Dataset dataset = binder.getBean();
      logger.debug("save dataset {}", dataset);
      service.save(dataset);
      AppResources resources = new AppResources(DatasetDialog.class, getLocale());
      showNotification(resources.message(SAVED, dataset.getName()));
      close();
      fireSavedEvent();
    }
  }

  void delete() {
    Dataset dataset = binder.getBean();
    logger.debug("delete dataset {}", dataset);
    service.delete(dataset);
    AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    showNotification(resources.message(DELETED, dataset.getName()));
    fireDeletedEvent();
    close();
  }

  public Dataset getDataset() {
    return binder.getBean();
  }

  void setDataset(Dataset dataset) {
    if (dataset == null) {
      dataset = new Dataset();
    }
    if (dataset.getTags() == null) {
      dataset.setTags(new HashSet<>());
    }
    if (dataset.getDate() == null) {
      dataset.setDate(LocalDate.now());
    }
    if (dataset.getSamples() == null) {
      dataset.setSamples(new ArrayList<>());
    }
    binder.setBean(dataset);
    boolean readOnly = !authenticatedUser.hasPermission(dataset, Permission.WRITE)
        || (dataset.getId() != null && !dataset.isEditable());
    binder.setReadOnly(readOnly);
    samples.setItems(dataset.getSamples());
    generateName.setVisible(!readOnly);
    sampleRemove.setVisible(!readOnly);
    addSample.setVisible(!readOnly);
    save.setVisible(!readOnly);
    cancel.setVisible(!readOnly);
    delete.setVisible(!readOnly);
    updateSamplesFields();
    updateHeader();
  }

  private void updateSamplesFields() {
    Locale locale = getLocale();
    List<Sample> samples = this.samples.getListDataView().getItems().toList();
    protocol.setValue(samples.stream().map(Sample::getProtocol).filter(Objects::nonNull)
        .map(Protocol::getName).distinct().collect(Collectors.joining(", ")));
    assay.setValue(samples.stream().map(Sample::getAssay).filter(Objects::nonNull).distinct()
        .collect(Collectors.joining(", ")));
    type.setValue(samples.stream().map(Sample::getType).filter(Objects::nonNull).distinct()
        .map(type -> type.getLabel(locale)).collect(Collectors.joining(", ")));
    target.setValue(samples.stream().map(Sample::getTarget).filter(Objects::nonNull).distinct()
        .collect(Collectors.joining(", ")));
    strain.setValue(samples.stream().map(Sample::getStrain).filter(Objects::nonNull).distinct()
        .collect(Collectors.joining(", ")));
    strainDescription.setValue(samples.stream().map(Sample::getStrainDescription)
        .filter(Objects::nonNull).distinct().collect(Collectors.joining(", ")));
    treatment.setValue(samples.stream().map(Sample::getTreatment).filter(Objects::nonNull)
        .distinct().collect(Collectors.joining(", ")));
  }
}
