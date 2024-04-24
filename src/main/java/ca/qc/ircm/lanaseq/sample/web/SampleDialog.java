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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.Sample.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NOTE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.REPLICATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.SAMPLE_ID;
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
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Sample dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleDialog extends Dialog implements LocaleChangeObserver, NotificationComponent {
  public static final String ID = "sample-dialog";
  public static final String HEADER = "header";
  public static final String SAVED = "saved";
  public static final String DELETED = "deleted";
  public static final String DELETE_HEADER = property(DELETE, "header");
  public static final String DELETE_MESSAGE = property(DELETE, "message");
  private static final long serialVersionUID = 166699830639260659L;
  private static final Logger logger = LoggerFactory.getLogger(SampleDialog.class);
  protected DatePicker date = new DatePicker();
  protected TextField sampleId = new TextField();
  protected TextField replicate = new TextField();
  protected ComboBox<Protocol> protocol = new ComboBox<>();
  protected ComboBox<String> assay = new ComboBox<>();
  protected Select<SampleType> type = new Select<>();
  protected TextField target = new TextField();
  protected TextField strain = new TextField();
  protected TextField strainDescription = new TextField();
  protected TextField treatment = new TextField();
  protected TextArea note = new TextArea();
  protected Div error = new Div();
  protected Button save = new Button();
  protected Button cancel = new Button();
  protected Button delete = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  private Binder<Sample> binder = new BeanValidationBinder<>(Sample.class);
  private transient SampleService service;
  private transient ProtocolService protocolService;
  private transient AuthenticatedUser authenticatedUser;

  @Autowired
  protected SampleDialog(SampleService service, ProtocolService protocolService,
      AuthenticatedUser authenticatedUser) {
    this.service = service;
    this.protocolService = protocolService;
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
    FormLayout sampleForm = new FormLayout(date, sampleId, replicate, protocol, assay, type);
    sampleForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout strainForm = new FormLayout(target, strain, strainDescription, treatment, note);
    strainForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout form = new FormLayout(sampleForm, strainForm);
    form.setResponsiveSteps(new ResponsiveStep("30em", 1), new ResponsiveStep("30em", 2));
    layout.add(form, error, confirm);
    layout.setSizeFull();
    getFooter().add(delete, cancel, save);
    date.setId(id(DATE));
    sampleId.setId(id(SAMPLE_ID));
    replicate.setId(id(REPLICATE));
    protocol.setId(id(PROTOCOL));
    protocol.setItemLabelGenerator(Protocol::getName);
    protocol.setItems(protocolService.all());
    assay.setId(id(ASSAY));
    assay.setAllowCustomValue(true);
    assay.addCustomValueSetListener(e -> assay.setValue(e.getDetail()));
    assay.setItems(service.topAssays(50));
    type.setId(id(TYPE));
    type.setItemLabelGenerator(t -> t.getLabel(getLocale()));
    type.setItems(SampleType.values());
    target.setId(id(TARGET));
    strain.setId(id(STRAIN));
    strainDescription.setId(id(STRAIN_DESCRIPTION));
    treatment.setId(id(TREATMENT));
    note.setId(id(NOTE));
    note.setHeight("10em");
    error.setId(id(ERROR_TEXT));
    error.setVisible(false);
    save.setId(id(SAVE));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> save());
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> cancel());
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
    setSample(null);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(SampleDialog.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    binder.forField(date).asRequired(webResources.message(REQUIRED)).bind(DATE);
    binder.forField(sampleId).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(SAMPLE_ID);
    binder.forField(replicate).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(REPLICATE);
    binder.forField(protocol).asRequired(webResources.message(REQUIRED)).bind(PROTOCOL);
    binder.forField(assay).asRequired(webResources.message(REQUIRED)).bind(ASSAY);
    binder.forField(type).withNullRepresentation(SampleType.NULL).bind(TYPE);
    binder.forField(target).withNullRepresentation("").bind(TARGET);
    binder.forField(strain).asRequired(webResources.message(REQUIRED)).withNullRepresentation("")
        .bind(STRAIN);
    binder.forField(strainDescription).withNullRepresentation("").bind(STRAIN_DESCRIPTION);
    binder.forField(treatment).withNullRepresentation("").bind(TREATMENT);
    binder.forField(note).withNullRepresentation("").bind(NOTE);
    setHeaderTitle(resources.message(HEADER, 0));
    date.setLabel(sampleResources.message(DATE));
    date.setI18n(datePickerI18n(getLocale()));
    date.setLocale(Locale.CANADA);
    sampleId.setLabel(sampleResources.message(SAMPLE_ID));
    replicate.setLabel(sampleResources.message(REPLICATE));
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
    note.setLabel(sampleResources.message(NOTE));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    delete.setText(webResources.message(DELETE));
    confirm.setHeader(resources.message(DELETE_HEADER));
    confirm.setConfirmText(webResources.message(DELETE));
    confirm.setCancelText(webResources.message(CANCEL));
    updateHeader();
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(SampleDialog.class, getLocale());
    Sample sample = binder.getBean();
    if (sample != null && sample.getName() != null) {
      setHeaderTitle(resources.message(HEADER, 1, sample.getName()));
      confirm.setText(resources.message(DELETE_MESSAGE, sample.getName()));
    } else {
      setHeaderTitle(resources.message(HEADER, 0));
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
  public Registration addSavedListener(ComponentEventListener<SavedEvent<SampleDialog>> listener) {
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
      addDeletedListener(ComponentEventListener<DeletedEvent<SampleDialog>> listener) {
    return addListener((Class) DeletedEvent.class, listener);
  }

  void fireDeletedEvent() {
    fireEvent(new DeletedEvent<>(this, true));
  }

  BinderValidationStatus<Sample> validateSample() {
    return binder.validate();
  }

  boolean validate() {
    error.setVisible(false);
    boolean valid = validateSample().isOk();
    if (valid) {
      Sample sample = binder.getBean();
      if (service.exists(sample.getName()) && (sample.getId() == null || !sample.getName()
          .equalsIgnoreCase(service.get(sample.getId()).map(Sample::getName).orElse("")))) {
        valid = false;
        AppResources sampleResources = new AppResources(Sample.class, getLocale());
        error.setText(sampleResources.message(NAME_ALREADY_EXISTS, sample.getName()));
        error.setVisible(true);
      }
    }
    return valid;
  }

  void save() {
    Sample sample = binder.getBean();
    sample.generateName();
    if (validate()) {
      logger.debug("save sample {}", sample);
      service.save(sample);
      AppResources resources = new AppResources(SampleDialog.class, getLocale());
      showNotification(resources.message(SAVED, sample.getName()));
      fireSavedEvent();
      close();
    }
  }

  void cancel() {
    close();
  }

  void delete() {
    Sample sample = binder.getBean();
    logger.debug("delete sample {}", sample);
    service.delete(sample);
    AppResources resources = new AppResources(SampleDialog.class, getLocale());
    showNotification(resources.message(DELETED, sample.getName()));
    fireDeletedEvent();
    close();
  }

  Sample getSample() {
    return binder.getBean();
  }

  void setSample(Sample sample) {
    if (sample == null) {
      sample = new Sample();
    }
    if (sample.getDate() == null) {
      sample.setDate(LocalDate.now());
    }
    binder.setBean(sample);
    boolean readOnly = !authenticatedUser.hasPermission(sample, Permission.WRITE)
        || (sample.getId() != null && !sample.isEditable());
    binder.setReadOnly(readOnly);
    save.setVisible(!readOnly);
    cancel.setVisible(!readOnly);
    delete.setVisible(!readOnly && service.isDeletable(sample));
    updateHeader();
  }
}
