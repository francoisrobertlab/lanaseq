package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
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

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
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
  private static final long serialVersionUID = 166699830639260659L;
  protected H3 header = new H3();
  protected TextField sampleId = new TextField();
  protected TextField replicate = new TextField();
  protected ComboBox<Protocol> protocol = new ComboBox<>();
  protected ComboBox<Assay> assay = new ComboBox<>();
  protected ComboBox<DatasetType> type = new ComboBox<>();
  protected TextField target = new TextField();
  protected TextField strain = new TextField();
  protected TextField strainDescription = new TextField();
  protected TextField treatment = new TextField();
  protected Button save = new Button();
  protected Button cancel = new Button();
  protected Button delete = new Button();
  @Autowired
  private transient SampleDialogPresenter presenter;

  protected SampleDialog() {
  }

  protected SampleDialog(SampleDialogPresenter presenter) {
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
    FormLayout sampleForm = new FormLayout(sampleId, replicate, protocol, assay, type);
    sampleForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout strainForm = new FormLayout(target, strain, strainDescription, treatment);
    strainForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout form = new FormLayout(sampleForm, strainForm);
    form.setResponsiveSteps(new ResponsiveStep("30em", 1), new ResponsiveStep("30em", 2));
    HorizontalLayout endButtons = new HorizontalLayout(delete);
    endButtons.setJustifyContentMode(JustifyContentMode.END);
    endButtons.setWidthFull();
    HorizontalLayout buttons = new HorizontalLayout(new HorizontalLayout(save, cancel), endButtons);
    buttons.setWidthFull();
    layout.add(header, form, buttons);
    header.setId(id(HEADER));
    sampleId.setId(id(SAMPLE_ID));
    replicate.setId(id(REPLICATE));
    protocol.setId(id(PROTOCOL));
    protocol.setItemLabelGenerator(Protocol::getName);
    protocol.setPreventInvalidInput(true);
    assay.setId(id(ASSAY));
    assay.setItemLabelGenerator(a -> a.getLabel(getLocale()));
    assay.setItems(Assay.values());
    assay.setPreventInvalidInput(true);
    type.setId(id(TYPE));
    type.setItemLabelGenerator(t -> t.getLabel(getLocale()));
    type.setItems(DatasetType.values());
    type.setPreventInvalidInput(true);
    target.setId(id(TARGET));
    strain.setId(id(STRAIN));
    strainDescription.setId(id(STRAIN_DESCRIPTION));
    treatment.setId(id(TREATMENT));
    save.setId(id(SAVE));
    save.setThemeName(PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save(getLocale()));
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    delete.setId(id(DELETE));
    delete.setThemeName(ERROR);
    delete.setIcon(VaadinIcon.TRASH.create());
    delete.addClickListener(e -> presenter.delete(getLocale()));
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(SampleDialog.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER, 0));
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
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    delete.setText(webResources.message(DELETE));
    updateHeader();
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(SampleDialog.class, getLocale());
    Sample sample = presenter.getSample();
    if (sample != null && sample.getSampleId() != null) {
      header.setText(resources.message(HEADER, 1, sample.getSampleId()));
    } else {
      header.setText(resources.message(HEADER, 0));
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

  public Sample getSample() {
    return presenter.getSample();
  }

  public void setSample(Sample sample) {
    presenter.setSample(sample);
    updateHeader();
  }
}
