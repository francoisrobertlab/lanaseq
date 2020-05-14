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
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.THEME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROJECT;
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
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleProperties;
import ca.qc.ircm.lanaseq.sample.web.SampleDialog;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
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
  public static final String SAVED = "saved";
  protected H3 header = new H3();
  protected TextField project = new TextField();
  protected ComboBox<Protocol> protocol = new ComboBox<>();
  protected ComboBox<Assay> assay = new ComboBox<>();
  protected ComboBox<DatasetType> type = new ComboBox<>();
  protected TextField target = new TextField();
  protected TextField strain = new TextField();
  protected TextField strainDescription = new TextField();
  protected TextField treatment = new TextField();
  protected H4 samplesHeader = new H4();
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> sampleId;
  protected Column<Sample> sampleReplicate;
  protected Column<Sample> sampleName;
  protected Button addSample = new Button();
  protected Button save = new Button();
  protected Button cancel = new Button();
  @Autowired
  protected SampleDialog sampleDialog;
  @Autowired
  private transient DatasetDialogPresenter presenter;

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
    FormLayout datasetForm = new FormLayout(project, protocol, assay, type);
    datasetForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout strainForm = new FormLayout(target, strain, strainDescription, treatment);
    strainForm.setResponsiveSteps(new ResponsiveStep("30em", 1));
    FormLayout form = new FormLayout(datasetForm, strainForm);
    form.setResponsiveSteps(new ResponsiveStep("30em", 1), new ResponsiveStep("30em", 2));
    layout.add(header, form, samplesHeader, samples, new HorizontalLayout(save, cancel));
    header.setId(id(HEADER));
    project.setId(id(PROJECT));
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
    samplesHeader.setId(id(SAMPLES_HEADER));
    samples.setId(id(SAMPLES));
    samples.setHeight("14em");
    samples.addItemDoubleClickListener(e -> presenter.editSample(e.getItem()));
    sampleId = samples.addColumn(sample -> sample.getSampleId(), SampleProperties.SAMPLE_ID)
        .setKey(SampleProperties.SAMPLE_ID)
        .setComparator(NormalizedComparator.of(sample -> sample.getSampleId()));
    sampleReplicate = samples.addColumn(sample -> sample.getReplicate(), SampleProperties.REPLICATE)
        .setKey(SampleProperties.REPLICATE)
        .setComparator(NormalizedComparator.of(sample -> sample.getReplicate()));
    sampleName = samples.addColumn(sample -> sample.getName(), SampleProperties.NAME)
        .setKey(SampleProperties.NAME)
        .setComparator(NormalizedComparator.of(sample -> sample.getName()));
    FooterRow footer = samples.appendFooterRow();
    footer.getCell(sampleName).setComponent(addSample);
    addSample.setId(id(ADD_SAMPLE));
    addSample.setIcon(VaadinIcon.PLUS.create());
    addSample.addClickListener(e -> presenter.addSample());
    save.setId(id(SAVE));
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save(getLocale()));
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    updateHeader();
    project.setLabel(datasetResources.message(PROJECT));
    protocol.setLabel(datasetResources.message(PROTOCOL));
    assay.setLabel(datasetResources.message(ASSAY));
    type.setLabel(datasetResources.message(TYPE));
    target.setLabel(datasetResources.message(TARGET));
    target.setPlaceholder(datasetResources.message(property(TARGET, PLACEHOLDER)));
    strain.setLabel(datasetResources.message(STRAIN));
    strain.setPlaceholder(datasetResources.message(property(STRAIN, PLACEHOLDER)));
    strainDescription.setLabel(datasetResources.message(STRAIN_DESCRIPTION));
    strainDescription
        .setPlaceholder(datasetResources.message(property(STRAIN_DESCRIPTION, PLACEHOLDER)));
    treatment.setLabel(datasetResources.message(TREATMENT));
    treatment.setPlaceholder(datasetResources.message(property(TREATMENT, PLACEHOLDER)));
    samplesHeader.setText(resources.message(SAMPLES_HEADER));
    sampleId.setHeader(sampleResources.message(SampleProperties.SAMPLE_ID));
    sampleReplicate.setHeader(sampleResources.message(SampleProperties.REPLICATE));
    sampleName.setHeader(sampleResources.message(SampleProperties.NAME));
    addSample.setText(webResources.message(ADD));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getId() != null) {
      header.setText(resources.message(HEADER, 1, dataset.getName()));
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

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
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
