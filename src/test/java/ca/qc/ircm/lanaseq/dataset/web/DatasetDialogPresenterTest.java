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
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.sample.web.SampleDialog;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.SelectedEvent;
import ca.qc.ircm.lanaseq.web.TagsField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class DatasetDialogPresenterTest extends AbstractKaribuTestCase {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Autowired
  private DatasetDialogPresenter presenter;
  @Mock
  private DatasetDialog dialog;
  @MockBean
  private DatasetService service;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private AuthorizationService authorizationService;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<SampleDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<DeletedEvent<SampleDialog>>> deletedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>>> selectListenerCaptor;
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
  private Map<Sample, TextField> sampleIdFields = new HashMap<>();
  private Map<Sample, TextField> sampleReplicateFields = new HashMap<>();
  private List<String> topTags = new ArrayList<>();
  private List<Protocol> protocols;
  private String tag1 = "Tag 1";
  private String tag2 = "Tag 2";
  private Protocol protocol;
  private Assay assay = Assay.CHIP_SEQ;
  private SampleType type = SampleType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private LocalDate date = LocalDate.of(2020, 7, 20);
  private String sampleId1 = "test sample 1";
  private String sampleReplicate1 = "rep1";
  private String sampleId2 = "test sample 2";
  private String sampleReplicate2 = "rep2";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    ui.setLocale(locale);
    dialog.header = new H3();
    dialog.tags = new TagsField();
    dialog.protocol = new ComboBox<>();
    dialog.assay = new Select<>();
    dialog.assay.setItems(Assay.values());
    dialog.type = new Select<>();
    dialog.type.setItems(SampleType.values());
    dialog.target = new TextField();
    dialog.strain = new TextField();
    dialog.strainDescription = new TextField();
    dialog.treatment = new TextField();
    dialog.date = new DatePicker();
    dialog.samples = new Grid<>();
    dialog.addNewSample = new Button();
    dialog.addSample = new Button();
    dialog.save = new Button();
    dialog.cancel = new Button();
    dialog.delete = new Button();
    dialog.selectSampleDialog = mock(SelectSampleDialog.class);
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
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    topTags.add("input");
    topTags.add("chip");
    when(service.topTags(anyInt())).thenReturn(topTags);
    presenter.init(dialog);
    presenter.localeChange(locale);
  }

  private void fillForm() {
    dialog.tags.setValue(Stream.of(tag1, tag2).collect(Collectors.toSet()));
    dialog.protocol.setValue(protocol);
    dialog.assay.setValue(assay);
    dialog.type.setValue(type);
    dialog.target.setValue(target);
    dialog.strain.setValue(strain);
    dialog.strainDescription.setValue(strainDescription);
    dialog.treatment.setValue(treatment);
    dialog.date.setValue(date);
    List<Sample> samples = items(dialog.samples);
    sampleIdFields.get(samples.get(0)).setValue(sampleId1);
    sampleReplicateFields.get(samples.get(0)).setValue(sampleReplicate1);
    sampleIdFields.get(samples.get(1)).setValue(sampleId2);
    sampleReplicateFields.get(samples.get(1)).setValue(sampleReplicate2);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void init() throws Throwable {
    Field newTagField = TagsField.class.getDeclaredField("newTag");
    newTagField.setAccessible(true);
    ComboBox<String> newTag = (ComboBox<String>) newTagField.get(dialog.tags);
    List<String> items = items(newTag);
    assertEquals(topTags, items);
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

    presenter.setDataset(dataset);

    assertTrue(dialog.tags.getValue().isEmpty());
    assertFalse(dialog.tags.isReadOnly());
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
    assertEquals(LocalDate.now(), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
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

    presenter.setDataset(dataset);

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
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
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertFalse(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(1)).getValue());
    assertFalse(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R2", sampleReplicateFields.get(samples.get(1)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(1)).isReadOnly());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(2)).getValue());
    assertFalse(sampleIdFields.get(samples.get(2)).isReadOnly());
    assertEquals("R3", sampleReplicateFields.get(samples.get(2)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(2)).isReadOnly());
  }

  @Test
  public void setDataset_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertTrue(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
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
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(1)).getValue());
    assertTrue(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R2", sampleReplicateFields.get(samples.get(1)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(1)).isReadOnly());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(2)).getValue());
    assertTrue(sampleIdFields.get(samples.get(2)).isReadOnly());
    assertEquals("R3", sampleReplicateFields.get(samples.get(2)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(2)).isReadOnly());
  }

  @Test
  public void setDataset_NotEditable() {
    Dataset dataset = repository.findById(5L).get();

    presenter.setDataset(dataset);

    assertEquals(1, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("chipseq"));
    assertTrue(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 2L, dialog.protocol.getValue().getId());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals(Assay.CHIP_SEQ, dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2b", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yBC103", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals(LocalDate.of(2018, 12, 05), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(1, samples.size());
    assertTrue(find(samples, 8L).isPresent());
    assertEquals("BC1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
  }

  @Test
  public void setDataset_NotEditableWithEditableSamples() {
    Dataset dataset = repository.findById(5L).get();
    dataset.getSamples().get(0).setEditable(true);

    presenter.setDataset(dataset);

    assertEquals(1, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("chipseq"));
    assertTrue(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 2L, dialog.protocol.getValue().getId());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals(Assay.CHIP_SEQ, dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2b", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yBC103", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals(LocalDate.of(2018, 12, 05), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(1, samples.size());
    assertTrue(find(samples, 8L).isPresent());
    assertEquals("BC1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
  }

  @Test
  public void setDataset_CannotWriteAnySample() {
    when(authorizationService.hasPermission(any(Sample.class), any())).thenReturn(false);
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
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
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(1)).getValue());
    assertTrue(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R2", sampleReplicateFields.get(samples.get(1)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(1)).isReadOnly());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(2)).getValue());
    assertTrue(sampleIdFields.get(samples.get(2)).isReadOnly());
    assertEquals("R3", sampleReplicateFields.get(samples.get(2)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(2)).isReadOnly());
  }

  @Test
  public void setDataset_NoEditableSamples() {
    Dataset dataset = repository.findById(1L).get();
    dataset.getSamples().forEach(sample -> sample.setEditable(false));

    presenter.setDataset(dataset);

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
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
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(1)).getValue());
    assertTrue(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R2", sampleReplicateFields.get(samples.get(1)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(1)).isReadOnly());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(2)).getValue());
    assertTrue(sampleIdFields.get(samples.get(2)).isReadOnly());
    assertEquals("R3", sampleReplicateFields.get(samples.get(2)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(2)).isReadOnly());
  }

  @Test
  public void setDataset_CannotWriteOneSample() {
    when(authorizationService.hasPermission(any(Sample.class), any())).then(i -> {
      Sample sample = i.getArgument(0);
      return sample.getId() == null || sample.getId() != 2;
    });
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
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
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertFalse(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(1)).getValue());
    assertTrue(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R2", sampleReplicateFields.get(samples.get(1)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(1)).isReadOnly());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(2)).getValue());
    assertFalse(sampleIdFields.get(samples.get(2)).isReadOnly());
    assertEquals("R3", sampleReplicateFields.get(samples.get(2)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(2)).isReadOnly());
  }

  @Test
  public void setDataset_OneSampleNotEditable() {
    Dataset dataset = repository.findById(1L).get();
    dataset.getSamples().get(1).setEditable(false);

    presenter.setDataset(dataset);

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
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
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertFalse(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(1)).getValue());
    assertTrue(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R2", sampleReplicateFields.get(samples.get(1)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(1)).isReadOnly());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(2)).getValue());
    assertFalse(sampleIdFields.get(samples.get(2)).isReadOnly());
    assertEquals("R3", sampleReplicateFields.get(samples.get(2)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(2)).isReadOnly());
  }

  @Test
  public void setDataset_CannotWriteOnlyOneSample() {
    when(authorizationService.hasPermission(any(Sample.class), any())).thenReturn(false);
    Dataset dataset = repository.findById(5L).get();
    dataset.setEditable(true);

    presenter.setDataset(dataset);

    assertEquals(1, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("chipseq"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 2L, dialog.protocol.getValue().getId());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals(Assay.CHIP_SEQ, dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2b", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yBC103", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals(LocalDate.of(2018, 12, 05), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(1, samples.size());
    assertTrue(find(samples, 8L).isPresent());
    assertEquals("BC1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
  }

  @Test
  public void setDataset_NotEditableOnlyOneSample() {
    Dataset dataset = repository.findById(5L).get();
    dataset.setEditable(true);
    dataset.getSamples().get(0).setEditable(false);

    presenter.setDataset(dataset);

    assertEquals(1, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("chipseq"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 2L, dialog.protocol.getValue().getId());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals(Assay.CHIP_SEQ, dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2b", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yBC103", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals(LocalDate.of(2018, 12, 05), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(1, samples.size());
    assertTrue(find(samples, 8L).isPresent());
    assertEquals("BC1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
  }

  @Test
  public void setDataset_Null() {
    presenter.setDataset(null);

    assertTrue(dialog.tags.getValue().isEmpty());
    assertFalse(dialog.tags.isReadOnly());
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
    assertEquals(LocalDate.now(), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    assertEquals(LocalDate.now(), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
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
    assertFalse(dialog.tags.isRequiredIndicatorVisible());
    assertTrue(dialog.protocol.isRequiredIndicatorVisible());
    assertTrue(dialog.assay.isRequiredIndicatorVisible());
    assertFalse(dialog.type.isRequiredIndicatorVisible());
    assertFalse(dialog.target.isRequiredIndicatorVisible());
    assertTrue(dialog.strain.isRequiredIndicatorVisible());
    assertFalse(dialog.strainDescription.isRequiredIndicatorVisible());
    assertFalse(dialog.treatment.isRequiredIndicatorVisible());
    assertTrue(dialog.date.isRequiredIndicatorVisible());
    List<Sample> samples = items(dialog.samples);
    assertTrue(sampleIdFields.get(samples.get(0)).isRequiredIndicatorVisible());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isRequiredIndicatorVisible());
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
  public void addNewSample() {
    assertEquals(2, items(dialog.samples).size());
    presenter.addNewSample();
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    Sample sample = samples.get(samples.size() - 1);
    assertEquals(null, sample.getSampleId());
    assertEquals(null, sample.getReplicate());
  }

  @Test
  public void addNewSample_NotEditableSamples() {
    Dataset dataset = repository.findById(5L).get();
    dataset.setEditable(true);
    dataset.getSamples().get(0).setEditable(false);
    presenter.setDataset(dataset);

    presenter.addNewSample();

    assertEquals(1, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("chipseq"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
    assertEquals((Long) 2L, dialog.protocol.getValue().getId());
    assertFalse(dialog.protocol.isReadOnly());
    assertEquals(Assay.CHIP_SEQ, dialog.assay.getValue());
    assertFalse(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertFalse(dialog.type.isReadOnly());
    assertEquals("polr2b", dialog.target.getValue());
    assertFalse(dialog.target.isReadOnly());
    assertEquals("yBC103", dialog.strain.getValue());
    assertFalse(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertFalse(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertFalse(dialog.treatment.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(2, samples.size());
    assertTrue(find(samples, 8L).isPresent());
    assertEquals("BC1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertEquals("", sampleIdFields.get(samples.get(1)).getValue());
    assertFalse(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("", sampleReplicateFields.get(samples.get(1)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(1)).isReadOnly());
  }

  @Test
  public void addSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(3, items(dialog.samples).size());
    verify(dialog.selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    presenter.addSample();
    verify(dialog.selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(dialog.selectSampleDialog, false, sample));
    List<Sample> samples = items(dialog.samples);
    assertEquals(4, samples.size());
    assertEquals(sample, samples.get(samples.size() - 1));
  }

  @Test
  public void addSample_AlreadyInDataset() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    Sample sample = dataset.getSamples().get(0);
    assertEquals(3, items(dialog.samples).size());
    verify(dialog.selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    presenter.addSample();
    verify(dialog.selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(dialog.selectSampleDialog, false, sample));
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
  }

  @Test
  public void addSample_NotEditableSamples() {
    Dataset dataset = repository.findById(5L).get();
    dataset.setEditable(true);
    dataset.getSamples().get(0).setEditable(false);
    presenter.setDataset(dataset);
    Sample sample = sampleRepository.findById(1L).get();

    verify(dialog.selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    presenter.addSample();
    verify(dialog.selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(dialog.selectSampleDialog, false, sample));

    List<Sample> samples = items(dialog.samples);
    assertEquals(2, samples.size());
    assertTrue(find(samples, 8L).isPresent());
    assertTrue(find(samples, 1L).isPresent());
    assertEquals("BC1", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertEquals("FR1", sampleIdFields.get(samples.get(1)).getValue());
    assertFalse(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(1)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(1)).isReadOnly());
  }

  @Test
  public void addSample_AddNotEditableSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    Sample sample = sampleRepository.findById(8L).get();

    verify(dialog.selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    presenter.addSample();
    verify(dialog.selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(dialog.selectSampleDialog, false, sample));

    List<Sample> samples = items(dialog.samples);
    assertEquals(4, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
    assertTrue(find(samples, 8L).isPresent());
    assertEquals("FR1", sampleIdFields.get(samples.get(0)).getValue());
    assertFalse(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(0)).getValue());
    assertFalse(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertEquals("BC1", sampleIdFields.get(samples.get(3)).getValue());
    assertTrue(sampleIdFields.get(samples.get(3)).isReadOnly());
    assertEquals("R1", sampleReplicateFields.get(samples.get(3)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(3)).isReadOnly());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void removeSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    dialog.samples = mock(Grid.class);
    when(dialog.samples.getDataProvider()).thenReturn(mock(ListDataProvider.class));
    List<Sample> samples = new ArrayList<Sample>(dataset.getSamples());
    Sample sample = samples.get(0);
    presenter.removeSample(sample);
    verify(dialog.samples.getDataProvider()).refreshAll();
    List<Sample> items = presenter.getSamples();
    assertEquals(samples.size() - 1, items.size());
    for (int i = 1; i < samples.size(); i++) {
      assertEquals(samples.get(i), items.get(i - 1));
    }
  }

  @Test
  public void removeSample_NotEditable() {
    Dataset dataset = repository.findById(1L).get();
    dataset.getSamples().forEach(sample -> sample.setEditable(false));
    dataset.getSamples().get(0).setEditable(true);
    presenter.setDataset(dataset);

    presenter.removeSample(dataset.getSamples().get(0));

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
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
    List<Sample> samples = items(dialog.samples);
    assertEquals(2, samples.size());
    assertTrue(find(samples, 2L).isPresent());
    assertEquals("FR2", sampleIdFields.get(samples.get(0)).getValue());
    assertTrue(sampleIdFields.get(samples.get(0)).isReadOnly());
    assertEquals("R2", sampleReplicateFields.get(samples.get(0)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(0)).isReadOnly());
    assertTrue(find(samples, 3L).isPresent());
    assertEquals("FR3", sampleIdFields.get(samples.get(1)).getValue());
    assertTrue(sampleIdFields.get(samples.get(1)).isReadOnly());
    assertEquals("R3", sampleReplicateFields.get(samples.get(1)).getValue());
    assertTrue(sampleReplicateFields.get(samples.get(1)).isReadOnly());
  }

  @Test
  public void dropSample_AboveMoveFarther() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(0), dataset.getSamples().get(2),
        GridDropLocation.ABOVE);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());
  }

  @Test
  public void dropSample_BelowMoveFarther() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(0), dataset.getSamples().get(2),
        GridDropLocation.BELOW);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 3L, samples.get(1).getId());
    assertEquals((Long) 1L, samples.get(2).getId());
  }

  @Test
  public void dropSample_AboveMoveNearer() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(2), dataset.getSamples().get(0),
        GridDropLocation.ABOVE);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 3L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 2L, samples.get(2).getId());
  }

  @Test
  public void dropSample_BelowMoveNearer() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(2), dataset.getSamples().get(0),
        GridDropLocation.BELOW);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 1L, samples.get(0).getId());
    assertEquals((Long) 3L, samples.get(1).getId());
    assertEquals((Long) 2L, samples.get(2).getId());
  }

  @Test
  public void dropSample_Save() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(0), dataset.getSamples().get(2),
        GridDropLocation.ABOVE);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());

    presenter.save();

    verify(service).save(datasetCaptor.capture());
    dataset = datasetCaptor.getValue();
    samples = dataset.getSamples();
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());
  }

  @Test
  public void save_TagsEmpty() {
    fillForm();
    dialog.tags.setValue(new HashSet<>());

    presenter.save();

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertTrue(dataset.getTags().isEmpty());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_ProtocolEmpty() {
    fillForm();
    dialog.protocol.setItems();

    presenter.save();

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
  public void save_ProtocolEmptyNoSample() {
    fillForm();
    dialog.protocol.setItems();

    presenter.save();

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
    fillForm();
    dialog.assay.clear();

    presenter.save();

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
    fillForm();
    dialog.type.setValue(SampleType.NULL);

    presenter.save();

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
    fillForm();
    dialog.target.setValue("");

    presenter.save();

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
    fillForm();
    dialog.strain.setValue("");

    presenter.save();

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
    fillForm();
    dialog.strainDescription.setValue("");

    presenter.save();

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
    fillForm();
    dialog.treatment.setValue("");

    presenter.save();

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
  public void save_DateEmpty() {
    fillForm();
    dialog.date.setValue(null);

    presenter.save();

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.date);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_FirstSampleIdEmpty() {
    fillForm();
    Sample sample = items(dialog.samples).get(0);
    sampleIdFields.get(sample).setValue("");

    presenter.save();

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
    fillForm();
    Sample sample = items(dialog.samples).get(0);
    sampleReplicateFields.get(sample).setValue("");

    presenter.save();

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
    fillForm();
    Sample sample = items(dialog.samples).get(0);
    sampleIdFields.get(sample).setValue("");
    sampleReplicateFields.get(sample).setValue("");

    presenter.save();

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
    fillForm();
    Sample sample = items(dialog.samples).get(1);
    sampleIdFields.get(sample).setValue("");

    presenter.save();

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
    fillForm();
    Sample sample = items(dialog.samples).get(1);
    sampleReplicateFields.get(sample).setValue("");

    presenter.save();

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
    fillForm();
    Sample sample = items(dialog.samples).get(1);
    sampleIdFields.get(sample).setValue("");
    sampleReplicateFields.get(sample).setValue("");

    presenter.save();

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
  public void save_NewSampleIdEmpty() {
    fillForm();
    presenter.addNewSample();
    List<Sample> samples = items(dialog.samples);
    Sample sample = samples.get(2);
    sampleReplicateFields.get(sample).setValue(sampleReplicate2 + "-new");
    sampleIdFields.get(sample).setValue("");

    presenter.save();

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(2);
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
  public void save_NewSampleReplicateEmpty() {
    fillForm();
    presenter.addNewSample();
    List<Sample> samples = items(dialog.samples);
    Sample sample = samples.get(2);
    sampleIdFields.get(sample).setValue(sampleId2 + "-new");
    sampleReplicateFields.get(sample).setValue("");

    presenter.save();

    List<BinderValidationStatus<Sample>> statuses = presenter.validateSamples();
    BinderValidationStatus<Sample> status = statuses.get(2);
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
  public void save_NewDataset() {
    fillForm();

    presenter.save();

    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains(tag1));
    assertTrue(dataset.getTags().contains(tag2));
    assertEquals(date, dataset.getDate());
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
    assertEquals(date, sample.getDate());
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
    assertEquals(date, sample.getDate());
    verify(dialog).showNotification(resources.message(SAVED, dataset.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateDataset() {
    Dataset dataset = repository.findById(2L).get();
    presenter.setDataset(dataset);
    fillForm();

    presenter.save();

    verify(service).save(datasetCaptor.capture());
    dataset = datasetCaptor.getValue();
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains(tag1));
    assertTrue(dataset.getTags().contains(tag2));
    assertEquals(date, dataset.getDate());
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
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
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
    assertEquals(LocalDate.of(2018, 10, 22), sample.getDate());
    verify(dialog).showNotification(resources.message(SAVED, dataset.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.cancel();

    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void delete() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);

    presenter.delete();

    verify(service, never()).save(any());
    verify(service).delete(dataset);
    verify(dialog).close();
    verify(dialog).showNotification(resources.message(DELETED, dataset.getName()));
    verify(dialog, never()).fireSavedEvent();
    verify(dialog).fireDeletedEvent();
  }
}
