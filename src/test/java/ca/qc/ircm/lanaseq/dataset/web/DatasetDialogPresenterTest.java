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

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.SampleDialog;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DatasetDialogPresenterTest extends AbstractViewTestCase {
  private DatasetDialogPresenter presenter;
  @Mock
  private DatasetDialog dialog;
  @Mock
  private DatasetService service;
  @Mock
  private ProtocolService protocolService;
  @Mock
  private SampleService sampleService;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<SampleDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<DeletedEvent<SampleDialog>>> deletedListenerCaptor;
  @Captor
  private ArgumentCaptor<ListDataProvider<Sample>> samplesDataProviderCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Protocol> protocols;
  private List<Sample> samples;
  private String name = "Test Dataset";
  private String project = "Test Project";
  private Protocol protocol;
  private Assay assay = Assay.CHIP_SEQ;
  private DatasetType type = DatasetType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private String sampleName1 = "test sample 1";
  private String sampleReplicate1 = "rep1";
  private String sampleName2 = "test sample 2";
  private String sampleReplicate2 = "rep2";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new DatasetDialogPresenter(service, protocolService, sampleService);
    dialog.header = new H3();
    dialog.name = new TextField();
    dialog.project = new TextField();
    dialog.protocol = new ComboBox<>();
    dialog.assay = new ComboBox<>();
    dialog.assay.setItems(Assay.values());
    dialog.type = new ComboBox<>();
    dialog.type.setItems(DatasetType.values());
    dialog.target = new TextField();
    dialog.strain = new TextField();
    dialog.strainDescription = new TextField();
    dialog.treatment = new TextField();
    dialog.samplesHeader = new H4();
    dialog.samples = new Grid<>();
    dialog.save = new Button();
    dialog.cancel = new Button();
    dialog.sampleDialog = mock(SampleDialog.class);
    protocols = protocolRepository.findAll();
    protocol = protocolRepository.findById(1L).get();
    when(protocolService.all()).thenReturn(protocols);
    samples = sampleRepository.findAll();
    when(sampleService.all(any())).thenReturn(samples);
    presenter.init(dialog);
  }

  @SuppressWarnings("unchecked")
  private void fillForm() {
    dialog.name.setValue(name);
    dialog.project.setValue(project);
    dialog.protocol.setValue(protocol);
    dialog.assay.setValue(assay);
    dialog.type.setValue(type);
    dialog.target.setValue(target);
    dialog.strain.setValue(strain);
    dialog.strainDescription.setValue(strainDescription);
    dialog.treatment.setValue(treatment);
    List<Sample> samples = new ArrayList<Sample>();
    Sample sample1 = new Sample();
    sample1.setName(sampleName1);
    sample1.setReplicate(sampleReplicate1);
    samples.add(sample1);
    Sample sample2 = new Sample();
    sample2.setName(sampleName2);
    sample2.setReplicate(sampleReplicate2);
    samples.add(sample2);
    ListDataProvider<Sample> samplesDataProvider =
        (ListDataProvider<Sample>) dialog.samples.getDataProvider();
    samplesDataProvider.getItems().clear();
    samplesDataProvider.getItems().addAll(samples);
  }

  @Test
  public void getDataset() {
    Dataset dataset = new Dataset();
    presenter.setDataset(dataset);
    assertEquals(dataset, presenter.getDataset());
  }

  @Test
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();

    presenter.localeChange(locale);
    presenter.setDataset(dataset);

    assertEquals("", dialog.name.getValue());
    assertEquals("", dialog.project.getValue());
    assertNull(dialog.protocol.getValue());
    assertNull(dialog.assay.getValue());
    assertEquals(DatasetType.NULL, dialog.type.getValue());
    assertEquals("", dialog.target.getValue());
    assertEquals("", dialog.strain.getValue());
    assertEquals("", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
    verify(sampleService, never()).all(dataset);
    assertTrue(items(dialog.samples).isEmpty());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = repository.findById(1L).get();

    presenter.localeChange(locale);
    presenter.setDataset(dataset);

    assertEquals("POLR2A DNA location", dialog.name.getValue());
    assertEquals("polymerase", dialog.project.getValue());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
    verify(sampleService).all(dataset);
    assertEquals(samples, items(dialog.samples));
  }

  @Test
  public void setDataset_BeforeLocaleChange() {
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);
    presenter.localeChange(locale);

    assertEquals("POLR2A DNA location", dialog.name.getValue());
    assertEquals("polymerase", dialog.project.getValue());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    verify(sampleService).all(dataset);
    assertEquals(samples, items(dialog.samples));
  }

  @Test
  public void setDataset_Null() {
    presenter.localeChange(locale);
    presenter.setDataset(null);

    assertEquals("", dialog.name.getValue());
    assertEquals("", dialog.project.getValue());
    assertNull(dialog.protocol.getValue());
    assertNull(dialog.assay.getValue());
    assertEquals(DatasetType.NULL, dialog.type.getValue());
    assertEquals("", dialog.target.getValue());
    assertEquals("", dialog.strain.getValue());
    assertEquals("", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
    verify(sampleService, never()).all(any());
    assertTrue(items(dialog.samples).isEmpty());
  }

  @Test
  public void requiredIndicator() {
    presenter.localeChange(locale);
    assertTrue(dialog.name.isRequiredIndicatorVisible());
    assertFalse(dialog.project.isRequiredIndicatorVisible());
    assertTrue(dialog.protocol.isRequiredIndicatorVisible());
    assertTrue(dialog.assay.isRequiredIndicatorVisible());
    assertFalse(dialog.type.isRequiredIndicatorVisible());
    assertFalse(dialog.target.isRequiredIndicatorVisible());
    assertTrue(dialog.strain.isRequiredIndicatorVisible());
    assertFalse(dialog.strainDescription.isRequiredIndicatorVisible());
    assertFalse(dialog.treatment.isRequiredIndicatorVisible());
  }

  @Test
  public void addSample() {
    presenter.addSample();
    verify(dialog.sampleDialog).setSample(null);
    verify(dialog.sampleDialog).open();
  }

  @Test
  public void editSample() {
    presenter.editSample(sample);
    verify(dialog.sampleDialog).setSample(sample);
    verify(dialog.sampleDialog).open();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void savedSample_New() {
    presenter.setDataset(new Dataset(1L));
    List<Sample> samples = new ArrayList<Sample>(this.samples);
    dialog.samples = mock(Grid.class);
    verify(dialog.sampleDialog).addSavedListener(savedListenerCaptor.capture());
    Sample sample = mock(Sample.class);
    when(dialog.sampleDialog.getSample()).thenReturn(sample);
    ComponentEventListener<SavedEvent<SampleDialog>> savedListener = savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(dialog.samples).setDataProvider(samplesDataProviderCaptor.capture());
    List<Sample> items = new ArrayList<>(samplesDataProviderCaptor.getValue().getItems());
    assertEquals(samples.size() + 1, items.size());
    for (int i = 0; i < samples.size(); i++) {
      assertEquals(samples.get(i), items.get(i));
    }
    assertEquals(sample, items.get(items.size() - 1));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void savedSample() {
    presenter.setDataset(new Dataset(1L));
    List<Sample> samples = new ArrayList<Sample>(this.samples);
    dialog.samples = mock(Grid.class);
    verify(dialog.sampleDialog).addSavedListener(savedListenerCaptor.capture());
    Sample sample = samples.get(0);
    when(dialog.sampleDialog.getSample()).thenReturn(sample);
    ComponentEventListener<SavedEvent<SampleDialog>> savedListener = savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(dialog.samples).setDataProvider(samplesDataProviderCaptor.capture());
    List<Sample> items = new ArrayList<>(samplesDataProviderCaptor.getValue().getItems());
    assertEquals(samples.size(), items.size());
    for (int i = 0; i < samples.size(); i++) {
      assertEquals(samples.get(i), items.get(i));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void deletedSample() {
    presenter.setDataset(new Dataset(1L));
    List<Sample> samples = new ArrayList<Sample>(this.samples);
    dialog.samples = mock(Grid.class);
    verify(dialog.sampleDialog).addDeletedListener(deletedListenerCaptor.capture());
    Sample sample = samples.get(0);
    when(dialog.sampleDialog.getSample()).thenReturn(sample);
    ComponentEventListener<DeletedEvent<SampleDialog>> deletedListener =
        deletedListenerCaptor.getValue();
    deletedListener.onComponentEvent(mock(DeletedEvent.class));
    verify(dialog.samples).setDataProvider(samplesDataProviderCaptor.capture());
    List<Sample> items = new ArrayList<>(samplesDataProviderCaptor.getValue().getItems());
    assertEquals(samples.size() - 1, items.size());
    for (int i = 1; i < samples.size(); i++) {
      assertEquals(samples.get(i), items.get(i - 1));
    }
  }

  @Test
  public void save_NameEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.name.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_ProjectEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.project.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getProject());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_ProtocolEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.protocol.setItems();

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.protocol);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_AssayEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.assay.clear();

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.assay);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_TypeEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.type.setValue(DatasetType.NULL);

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getType());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_TargetEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.target.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getTarget());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_StrainEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.strain.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.strain);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_StrainDescriptionEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.strainDescription.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getStrainDescription());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_TreatmentEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.treatment.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getTreatment());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NewDataset() {
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(name, dataset.getName());
    assertEquals(project, dataset.getProject());
    assertEquals(protocol.getId(), dataset.getProtocol().getId());
    assertEquals(assay, dataset.getAssay());
    assertEquals(type, dataset.getType());
    assertEquals(target, dataset.getTarget());
    assertEquals(strain, dataset.getStrain());
    assertEquals(strainDescription, dataset.getStrainDescription());
    assertEquals(treatment, dataset.getTreatment());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals(sampleName1, sample.getName());
    assertEquals(sampleReplicate1, sample.getReplicate());
    sample = dataset.getSamples().get(1);
    assertEquals(sampleName2, sample.getName());
    assertEquals(sampleReplicate2, sample.getReplicate());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateDataset() {
    Dataset dataset = repository.findById(2L).get();
    presenter.setDataset(dataset);
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(service).save(datasetCaptor.capture());
    dataset = datasetCaptor.getValue();
    assertEquals(name, dataset.getName());
    assertEquals(project, dataset.getProject());
    assertEquals(protocol.getId(), dataset.getProtocol().getId());
    assertEquals(assay, dataset.getAssay());
    assertEquals(type, dataset.getType());
    assertEquals(target, dataset.getTarget());
    assertEquals(strain, dataset.getStrain());
    assertEquals(strainDescription, dataset.getStrainDescription());
    assertEquals(treatment, dataset.getTreatment());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals(sampleName1, sample.getName());
    assertEquals(sampleReplicate1, sample.getReplicate());
    sample = dataset.getSamples().get(1);
    assertEquals(sampleName2, sample.getName());
    assertEquals(sampleReplicate2, sample.getReplicate());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.localeChange(locale);

    presenter.cancel();

    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
  }
}
