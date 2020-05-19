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

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
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
public class SampleDialogPresenterTest extends AbstractViewTestCase {
  private SampleDialogPresenter presenter;
  @Mock
  private SampleDialog dialog;
  @Mock
  private SampleService service;
  @Mock
  private ProtocolService protocolService;
  @Captor
  private ArgumentCaptor<Sample> sampleCaptor;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Protocol> protocols;
  private String sampleId = "Test Sample";
  private String replicate = "Test Replicate";
  private Protocol protocol;
  private Assay assay = Assay.CHIP_SEQ;
  private DatasetType type = DatasetType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new SampleDialogPresenter(service, protocolService);
    dialog.header = new H3();
    dialog.sampleId = new TextField();
    dialog.replicate = new TextField();
    dialog.protocol = new ComboBox<>();
    dialog.assay = new ComboBox<>();
    dialog.assay.setItems(Assay.values());
    dialog.type = new ComboBox<>();
    dialog.type.setItems(DatasetType.values());
    dialog.target = new TextField();
    dialog.strain = new TextField();
    dialog.strainDescription = new TextField();
    dialog.treatment = new TextField();
    dialog.save = new Button();
    dialog.cancel = new Button();
    dialog.delete = new Button();
    protocols = protocolRepository.findAll();
    protocol = protocolRepository.findById(1L).get();
    when(protocolService.all()).thenReturn(protocols);
    presenter.init(dialog);
  }

  private void fillForm() {
    dialog.sampleId.setValue(sampleId);
    dialog.replicate.setValue(replicate);
    dialog.protocol.setValue(protocol);
    dialog.assay.setValue(assay);
    dialog.type.setValue(type);
    dialog.target.setValue(target);
    dialog.strain.setValue(strain);
    dialog.strainDescription.setValue(strainDescription);
    dialog.treatment.setValue(treatment);
  }

  @Test
  public void getSample() {
    Sample sample = new Sample();
    presenter.setSample(sample);
    assertEquals(sample, presenter.getSample());
  }

  @Test
  public void setSample_NewSample() {
    Sample sample = new Sample();

    presenter.localeChange(locale);
    presenter.setSample(sample);

    assertEquals("", dialog.sampleId.getValue());
    assertEquals("", dialog.replicate.getValue());
    assertNull(dialog.protocol.getValue());
    assertNull(dialog.assay.getValue());
    assertEquals(DatasetType.NULL, dialog.type.getValue());
    assertEquals("", dialog.target.getValue());
    assertEquals("", dialog.strain.getValue());
    assertEquals("", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(1L).get();

    presenter.localeChange(locale);
    presenter.setSample(sample);

    assertEquals("FR1", dialog.sampleId.getValue());
    assertEquals("R1", dialog.replicate.getValue());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertTrue(dialog.delete.isVisible());
  }

  @Test
  public void setSample_BeforeLocaleChange() {
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);
    presenter.localeChange(locale);

    assertEquals("FR1", dialog.sampleId.getValue());
    assertEquals("R1", dialog.replicate.getValue());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertTrue(dialog.delete.isVisible());
  }

  @Test
  public void setSample_Null() {
    presenter.localeChange(locale);
    presenter.setSample(null);

    assertEquals("", dialog.sampleId.getValue());
    assertEquals("", dialog.replicate.getValue());
    assertNull(dialog.protocol.getValue());
    assertNull(dialog.assay.getValue());
    assertEquals(DatasetType.NULL, dialog.type.getValue());
    assertEquals("", dialog.target.getValue());
    assertEquals("", dialog.strain.getValue());
    assertEquals("", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void requiredIndicator() {
    presenter.localeChange(locale);
    assertTrue(dialog.sampleId.isRequiredIndicatorVisible());
    assertTrue(dialog.replicate.isRequiredIndicatorVisible());
    assertTrue(dialog.protocol.isRequiredIndicatorVisible());
    assertTrue(dialog.assay.isRequiredIndicatorVisible());
    assertFalse(dialog.type.isRequiredIndicatorVisible());
    assertFalse(dialog.target.isRequiredIndicatorVisible());
    assertTrue(dialog.strain.isRequiredIndicatorVisible());
    assertFalse(dialog.strainDescription.isRequiredIndicatorVisible());
    assertFalse(dialog.treatment.isRequiredIndicatorVisible());
  }

  @Test
  public void protocols() {
    List<Protocol> protocols = items(dialog.protocol);
    assertEquals(this.protocols.size(), protocols.size());
    for (int i = 0; i < protocols.size(); i++) {
      assertEquals(this.protocols.get(i), protocols.get(i));
    }
  }

  @Test
  public void save_NameEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.sampleId.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.sampleId);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_ReplicateEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.replicate.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.replicate);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
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
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getType());
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
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getTarget());
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
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getStrainDescription());
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
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getTreatment());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NewSample() {
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    assertEquals(protocol, sample.getProtocol());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    verify(dialog).showNotification(resources.message(SAVED, sample.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void save_UpdateSample() {
    Sample sample = repository.findById(2L).get();
    presenter.setSample(sample);
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(service).save(sampleCaptor.capture());
    sample = sampleCaptor.getValue();
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    assertEquals(protocol, sample.getProtocol());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    verify(dialog).showNotification(resources.message(SAVED, sample.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_SampleProperties() {
    Sample sample = repository.findById(2L).get();
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();

    presenter.cancel();

    assertEquals("FR2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    verify(service, never()).save(any());
    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_SamplePropertiesAfterValidationFail() {
    Sample sample = repository.findById(2L).get();
    presenter.init(dialog);
    presenter.localeChange(locale);
    fillForm();
    dialog.replicate.setValue("");
    presenter.save(locale);

    presenter.cancel();

    assertEquals("FR2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(DatasetType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    verify(service, never()).save(any());
    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.cancel();

    verify(service, never()).save(any());
    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void delete() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.delete();

    verify(service, never()).save(any());
    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog).fireDeletedEvent();
  }
}
