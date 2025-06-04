package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.HELPER;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.Sample.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.FILENAMES;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.KEYWORDS;
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

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.security.Permission;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.FilenamesField;
import ca.qc.ircm.lanaseq.web.KeywordsField;
import ca.qc.ircm.lanaseq.web.SavedEvent;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import java.io.Serial;
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
public class SampleDialog extends Dialog implements LocaleChangeObserver {

  public static final String ID = "sample-dialog";
  public static final String HEADER = "header";
  public static final String SAVED = "saved";
  public static final String DELETED = "deleted";
  public static final String DELETE_HEADER = property(DELETE, "header");
  public static final String DELETE_MESSAGE = property(DELETE, "message");
  private static final String MESSAGE_PREFIX = messagePrefix(SampleDialog.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 166699830639260659L;
  private static final Logger logger = LoggerFactory.getLogger(SampleDialog.class);
  protected DatePicker date = new DatePicker();
  protected TextField sampleId = new TextField();
  protected TextField replicate = new TextField();
  protected ComboBox<Protocol> protocol = new ComboBox<>();
  protected ComboBox<String> assay = new ComboBox<>();
  protected ComboBox<String> type = new ComboBox<>();
  protected TextField target = new TextField();
  protected TextField strain = new TextField();
  protected TextField strainDescription = new TextField();
  protected TextField treatment = new TextField();
  protected KeywordsField keywords = new KeywordsField();
  protected FilenamesField filenames = new FilenamesField();
  protected TextArea note = new TextArea();
  protected Div error = new Div();
  protected Button save = new Button();
  protected Button cancel = new Button();
  protected Button delete = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  private final Binder<Sample> binder = new BeanValidationBinder<>(Sample.class);
  private final transient SampleService service;
  private final transient ProtocolService protocolService;
  private final transient AuthenticatedUser authenticatedUser;

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
    setWidth("1100px");
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    FormLayout sampleForm = new FormLayout(date, sampleId, replicate, protocol, assay, type);
    sampleForm.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("20em", 2));
    FormLayout strainForm = new FormLayout(target, strain, strainDescription, treatment);
    strainForm.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("20em", 2));
    FormLayout form = new FormLayout(sampleForm, strainForm, keywords, filenames, note);
    form.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("20em", 2));
    form.setColspan(keywords, 2);
    form.setColspan(filenames, 2);
    form.setColspan(note, 2);
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
    type.setAllowCustomValue(true);
    type.addCustomValueSetListener(e -> type.setValue(e.getDetail()));
    type.setItems(service.topTypes(50));
    target.setId(id(TARGET));
    strain.setId(id(STRAIN));
    strainDescription.setId(id(STRAIN_DESCRIPTION));
    treatment.setId(id(TREATMENT));
    keywords.setId(id(KEYWORDS));
    keywords.setSuggestions(service.topKeywords(50));
    filenames.setId(id(FILENAMES));
    note.setId(id(NOTE));
    note.setHeight("6em");
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
    setSampleId(0);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    binder.forField(date).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED)).bind(DATE);
    binder.forField(sampleId).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("").bind(SAMPLE_ID);
    binder.forField(replicate).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("").bind(REPLICATE);
    binder.forField(protocol).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .bind(PROTOCOL);
    binder.forField(assay).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED)).bind(ASSAY);
    binder.forField(type).withNullRepresentation("").bind(TYPE);
    binder.forField(target).withNullRepresentation("").bind(TARGET);
    binder.forField(strain).asRequired(getTranslation(CONSTANTS_PREFIX + REQUIRED))
        .withNullRepresentation("").bind(STRAIN);
    binder.forField(strainDescription).withNullRepresentation("").bind(STRAIN_DESCRIPTION);
    binder.forField(treatment).withNullRepresentation("").bind(TREATMENT);
    binder.forField(keywords).bind(KEYWORDS);
    binder.forField(filenames).bind(FILENAMES);
    binder.forField(note).withNullRepresentation("").bind(NOTE);
    setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, 0));
    date.setLabel(getTranslation(SAMPLE_PREFIX + DATE));
    date.setI18n(datePickerI18n(getLocale()));
    date.setLocale(Locale.CANADA);
    sampleId.setLabel(getTranslation(SAMPLE_PREFIX + SAMPLE_ID));
    replicate.setLabel(getTranslation(SAMPLE_PREFIX + REPLICATE));
    protocol.setLabel(getTranslation(SAMPLE_PREFIX + PROTOCOL));
    assay.setLabel(getTranslation(SAMPLE_PREFIX + ASSAY));
    type.setLabel(getTranslation(SAMPLE_PREFIX + TYPE));
    target.setLabel(getTranslation(SAMPLE_PREFIX + TARGET));
    target.setHelperText(getTranslation(SAMPLE_PREFIX + property(TARGET, HELPER)));
    strain.setLabel(getTranslation(SAMPLE_PREFIX + STRAIN));
    strain.setHelperText(getTranslation(SAMPLE_PREFIX + property(STRAIN, HELPER)));
    strainDescription.setLabel(getTranslation(SAMPLE_PREFIX + STRAIN_DESCRIPTION));
    strainDescription.setHelperText(
        getTranslation(SAMPLE_PREFIX + property(STRAIN_DESCRIPTION, HELPER)));
    treatment.setLabel(getTranslation(SAMPLE_PREFIX + TREATMENT));
    treatment.setHelperText(getTranslation(SAMPLE_PREFIX + property(TREATMENT, HELPER)));
    keywords.setLabel(getTranslation(SAMPLE_PREFIX + KEYWORDS));
    filenames.setLabel(getTranslation(SAMPLE_PREFIX + FILENAMES));
    filenames.setHelperText(getTranslation(SAMPLE_PREFIX + property(FILENAMES, HELPER)));
    note.setLabel(getTranslation(SAMPLE_PREFIX + NOTE));
    save.setText(getTranslation(CONSTANTS_PREFIX + SAVE));
    cancel.setText(getTranslation(CONSTANTS_PREFIX + CANCEL));
    delete.setText(getTranslation(CONSTANTS_PREFIX + DELETE));
    confirm.setHeader(getTranslation(MESSAGE_PREFIX + DELETE_HEADER));
    confirm.setConfirmText(getTranslation(CONSTANTS_PREFIX + DELETE));
    confirm.setCancelText(getTranslation(CONSTANTS_PREFIX + CANCEL));
    updateHeader();
  }

  private void updateHeader() {
    Sample sample = binder.getBean();
    if (sample != null && sample.getId() != 0) {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, 1, sample.getName()));
      confirm.setText(getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, sample.getName()));
    } else {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, 0));
    }
  }

  /**
   * Adds listener to be informed when a sample was saved.
   *
   * @param listener listener
   * @return listener registration
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Registration addSavedListener(ComponentEventListener<SavedEvent<SampleDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  /**
   * Adds listener to be informed when a sample was saved.
   *
   * @param listener listener
   * @return listener registration
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Registration addDeletedListener(
      ComponentEventListener<DeletedEvent<SampleDialog>> listener) {
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
      if (service.exists(sample.getName()) && (sample.getId() == 0 || !sample.getName()
          .equalsIgnoreCase(service.get(sample.getId()).map(Sample::getName).orElse("")))) {
        valid = false;
        error.setText(getTranslation(SAMPLE_PREFIX + NAME_ALREADY_EXISTS, sample.getName()));
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
      Notification.show(getTranslation(MESSAGE_PREFIX + SAVED, sample.getName()));
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
    Notification.show(getTranslation(MESSAGE_PREFIX + DELETED, sample.getName()));
    fireDeletedEvent();
    close();
  }

  long getSampleId() {
    return binder.getBean().getId();
  }

  void setSampleId(long id) {
    Sample sample;
    if (id == 0) {
      sample = new Sample();
      sample.setOwner(authenticatedUser.getUser().orElseThrow());
    } else {
      sample = service.get(id).orElseThrow();
    }
    binder.setBean(sample);
    boolean readOnly =
        !authenticatedUser.hasPermission(sample, Permission.WRITE) || (sample.getId() != 0
            && !sample.isEditable());
    binder.setReadOnly(readOnly);
    save.setVisible(!readOnly);
    cancel.setVisible(!readOnly);
    delete.setVisible(!readOnly && service.isDeletable(sample));
    updateHeader();
  }
}
