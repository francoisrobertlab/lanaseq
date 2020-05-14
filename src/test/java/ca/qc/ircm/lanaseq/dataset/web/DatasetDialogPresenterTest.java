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
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private Map<Sample, TextField> sampleIdFields = new HashMap<>();
  private Map<Sample, TextField> sampleReplicateFields = new HashMap<>();
  private List<Protocol> protocols;
  private String project = "Test Project";
  private Protocol protocol;
  private Assay assay = Assay.CHIP_SEQ;
  private DatasetType type = DatasetType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private String sampleId1 = "test sample 1";
  private String sampleReplicate1 = "rep1";
  private String sampleId2 = "test sample 2";
  private String sampleReplicate2 = "rep2";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new DatasetDialogPresenter(service, protocolService);
    dialog.header = new H3();
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
    protocols = protocolRepository.findAll();
    protocol = protocolRepository.findById(1L).get();
    when(protocolService.all()).thenReturn(protocols);
    when(dialog.sampleIdField(any())).then(i -> {
      Sample sample = i.getArgument(0);
      if (sampleIdFields.containsKey(sample)) {
        return sampleIdFields.get(sample);
      }
      TextField field = new TextField();
      sampleIdFields.put(sample, field);
      return field;
    });
    when(dialog.sampleReplicateField(any())).then(i -> {
      Sample sample = i.getArgument(0);
      if (sampleReplicateFields.containsKey(sample)) {
        return sampleReplicateFields.get(sample);
      }
      TextField field = new TextField();
      sampleReplicateFields.put(sample, field);
      return field;
    });
    presenter.init(dialog);
  }

  private void fillForm() {
    dialog.project.setValue(project);
    dialog.protocol.setValue(protocol);
    dialog.assay.setValue(assay);
    dialog.type.setValue(type);
    dialog.target.setValue(target);
    dialog.strain.setValue(strain);
    dialog.strainDescription.setValue(strainDescription);
    dialog.treatment.setValue(treatment);
    List<Sample> samples = items(dialog.samples);
    sampleIdFields.get(samples.get(0)).setValue(sampleId1);
    sampleReplicateFields.get(samples.get(0)).setValue(sampleReplicate1);
    sampleIdFields.get(samples.get(1)).setValue(sampleId2);
    sampleReplicateFields.get(samples.get(1)).setValue(sampleReplicate2);
  }

  @Test
  public void getDataset() {
    Dataset dataset = new Dataset();
    presenter.setDataset(dataset, locale);
    assertEquals(dataset, presenter.getDataset());
  }

  @Test
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();

    presenter.localeChange(locale);
    presenter.setDataset(dataset, locale);

    assertEquals("", dialog.project.getValue());
    assertNull(dialog.protocol.getValue());
    assertNull(dialog.assay.getValue());
    assertEquals(DatasetType.NULL, dialog.type.getValue());
    assertEquals("", dialog.target.getValue());
    assertEquals("", dialog.strain.getValue());
    assertEquals("", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
    List<Sample> samples = items(dialog.samples);
    assertEquals(2, samples.size());
    assertNull(samples.get(0).getSampleId());
    assertEquals("", sampleIdFields.get(samples.get(0)).getValue());
    assertNull(samples.get(0).getReplicate());
    assertEquals("", sampleReplicateFields.get(samples.get(0)).getValue());
    assertNull(samples.get(1).getSampleId());
    assertEquals("", sampleIdFields.get(samples.get(1)).getValue());
    assertNull(samples.get(1).getReplicate());
    assertEquals("", sampleReplicateFields.get(samples.get(1)).getValue());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = repository.findById(1L).get();

    presenter.localeChange(locale);
    presenter.setDataset(dataset, locale);

    assertEquals("polymerase", dialog.project.getValue());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(1)).getValue());
    assertEquals("R2", sampleReplicateFields.get(samples.get(1)).getValue());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(2)).getValue());
    assertEquals("R3", sampleReplicateFields.get(samples.get(2)).getValue());
  }

  @Test
  public void setDataset_Null() {
    presenter.localeChange(locale);
    presenter.setDataset(null, locale);

    assertEquals("", dialog.project.getValue());
    assertNull(dialog.protocol.getValue());
    assertNull(dialog.assay.getValue());
    assertEquals(DatasetType.NULL, dialog.type.getValue());
    assertEquals("", dialog.target.getValue());
    assertEquals("", dialog.strain.getValue());
    assertEquals("", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
    List<Sample> samples = items(dialog.samples);
    assertEquals(2, samples.size());
    assertNull(samples.get(0).getSampleId());
    assertEquals("", sampleIdFields.get(samples.get(0)).getValue());
    assertNull(samples.get(0).getReplicate());
    assertEquals("", sampleReplicateFields.get(samples.get(0)).getValue());
    assertNull(samples.get(1).getSampleId());
    assertEquals("", sampleIdFields.get(samples.get(1)).getValue());
    assertNull(samples.get(1).getReplicate());
    assertEquals("", sampleReplicateFields.get(samples.get(1)).getValue());
  }

  @Test
  public void requiredIndicator() {
    presenter.localeChange(locale);
    assertFalse(dialog.project.isRequiredIndicatorVisible());
    assertTrue(dialog.protocol.isRequiredIndicatorVisible());
    assertTrue(dialog.assay.isRequiredIndicatorVisible());
    assertFalse(dialog.type.isRequiredIndicatorVisible());
    assertFalse(dialog.target.isRequiredIndicatorVisible());
    assertTrue(dialog.strain.isRequiredIndicatorVisible());
    assertFalse(dialog.strainDescription.isRequiredIndicatorVisible());
    assertFalse(dialog.treatment.isRequiredIndicatorVisible());
    List<Sample> samples = items(dialog.samples);
    assertTrue(sampleIdFields.get(samples.get(0)).isRequiredIndicatorVisible());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isRequiredIndicatorVisible());
  }

  @Test
  public void addSample() {
    assertEquals(2, items(dialog.samples).size());
    presenter.addSample();
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    Sample sample = samples.get(samples.size() - 1);
    assertEquals(null, sample.getSampleId());
    assertEquals(null, sample.getReplicate());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void deletedSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset, locale);
    dialog.samples = mock(Grid.class);
    List<Sample> samples = new ArrayList<Sample>(dataset.getSamples());
    Sample sample = samples.get(0);
    presenter.removeSample(sample);
    verify(dialog.samples).setDataProvider(samplesDataProviderCaptor.capture());
    List<Sample> items = new ArrayList<>(samplesDataProviderCaptor.getValue().getItems());
    assertEquals(samples.size() - 1, items.size());
    for (int i = 1; i < samples.size(); i++) {
      assertEquals(samples.get(i), items.get(i - 1));
    }
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

    BinderValidationStatus<Sample> status = presenter.validateSample();
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

    BinderValidationStatus<Sample> status = presenter.validateSample();
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

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getSamples().get(0).getType());
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

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getSamples().get(0).getTarget());
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

    BinderValidationStatus<Sample> status = presenter.validateSample();
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

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getSamples().get(0).getStrainDescription());
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

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getSamples().get(0).getTreatment());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_FirstSampleIdEmpty() {
    presenter.localeChange(locale);
    fillForm();
    Sample sample = items(dialog.samples).get(0);
    sampleIdFields.get(sample).setValue("");

    presenter.save(locale);

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(0);
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, sampleIdFields.get(sample));
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_FirstSampleReplicateEmpty() {
    presenter.localeChange(locale);
    fillForm();
    Sample sample = items(dialog.samples).get(0);
    sampleReplicateFields.get(sample).setValue("");

    presenter.save(locale);

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(0);
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, sampleReplicateFields.get(sample));
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_FirstSampleIdAndReplicateEmpty() {
    presenter.localeChange(locale);
    fillForm();
    Sample sample = items(dialog.samples).get(0);
    sampleIdFields.get(sample).setValue("");
    sampleReplicateFields.get(sample).setValue("");

    presenter.save(locale);

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(0);
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(1, dataset.getSamples().size());
    assertEquals(sampleId2, dataset.getSamples().get(0).getSampleId());
    assertEquals(sampleReplicate2, dataset.getSamples().get(0).getReplicate());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_SecondSampleIdEmpty() {
    presenter.localeChange(locale);
    fillForm();
    Sample sample = items(dialog.samples).get(1);
    sampleIdFields.get(sample).setValue("");

    presenter.save(locale);

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(1);
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, sampleIdFields.get(sample));
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_SecondSampleReplicateEmpty() {
    presenter.localeChange(locale);
    fillForm();
    Sample sample = items(dialog.samples).get(1);
    sampleReplicateFields.get(sample).setValue("");

    presenter.save(locale);

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(1);
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, sampleReplicateFields.get(sample));
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_SecondSampleIdAndReplicateEmpty() {
    presenter.localeChange(locale);
    fillForm();
    Sample sample = items(dialog.samples).get(1);
    sampleIdFields.get(sample).setValue("");
    sampleReplicateFields.get(sample).setValue("");

    presenter.save(locale);

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(1);
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(1, dataset.getSamples().size());
    assertEquals(sampleId1, dataset.getSamples().get(0).getSampleId());
    assertEquals(sampleReplicate1, dataset.getSamples().get(0).getReplicate());
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
    assertEquals(project, dataset.getProject());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals(sampleId1, sample.getSampleId());
    assertEquals(sampleReplicate1, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    sample = dataset.getSamples().get(1);
    assertEquals(sampleId2, sample.getSampleId());
    assertEquals(sampleReplicate2, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    verify(dialog).showNotification(resources.message(SAVED, dataset.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateDataset() {
    Dataset dataset = repository.findById(2L).get();
    presenter.setDataset(dataset, locale);
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(service).save(datasetCaptor.capture());
    dataset = datasetCaptor.getValue();
    assertEquals(project, dataset.getProject());
    assertEquals(2, dataset.getSamples().size());
    Sample sample = dataset.getSamples().get(0);
    assertEquals(sampleId1, sample.getSampleId());
    assertEquals(sampleReplicate1, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    sample = dataset.getSamples().get(1);
    assertEquals(sampleId2, sample.getSampleId());
    assertEquals(sampleReplicate2, sample.getReplicate());
    assertEquals(protocol.getId(), sample.getProtocol().getId());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    verify(dialog).showNotification(resources.message(SAVED, dataset.getName()));
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
