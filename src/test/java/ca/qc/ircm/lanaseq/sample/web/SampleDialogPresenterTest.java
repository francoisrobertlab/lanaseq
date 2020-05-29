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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.sample.web.SampleDialog.SampleFile;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class SampleDialogPresenterTest extends AbstractViewTestCase {
  @Autowired
  private SampleDialogPresenter presenter;
  @Mock
  private SampleDialog dialog;
  @MockBean
  private SampleService service;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private AuthorizationService authorizationService;
  @Captor
  private ArgumentCaptor<Sample> sampleCaptor;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Protocol> protocols;
  private String sampleId = "Test Sample";
  private String replicate = "Test Replicate";
  private Protocol protocol;
  private Assay assay = Assay.CHIP_SEQ;
  private SampleType type = SampleType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private List<Path> files = new ArrayList<>();
  private Random random = new Random();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog.header = new H3();
    dialog.sampleId = new TextField();
    dialog.replicate = new TextField();
    dialog.protocol = new ComboBox<>();
    dialog.assay = new ComboBox<>();
    dialog.assay.setItems(Assay.values());
    dialog.type = new ComboBox<>();
    dialog.type.setItems(SampleType.values());
    dialog.target = new TextField();
    dialog.strain = new TextField();
    dialog.strainDescription = new TextField();
    dialog.treatment = new TextField();
    dialog.files = new Grid<>();
    dialog.filenameEdit = new TextField();
    dialog.save = new Button();
    dialog.cancel = new Button();
    dialog.delete = new Button();
    protocols = protocolRepository.findAll();
    protocol = protocolRepository.findById(1L).get();
    files.add(Paths.get("sample_R1.fastq"));
    files.add(Paths.get("sample_R2.fastq"));
    files.add(Paths.get("sample.bw"));
    files.add(Paths.get("sample.png"));
    when(protocolService.all()).thenReturn(protocols);
    when(service.files(any())).thenReturn(files);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
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

    presenter.setSample(sample);

    verify(service).files(sample);
    assertEquals("", dialog.sampleId.getValue());
    assertFalse(dialog.sampleId.isReadOnly());
    assertEquals("", dialog.replicate.getValue());
    assertFalse(dialog.replicate.isReadOnly());
    assertNull(dialog.protocol.getValue());
    assertFalse(dialog.protocol.isReadOnly());
    assertNull(dialog.assay.getValue());
    assertFalse(dialog.assay.isReadOnly());
    assertEquals(SampleType.NULL, dialog.type.getValue());
    assertFalse(dialog.type.isReadOnly());
    assertEquals("", dialog.target.getValue());
    assertFalse(dialog.target.isReadOnly());
    assertEquals("", dialog.strain.getValue());
    assertFalse(dialog.strain.isReadOnly());
    assertEquals("", dialog.strainDescription.getValue());
    assertFalse(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertFalse(dialog.treatment.isReadOnly());
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    assertEquals("FR1", dialog.sampleId.getValue());
    assertFalse(dialog.sampleId.isReadOnly());
    assertEquals("R1", dialog.replicate.getValue());
    assertFalse(dialog.replicate.isReadOnly());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertFalse(dialog.protocol.isReadOnly());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertFalse(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertFalse(dialog.type.isReadOnly());
    assertEquals("polr2a", dialog.target.getValue());
    assertFalse(dialog.target.isReadOnly());
    assertEquals("yFR100", dialog.strain.getValue());
    assertFalse(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertFalse(dialog.strainDescription.isReadOnly());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertFalse(dialog.treatment.isReadOnly());
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setSample_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    assertEquals("FR1", dialog.sampleId.getValue());
    assertTrue(dialog.sampleId.isReadOnly());
    assertEquals("R1", dialog.replicate.getValue());
    assertTrue(dialog.replicate.isReadOnly());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2a", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yFR100", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setSample_DeletableSample() {
    when(service.isDeletable(any())).thenReturn(true);
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    assertEquals("FR1", dialog.sampleId.getValue());
    assertFalse(dialog.sampleId.isReadOnly());
    assertEquals("R1", dialog.replicate.getValue());
    assertFalse(dialog.replicate.isReadOnly());
    assertEquals((Long) 1L, dialog.protocol.getValue().getId());
    assertFalse(dialog.protocol.isReadOnly());
    assertEquals(Assay.MNASE_SEQ, dialog.assay.getValue());
    assertFalse(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertFalse(dialog.type.isReadOnly());
    assertEquals("polr2a", dialog.target.getValue());
    assertFalse(dialog.target.isReadOnly());
    assertEquals("yFR100", dialog.strain.getValue());
    assertFalse(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertFalse(dialog.strainDescription.isReadOnly());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertFalse(dialog.treatment.isReadOnly());
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertTrue(dialog.delete.isVisible());
  }

  @Test
  public void setSample_Null() {
    presenter.setSample(null);

    verify(service, atLeastOnce()).files(any());
    assertEquals("", dialog.sampleId.getValue());
    assertFalse(dialog.sampleId.isReadOnly());
    assertEquals("", dialog.replicate.getValue());
    assertFalse(dialog.replicate.isReadOnly());
    assertNull(dialog.protocol.getValue());
    assertFalse(dialog.protocol.isReadOnly());
    assertNull(dialog.assay.getValue());
    assertFalse(dialog.assay.isReadOnly());
    assertEquals(SampleType.NULL, dialog.type.getValue());
    assertFalse(dialog.type.isReadOnly());
    assertEquals("", dialog.target.getValue());
    assertFalse(dialog.target.isReadOnly());
    assertEquals("", dialog.strain.getValue());
    assertFalse(dialog.strain.isReadOnly());
    assertEquals("", dialog.strainDescription.getValue());
    assertFalse(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertFalse(dialog.treatment.isReadOnly());
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void requiredIndicator() {
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
  public void filenameEdit_Empty() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void filenameEdit_Invalid() throws Throwable {
    dialog.filenameEdit.setValue("abc?.txt");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(resources.message(FILENAME_REGEX_ERROR)), error.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void filenameEdit_Exists() throws Throwable {
    Path path = temporaryFolder.newFile("source.txt").toPath();
    temporaryFolder.newFile("abc.txt");
    SampleFile file = new SampleFile(path);
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(file);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(ALREADY_EXISTS)), error.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void filenameEdit_ExistsSameFile() throws Throwable {
    Path path = temporaryFolder.newFile("source.txt").toPath();
    SampleFile file = new SampleFile(path);
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(file);

    dialog.filenameEdit.setValue("source.txt");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertTrue(status.isOk());
  }

  @Test
  public void rename() throws Throwable {
    Path path = temporaryFolder.newFile("source.txt").toPath();
    SampleFile file = new SampleFile(path);
    file.setFilename("target.txt");
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path, bytes);

    presenter.rename(file, locale);

    assertTrue(Files.exists(path.resolveSibling("target.txt")));
    assertArrayEquals(bytes, Files.readAllBytes(path.resolveSibling("target.txt")));
    assertFalse(Files.exists(path));
  }

  @Test
  public void save_NameEmpty() {
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
    verify(service, never()).delete(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_ReplicateEmpty() {
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
    verify(service, never()).delete(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_ProtocolEmpty() {
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
    verify(service, never()).delete(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_AssayEmpty() {
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
    verify(service, never()).delete(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_TypeEmpty() {
    fillForm();
    dialog.type.setValue(SampleType.NULL);

    presenter.save(locale);

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getType());
    verify(service, never()).delete(any());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_TargetEmpty() {
    fillForm();
    dialog.target.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getTarget());
    verify(service, never()).delete(any());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_StrainEmpty() {
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
    verify(service, never()).delete(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_StrainDescriptionEmpty() {
    fillForm();
    dialog.strainDescription.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getStrainDescription());
    verify(service, never()).delete(any());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_TreatmentEmpty() {
    fillForm();
    dialog.treatment.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Sample> status = presenter.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getTreatment());
    verify(service, never()).delete(any());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NewSample() {
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
    verify(service, never()).delete(any());
    verify(dialog).showNotification(resources.message(SAVED, sample.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void save_UpdateSample() {
    Sample sample = repository.findById(2L).get();
    presenter.setSample(sample);
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
    verify(service, never()).delete(any());
    verify(dialog).showNotification(resources.message(SAVED, sample.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_SampleProperties() {
    Sample sample = repository.findById(2L).get();
    fillForm();

    presenter.cancel();

    assertEquals("FR2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_SamplePropertiesAfterValidationFail() {
    Sample sample = repository.findById(2L).get();
    fillForm();
    dialog.replicate.setValue("");
    presenter.save(locale);

    presenter.cancel();

    assertEquals("FR2", sample.getSampleId());
    assertEquals("R2", sample.getReplicate());
    assertEquals((Long) 1L, sample.getProtocol().getId());
    assertEquals(Assay.MNASE_SEQ, sample.getAssay());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, sample.getType());
    assertEquals("polr2a", sample.getTarget());
    assertEquals("yFR100", sample.getStrain());
    assertEquals("WT", sample.getStrainDescription());
    assertEquals("Rappa", sample.getTreatment());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.cancel();

    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
    verify(dialog, never()).fireDeletedEvent();
  }

  @Test
  public void delete() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);

    presenter.delete(locale);

    verify(service, never()).save(any());
    verify(service).delete(sample);
    verify(dialog).close();
    verify(dialog).showNotification(resources.message(DELETED, sample.getName()));
    verify(dialog, never()).fireSavedEvent();
    verify(dialog).fireDeletedEvent();
  }
}
